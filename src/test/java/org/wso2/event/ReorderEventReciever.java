package org.wso2.event;

import org.wso2.event.server.EventServerConfig;
import org.wso2.event.server.ReorderingEventServer;
import org.wso2.event.server.StreamDefinition;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by tishan on 6/7/14.
 */
public class ReorderEventReciever {

    private static volatile AtomicLong count = new AtomicLong();
    private static volatile long start = System.currentTimeMillis();

    public static void main(String[] args) throws Exception {

        int port = 7700;
        if (args.length != 0 && args[0] != null) {
            port = Integer.parseInt(args[0]);
        }

        StreamDefinition streamDefinition = new StreamDefinition();
        streamDefinition.setStreamId("TestStream");
        streamDefinition.addAttribute("att1", StreamDefinition.Type.LONG);
        streamDefinition.addAttribute("att2", StreamDefinition.Type.FLOAT);
        streamDefinition.addAttribute("att3", StreamDefinition.Type.STRING);
        streamDefinition.addAttribute("att4", StreamDefinition.Type.INT);


        ReorderingEventServer eventServer = new ReorderingEventServer(new EventServerConfig(port), streamDefinition, new org.wso2.event.server.StreamCallback() {
            @Override
            public void receive(Object[] event) {
                //System.out.println("came");
                long value = count.incrementAndGet();
                if (value % 10000 == 0) {
                    long end = System.currentTimeMillis();
                    System.out.println("TP:" + (10000000 * 1000.0 / (end - start)));
                    start = end;
                }
            }
        });

        eventServer.start();

        Thread.sleep(10000);
    }
}
