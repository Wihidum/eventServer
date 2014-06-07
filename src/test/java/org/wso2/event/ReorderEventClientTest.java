package org.wso2.event;

import org.wso2.event.client.EventClient;
import org.wso2.event.server.StreamDefinition;

import java.util.Random;


public class ReorderEventClientTest {
    public static void main(String[] args) throws Exception {

        StreamDefinition streamDefinition = new StreamDefinition();
        streamDefinition.setStreamId("TestStream");
        streamDefinition.addAttribute("att1", StreamDefinition.Type.LONG);
        streamDefinition.addAttribute("att2", StreamDefinition.Type.FLOAT);
        streamDefinition.addAttribute("att3", StreamDefinition.Type.STRING);
        streamDefinition.addAttribute("att4", StreamDefinition.Type.INT);

        EventClient eventClient = new EventClient("localhost:7612", streamDefinition);


        Thread.sleep(1000);
        System.out.println("Start testing");
        Random random = new Random();

        for (int i = 0; i < 1000; i++) {
            eventClient.sendEvent(new Object[]{System.currentTimeMillis(), random.nextFloat(), "Abcdefghijklmnop" + random.nextLong(), random.nextInt()});

        }
    }
}
