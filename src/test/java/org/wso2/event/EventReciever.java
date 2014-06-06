package org.wso2.event;

import org.wso2.event.server.EventServer;
import org.wso2.event.server.EventServerConfig;
import org.wso2.event.server.StreamCallback;
import org.wso2.event.server.StreamDefinition;

import java.util.concurrent.atomic.AtomicLong;

public class EventReciever {

    private static volatile AtomicLong count=new AtomicLong();
    private static volatile long start=System.currentTimeMillis();

    public static void main(String[] args) throws Exception {

        StreamDefinition streamDefinition = new StreamDefinition();
        streamDefinition.setStreamId("TestStream");
        streamDefinition.addAttribute("att1", StreamDefinition.Type.INT);
        streamDefinition.addAttribute("att2", StreamDefinition.Type.FLOAT);
        streamDefinition.addAttribute("att3", StreamDefinition.Type.STRING);
        streamDefinition.addAttribute("att4", StreamDefinition.Type.INT);

        EventServer eventServer = new EventServer(new EventServerConfig(7613), streamDefinition, new StreamCallback() {
            @Override
            public void receive(Object[] event) {
                long value =  count.incrementAndGet();
                if(value%10000000==0){
                    long end=System.currentTimeMillis();
                    System.out.println("TP:"+(10000000*1000.0/(end-start)));
                    start=end;
                }
            }
        });

        eventServer.start();

        Thread.sleep(10000);
    }
}
