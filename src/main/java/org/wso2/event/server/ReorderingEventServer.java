package org.wso2.event.server;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

public class ReorderingEventServer {

    private EventServerConfig eventServerConfig = new EventServerConfig(7211);
    private StreamDefinition streamDefinition;
    private StreamCallback streamCallback;
    private ExecutorService pool;
    private StreamRuntimeInfo streamRuntimeInfo;
    private int count;
    private List<Queue<Object[]>> reorderQueueList;
    private volatile List<Long> lastElementList;
    private final int MAX_CONNECTIONS = eventServerConfig.getNumberOfThreads();
    private final int AVG_EVENT_CAPACITY = 1000;

    public ReorderingEventServer(EventServerConfig eventServerConfig, StreamDefinition streamDefinition, StreamCallback streamCallback) {
        this.eventServerConfig = eventServerConfig;
        this.streamDefinition = streamDefinition;
        this.streamCallback = streamCallback;
        this.streamRuntimeInfo = EventServerUtils.createStreamRuntimeInfo(streamDefinition);
        pool = Executors.newFixedThreadPool(eventServerConfig.getNumberOfThreads());
        count = 0;
        reorderQueueList = new ArrayList<Queue<Object[]>>();
        lastElementList = new CopyOnWriteArrayList<Long>();

    }


    public void start() throws Exception {
        System.out.println("Starting on " + eventServerConfig.getPort());
        ServerSocket welcomeSocket = new ServerSocket(eventServerConfig.getPort());
        while (true) {
            try {
                Thread reorderThread = new Thread(new Runnable() {

                    @Override
                    public void run() {
                        try {
                            while (true) {
                                Thread.sleep(500);//per every 0.5 sec
                                if (lastElementList.size() > 0) {
                                    List<Long> tempList = new ArrayList<Long>(MAX_CONNECTIONS);
                                    Iterator<Long> longIterator = lastElementList.iterator();
                                    while (longIterator.hasNext()) {
                                        tempList.add(longIterator.next());
                                    }
                                    Collections.sort(tempList);
                                    long minimum = tempList.get(0);
                                    for (Queue<Object[]> queue : reorderQueueList) {
                                        while (true) {
                                            if (queue.size() > 0 && (Long) (queue.peek()[0]) <= minimum) {
                                                streamCallback.receive(queue.poll());
                                            } else {
                                                break;
                                            }
                                        }
                                    }
                                }
                            }
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                });
                reorderThread.start();
                final Socket connectionSocket = welcomeSocket.accept();
                pool.submit(new Runnable() {

                    @Override
                    public void run() {
                        try {
                            int position = getCount();
                            System.out.println("position :" + position);
                            reorderQueueList.add(new LinkedBlockingQueue<Object[]>(AVG_EVENT_CAPACITY));
                            BufferedInputStream in = new BufferedInputStream(connectionSocket.getInputStream());

                            while (true) {
                                int streamNameSize = loadData(in) & 0xff;
                                byte[] streamNameData = loadData(in, new byte[streamNameSize]);
//                                System.out.println(new String(streamNameData, 0, streamNameData.length));

                                Object[] event = new Object[streamRuntimeInfo.getNoOfAttributes()];
                                byte[] fixedMessageData = loadData(in, new byte[streamRuntimeInfo.getFixedMessageSize()]);

                                ByteBuffer bbuf = ByteBuffer.wrap(fixedMessageData, 0, fixedMessageData.length);
                                StreamDefinition.Type[] attributeTypes = streamRuntimeInfo.getAttributeTypes();
                                for (int i = 0; i < attributeTypes.length; i++) {
                                    StreamDefinition.Type type = attributeTypes[i];
                                    switch (type) {
                                        case INT:
                                            event[i] = bbuf.getInt();
                                            continue;
                                        case LONG:
                                            event[i] = bbuf.getLong();
                                            continue;
                                        case BOOLEAN:
                                            event[i] = bbuf.get() == 1;
                                            continue;
                                        case FLOAT:
                                            event[i] = bbuf.getFloat();
                                            continue;
                                        case DOUBLE:
                                            event[i] = bbuf.getLong();
                                            continue;
                                        case STRING:
                                            int size = bbuf.getShort() & 0xffff;
                                            byte[] stringData = loadData(in, new byte[size]);
                                            event[i] = new String(stringData, 0, stringData.length);
                                    }
                                }
                                try {
                                    lastElementList.remove(position);
                                } catch (Exception e) {
                                    //do nothing
                                }
                                lastElementList.add(position, (Long) event[0]);
                                reorderQueueList.get(position).offer(event);
                                //streamCallback.receive(event);
                            }
                        } catch (IOException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    }
                });
            } catch (Throwable e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

    }

    private synchronized int getCount() {
        int result = count;
        count++;
        return result;
    }

    private int loadData(BufferedInputStream in) throws IOException {

        while (true) {
            int byteData = in.read();
            if (byteData != -1) {
                return byteData;
            }
        }
    }

    private byte[] loadData(BufferedInputStream in, byte[] dataArray) throws IOException {

        int start = 0;
        while (true) {
            int readCount = in.read(dataArray, 0, dataArray.length - start);
            start += readCount;
            if (start == dataArray.length) {
                return dataArray;
            }
        }
    }
}
