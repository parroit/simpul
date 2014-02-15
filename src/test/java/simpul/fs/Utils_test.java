package simpul.fs;

import org.junit.Before;
import org.junit.Test;
import simpul.eventloop.EventLoop;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

public class Utils_test {
    private EventLoop eventLoop;
    private Utils fs;
    private final boolean[] itRun=new boolean[1];

    @Before
    public void before(){
        eventLoop = new EventLoop();
        eventLoop.on("uncaughtException", (Throwable t)-> t.printStackTrace());
        fs = new Utils(eventLoop);
        itRun[0] = false;
    }

    @Test
    public void sizeReturnFileSizeInBytes() {


        eventLoop.runTicket(() -> fs.size("TestAssets/test.txt",(err,data)->{
            assertThat(data, equalTo(14L));
            assertThat(err, equalTo(null));
            itRun[0] = true;
        }));


        eventLoop.await(100);
        assertThat(itRun[0], equalTo(true));

    }

    @Test
    public void statReturnFileAtributes() {


        eventLoop.runTicket(() -> fs.stat("TestAssets/test.txt", (err, data) -> {
            assertThat(data.size(), equalTo(14L));
            assertThat(data.isRegularFile(), equalTo(true));
            assertThat(err, equalTo(null));
            itRun[0] = true;
        }));


        eventLoop.await(100);
        assertThat(itRun[0], equalTo(true));
    }
      
    @Test
    public void sizeReturnException() {


        eventLoop.runTicket(() -> fs.size("TestAssets/bad-file.txt",(err,data)->{
            assertThat(data, equalTo(null));
            assertThat(err.getClass().getSimpleName(), equalTo("NoSuchFileException"));
            assertThat(err.getMessage(), equalTo("TestAssets/bad-file.txt"));
            itRun[0] = true;
        }));


        eventLoop.await(100);
        assertThat(itRun[0], equalTo(true));

    }

}
