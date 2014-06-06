package org.wso2.event;

import org.wso2.event.client.EventClient;
import org.wso2.event.server.EventServer;
import org.wso2.event.server.EventServerConfig;
import org.wso2.event.server.StreamCallback;
import org.wso2.event.server.StreamDefinition;

/**
 * Created by sameerak on 6/5/14.
 */
public class EventProcessor {

    public static void main(String[] args) throws Exception {

        int port = 7613;
        String receiver = "localhost:7613";
        if (args.length != 0 && args[0] != null) {
            port = Integer.parseInt(args[0]);
        }
        if (args.length != 0 && args[1] != null) {
            receiver = args[1];
        }


        final StreamDefinition streamDefinition = new StreamDefinition();
        streamDefinition.setStreamId("TestStream");
        streamDefinition.addAttribute("att1", StreamDefinition.Type.INT);
        streamDefinition.addAttribute("att2", StreamDefinition.Type.FLOAT);
        streamDefinition.addAttribute("att3", StreamDefinition.Type.STRING);
        streamDefinition.addAttribute("att4", StreamDefinition.Type.INT);
        final EventClient eventClient = new EventClient(receiver, streamDefinition);

        EventServer eventServer = new EventServer(new EventServerConfig(port), streamDefinition, new StreamCallback() {
            @Override
            public void receive(Object[] event){
                try {
                    eventClient.sendEvent(event);
                } catch (Exception e) {
                    e.printStackTrace();
                }

//                System.out.println(Arrays.deepToString(event));
            }
        });

        eventServer.start();

        Thread.sleep(10000);
    }
}
