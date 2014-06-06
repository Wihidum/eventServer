package org.wso2.event;

import org.wso2.event.client.EventClient;
import org.wso2.event.server.StreamDefinition;

import java.util.Random;

/**
 * Created by sameerak on 6/5/14.
 */
public class RoundRobinSender {


    public static void main(String[] args) throws Exception {

        StreamDefinition streamDefinition = new StreamDefinition();
        streamDefinition.setStreamId("TestStream");
        streamDefinition.addAttribute("att1", StreamDefinition.Type.INT);
        streamDefinition.addAttribute("att2", StreamDefinition.Type.FLOAT);
        streamDefinition.addAttribute("att3", StreamDefinition.Type.STRING);
        streamDefinition.addAttribute("att4", StreamDefinition.Type.INT);

        String[] serverLocations = {"localhost:7615", "localhost:7616", "localhost:7617"}; //TODO give filtering server

        EventClient[] eventSenders = new EventClient[serverLocations.length];

        for (int i = 0; i < serverLocations.length; i++){
            eventSenders[i] = new EventClient(serverLocations[i], streamDefinition);
        }


//        EventClient eventClient = new EventClient("localhost:7612", streamDefinition);


        Thread.sleep(1000);
        System.out.println("Start testing");
        Random random = new Random();

        //round robin distribution

        int choice = 0;
        for (int i = 0; i < 1000000000; i++) {
            choice = i%serverLocations.length;
            eventSenders[choice].sendEvent(new Object[]{random.nextInt(), random.nextFloat(), "Abcdefghijklmnop" + random.nextLong(), random.nextInt()});
//            eventClient.sendEvent(new Object[]{random.nextInt(), random.nextFloat(), "Abcdefghijklmnop" + random.nextLong(), random.nextInt()});

        }
    }
}
