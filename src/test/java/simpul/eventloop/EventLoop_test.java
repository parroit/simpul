package simpul.eventloop;


import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

public class EventLoop_test {
    private EventLoop eventLoop;
    
    @Before
    public void before(){
        eventLoop = new EventLoop();
    }
    
    @Test
    public void itStart() {
        boolean[] itRun=new boolean[1];

        
        eventLoop.runTicket(() -> {
            itRun[0] = true;
        });

        eventLoop.await(10);
        assertThat(itRun[0], equalTo(true));
    }

    @Test
    public void itEmitEventOnException() {
        String[] message = new String[1];

        eventLoop.on("uncaughtException",(Exception e)->{
            message[0] = e.getMessage();
        });

        eventLoop.runTicket(() -> {
            throw new RuntimeException("test");
        });

        eventLoop.await(10);
        assertThat(message[0], equalTo("test"));
    }

}
