package org.wso2.event.client;

import org.wso2.event.server.EventServerUtils;
import org.wso2.event.server.StreamDefinition;
import org.wso2.event.server.StreamRuntimeInfo;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.ByteBuffer;

public class EventClient {

    private final String hostUrl;
    private final StreamDefinition streamDefinition;
    private final StreamRuntimeInfo streamRuntimeInfo;
    private OutputStream outputStream;
    private Socket clientSocket;


    public EventClient(String hostUrl, StreamDefinition streamDefinition) throws Exception {
        this.hostUrl = hostUrl;
        this.streamDefinition = streamDefinition;
        this.streamRuntimeInfo = EventServerUtils.createStreamRuntimeInfo(streamDefinition);

        System.out.println("Sending to " + hostUrl);
        String[] hp = hostUrl.split(":");
        String host = hp[0];
        int port = Integer.parseInt(hp[1]);
        clientSocket = new Socket(host, port);
//	    clientSocket = new Socket("184.72.186.131", 7611);
        outputStream = new BufferedOutputStream(clientSocket.getOutputStream());
    }

    public void close() {
        try {
            outputStream.flush();
            clientSocket.close();
        } catch (Throwable e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }


    public void sendEvent(Object[] event) throws IOException {
        outputStream.write((byte) streamRuntimeInfo.getStreamId().length());
        outputStream.write((streamRuntimeInfo.getStreamId()).getBytes("UTF-8"));

        ByteBuffer buf = ByteBuffer.allocate(streamRuntimeInfo.getFixedMessageSize());
        int[] stringDataIndex = new int[streamRuntimeInfo.getNoOfStringAttributes()];
        int stringIndex = 0;
        StreamDefinition.Type[] types = streamRuntimeInfo.getAttributeTypes();
        for (int i = 0, typesLength = types.length; i < typesLength; i++) {
            StreamDefinition.Type type = types[i];
            switch (type) {
                case INT:
                    buf.putInt((Integer) event[i]);
                    continue;
                case LONG:
                    buf.putLong((Long) event[i]);
                    continue;
                case BOOLEAN:
                    buf.put((byte) (((Boolean) event[i]) ? 1 : 0));
                    continue;
                case FLOAT:
                    buf.putFloat((Float) event[i]);
                    continue;
                case DOUBLE:
                    buf.putDouble((Double) event[i]);
                    continue;
                case STRING:
                    buf.putShort((short) ((String) event[i]).length());
                    stringDataIndex[stringIndex] = i;
                    stringIndex++;
            }
        }

        outputStream.write(buf.array());
        for (int aStringIndex : stringDataIndex) {
            outputStream.write(((String) event[aStringIndex]).getBytes("UTF-8"));
        }
        outputStream.flush();

    }

//
//    public static void main(String[] args) throws Exception {
//
//        Thread.sleep(1000);
//        System.out.println("Start testing");
//        Random random = new Random();
//        SimpleEventHandler client = new SimpleEventHandler("localhost:7612");
//
//        for (int i = 0; i < 100000000; i++) {
//            client.sendEvent(new Object[]{random.nextInt(), random.nextFloat(), "Abcdefghijklmnop" + random.nextLong(), random.nextInt()}, streamRuntimeInfo;
//
//        }
//    }
}
