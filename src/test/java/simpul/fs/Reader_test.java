package simpul.fs;

import org.junit.Before;
import org.junit.Test;
import simpul.Interfaces;
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
        eventLoop.on("uncaughtException", (Throwable t)-> t.printStackTrace());
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
    public void itStreamTextFileContent() {


        eventLoop.runTicket(() -> {
            StringBuilder builder= new StringBuilder();
            Interfaces.ReadableStream stream = fs.createReadStream("TestAssets/test.txt", "UTF-8");

            stream.on("data",(data)->{
                builder.append(data);
            });

            stream.on("end",(empty)->{
                String result = builder.toString();
                assertThat(result, equalTo("this is a test"));
                itRun[0] = true;
            });

            stream.on("error",(RuntimeException err)->{
                throw err;
            });
        });


        eventLoop.await(100);
        assertThat(itRun[0], equalTo(true));

    }

    @Test
    public void createReadStreamEmitErrors() {


        eventLoop.runTicket(() -> {
            Interfaces.ReadableStream stream = fs.createReadStream("TestAssets/bad-file.txt", "UTF-8");


            stream.on("error",(RuntimeException err)->{
                assertThat(err.getCause().getClass().getSimpleName(), equalTo("NoSuchFileException"));
                assertThat(err.getMessage(), equalTo("java.nio.file.NoSuchFileException: TestAssets/bad-file.txt"));
                itRun[0] = true;
            });
        });


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
