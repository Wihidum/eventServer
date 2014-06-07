package org.wso2.event;

import org.wso2.event.client.EventClient;
import org.wso2.event.server.EventServer;
import org.wso2.event.server.EventServerConfig;
import org.wso2.event.server.StreamDefinition;
import org.wso2.siddhi.core.SiddhiManager;
import org.wso2.siddhi.core.event.Event;
import org.wso2.siddhi.core.stream.input.InputHandler;

import java.io.IOException;

/**
 * Created by sameerak on 6/6/14.
 */
public class EventProcessor7616 {
    private static SiddhiManager siddhiManager;
    private static volatile long count=0;

    public static void main(String[] args) throws Exception {

        StreamDefinition streamDefinition = new StreamDefinition();
        streamDefinition.setStreamId("TestStream");
        streamDefinition.addAttribute("att1", StreamDefinition.Type.INT);
        streamDefinition.addAttribute("att2", StreamDefinition.Type.FLOAT);
        streamDefinition.addAttribute("att3", StreamDefinition.Type.STRING);
        streamDefinition.addAttribute("att4", StreamDefinition.Type.INT);
        final EventClient eventClient = new EventClient("localhost:7700", streamDefinition); //creating a connection to the output receiver

        siddhiManager = new SiddhiManager();
        StringBuilder stringBuilder = new StringBuilder();
        String attributeStr = streamDefinition.getAttributeList().get(0).getName()+" "+streamDefinition.getAttributeList().get(0).getType().toString().toLowerCase();
        for(int i=1; i<streamDefinition.getAttributeList().size();i++){
            attributeStr += "," + streamDefinition.getAttributeList().get(i).getName() + " " + streamDefinition.getAttributeList().get(i).getType().toString().toLowerCase();
        }

        siddhiManager.defineStream("define stream "+streamDefinition.getStreamId()+" ( "+attributeStr+" )");
        siddhiManager.addQuery("from  TestStream [att1>50]" +
                "select att1, att2, att3, att4 "+
                "insert into StockQuote ;");
        siddhiManager.addCallback("StockQuote", new org.wso2.siddhi.core.stream.output.StreamCallback() {
            @Override
            public void receive(Event[] events) {
//                EventPrinter.print(events);
//                count++;
                //send processed events to the output receiver
                //get event data from the event array and send them individually to the reciever
                for(Event event : events){
                    try {
                        eventClient.sendEvent(event.getData());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        EventServer eventServer = new EventServer(new EventServerConfig(7616), streamDefinition, new org.wso2.event.server.StreamCallback() {
            @Override
            public void receive(Object[] event) {
//                System.out.println("test");
                InputHandler inputHandler = siddhiManager.getInputHandler("TestStream");
                if(inputHandler != null) {
                    try {
                        inputHandler.send(event);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }else{
                    System.out.println("Could not retrieve stream handler");
                    throw new RuntimeException("Could not retrieve stream handler");
                }

            }
        });

        eventServer.start();

        Thread.sleep(10000);
    }
}
