package simpul.core;


import org.junit.Before;
import org.junit.Test;
import simpul.eventloop.EventLoop;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

public class Timers_test {
    private EventLoop eventLoop;
    private Timers timers;
    @Before
    public void before(){
        eventLoop = new EventLoop();
        timers = new Timers(eventLoop);
    }
    
    @Test
    public void itRunTimeouts() {
        boolean[] itRun=new boolean[1];

        
        eventLoop.runTicket(() -> {
            timers.setTimeout(()->{
                itRun[0] = true;
            },100);

        });

        eventLoop.await(200);
        assertThat(itRun[0], equalTo(true));
    }


    @Test
    public void itCancelTimeouts() {
        boolean[] itRun=new boolean[1];


        eventLoop.runTicket(() -> {
            long[] id = new long[1];
            id[0] = timers.setTimeout(()->{
                itRun[0] = true;
            },100);

            timers.clearTimeout(id[0]);

        });

        assertThat(eventLoop.await(200), equalTo(true));
        assertThat(itRun[0], equalTo(false));
    }

    @Test
    public void itCancelIntervals() {
        boolean[] itRun=new boolean[1];


        eventLoop.runTicket(() -> {
            long[] id = new long[1];
            id[0] = timers.setInterval(() -> {
                itRun[0] = true;
            }, 100);

            timers.clearInterval(id[0]);

        });

        assertThat(eventLoop.await(200),equalTo(true));
        assertThat(itRun[0], equalTo(false));
    }


    @Test
    public void itRunIntervals() {
        int[] times=new int[]{0};


        eventLoop.runTicket(() -> {
            long[] id = new long[1];

            id[0] = timers.setInterval(()->{
                times[0]++;
                if (times[0] == 10) {
                    timers.clearInterval(id[0]);
                }
            },10);

        });

        eventLoop.await(200);
        assertThat(times[0], equalTo(10));
    }

}
