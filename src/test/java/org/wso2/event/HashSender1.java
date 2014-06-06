package org.wso2.event;

import org.wso2.event.client.EventClient;
import org.wso2.event.client.util.GeneralHashFunctionLibrary;
import org.wso2.event.client.util.HashFactory;
import org.wso2.event.server.StreamDefinition;

import java.util.Random;

/**
 * Created by sameerak on 6/7/14.
 */
public class HashSender1 {


    public static void main(String[] args) throws Exception {

        String[] serverLocations = {"localhost:7615", "localhost:7616", "localhost:7617"};

        if (args.length != 0 && args[0] != null) {
            String receivers = args[0];
            serverLocations = receivers.split("/");
        }

        StreamDefinition streamDefinition = new StreamDefinition();
        streamDefinition.setStreamId("TestStream1");
        streamDefinition.addAttribute("att1", StreamDefinition.Type.INT);
        streamDefinition.addAttribute("att2", StreamDefinition.Type.FLOAT);
        streamDefinition.addAttribute("att3", StreamDefinition.Type.STRING);
        streamDefinition.addAttribute("att4", StreamDefinition.Type.INT);



        EventClient[] eventSenders = new EventClient[serverLocations.length];

        for (int i = 0; i < serverLocations.length; i++){
            eventSenders[i] = new EventClient(serverLocations[i], streamDefinition);
        }


//        EventClient eventClient = new EventClient("localhost:7612", streamDefinition);


        Thread.sleep(1000);
        System.out.println("Start testing");
        Random random = new Random();

        //Hash function distribution
        HashFactory hashFactory = new GeneralHashFunctionLibrary();

        int choice = 0;
        long hashValue;
        for (int i = 0; i < 1000000000; i++) {
//            choice = i%serverLocations.length;
//            eventSenders[choice].sendEvent(new Object[]{random.nextInt(), random.nextFloat(), "Abcdefghijklmnop" + random.nextLong(), random.nextInt()});
            Object[] event = new Object[]{random.nextInt(), random.nextFloat(), "Abcdefghijklmnop" + random.nextLong(), random.nextInt()};
            hashValue = hashFactory.RSHash(event[0].toString());
            choice = (int) (hashValue%serverLocations.length);
            eventSenders[choice].sendEvent(event);
//            eventClient.sendEvent(new Object[]{random.nextInt(), random.nextFloat(), "Abcdefghijklmnop" + random.nextLong(), random.nextInt()});

        }
    }
}
