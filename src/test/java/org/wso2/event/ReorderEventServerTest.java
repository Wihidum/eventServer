package org.wso2.event;


import org.wso2.event.server.EventServerConfig;
import org.wso2.event.server.ReorderingEventServer;
import org.wso2.event.server.StreamDefinition;
import org.wso2.siddhi.core.SiddhiManager;
import org.wso2.siddhi.core.event.Event;
import org.wso2.siddhi.core.stream.input.InputHandler;
import org.wso2.siddhi.core.util.EventPrinter;

public class ReorderEventServerTest {
    private static SiddhiManager siddhiManager;

    public static void main(String[] args) throws Exception {

        StreamDefinition streamDefinition = new StreamDefinition();
        streamDefinition.setStreamId("TestStream");
        streamDefinition.addAttribute("att1", StreamDefinition.Type.LONG);
        streamDefinition.addAttribute("att2", StreamDefinition.Type.FLOAT);
        streamDefinition.addAttribute("att3", StreamDefinition.Type.STRING);
        streamDefinition.addAttribute("att4", StreamDefinition.Type.INT);

        siddhiManager = new SiddhiManager();
        String attributeStr = streamDefinition.getAttributeList().get(0).getName() + " " + streamDefinition.getAttributeList().get(0).getType().toString().toLowerCase();
        for (int i = 1; i < streamDefinition.getAttributeList().size(); i++) {
            attributeStr += "," + streamDefinition.getAttributeList().get(i).getName() + " " + streamDefinition.getAttributeList().get(i).getType().toString().toLowerCase();
        }

        siddhiManager.defineStream("define stream " + streamDefinition.getStreamId() + " ( " + attributeStr + " )");
        siddhiManager.addQuery("from  TestStream " +
                "select att1, att2, att3, att4 " +
                "insert into StockQuote ;");
        siddhiManager.addCallback("StockQuote", new org.wso2.siddhi.core.stream.output.StreamCallback() {
            @Override
            public void receive(Event[] events) {
                EventPrinter.print(events);
            }
        });

        ReorderingEventServer eventServer = new ReorderingEventServer(new EventServerConfig(7612), streamDefinition, new org.wso2.event.server.StreamCallback() {
            @Override
            public void receive(Object[] event) {
                InputHandler inputHandler = siddhiManager.getInputHandler("TestStream");
                if (inputHandler != null) {
                    try {
                        inputHandler.send(event);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                } else {
                    System.out.println("Could not retrieve stream handler");
                    throw new RuntimeException("Could not retrieve stream handler");
                }
            }
        });

        eventServer.start();

        Thread.sleep(10000);
    }
}
