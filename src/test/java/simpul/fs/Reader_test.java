package simpul.fs;

import org.junit.Before;
import org.junit.Test;
import simpul.eventloop.EventLoop;

import java.nio.charset.Charset;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

public class Reader_test {
    private EventLoop eventLoop;
    private Reader fs;
    private final boolean[] itRun=new boolean[1];

    @Before
    public void before(){
        eventLoop = new EventLoop();
        fs = new Reader(eventLoop);
        itRun[0] = false;
    }

    @Test
    public void itReadTextFileContent() {


        eventLoop.runTicket(() -> fs.readFile("TestAssets/test.txt","UTF-8",(err,data)->{
            assertThat(data, equalTo("this is a test"));
            itRun[0] = true;
        }));


        eventLoop.await(100);
        assertThat(itRun[0], equalTo(true));

    }

    @Test
    public void itReadBinaryFileContent() {


        eventLoop.runTicket(() -> fs.readFile("TestAssets/test.txt",(err,data)->{
            String text = new String(data.array(),0,14, Charset.defaultCharset());
            assertThat(text, equalTo("this is a test"));
            itRun[0] = true;
        }));


        eventLoop.await(100);
        assertThat(itRun[0], equalTo(true));

    }
}
