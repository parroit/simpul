/*
package simpul.fs;

import org.fusesource.jansi.Ansi;
import simpul.eventloop.EventLoop;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.Charset;

import static org.fusesource.jansi.Ansi.Color.GREEN;
import static org.fusesource.jansi.Ansi.Color.RED;
import static org.fusesource.jansi.Ansi.ansi;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

abstract  class Mocha {
    private int indention;
    protected abstract Runnable test();
    protected EventLoop eventLoop;

    private Ansi stackTrace(Throwable t){
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);

        t.printStackTrace(pw);
        return ansi().fg(RED).a(t.getMessage() + "\n").reset().a(sw.toString());
    }

    //@Test
    public void mocha() {
        Runnable test = test();

        eventLoop = new EventLoop();
        eventLoop.on("uncaughtException",(Throwable ex)->{
            System.out.println(stackTrace(ex));
        });
        eventLoop.runTicket(test);
        eventLoop.await(100);

    }

    protected void describe(String description,Runnable suite){
        try {
            System.out.println(indent()+"\u2022 "+description);
            indention++;
            suite.run();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private String indent() {
        return new String(new char[indention]).replace("\0", "\t");
    }

    protected void it(String assertion,Runnable test){
        try {
            test.run();
            System.out.println(ansi().fg(GREEN).a(indent() + "\u2022 it " + assertion).reset());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}

public class Mocha_test extends Mocha{




    @Override
    protected Runnable test() {
        return ()-> {
            Reader fs = new Reader(eventLoop);
            describe("fs utils",()-> {
                it ("read text file content", ()-> {
                    fs.readFile("TestAssets/test.txt", "UTF-8", (err, data) -> {
                        assertThat(data, equalTo("this is a test"));

                    });
                });

                it ("read binary file content", ()-> {
                    fs.readFile("TestAssets/test.txt", (err, data) -> {
                        String text = new String(data.array(), 0, 14, Charset.defaultCharset());
                        assertThat(text, equalTo(" this is a test"));

                    });

                });

            });
        };
    }
}
*/
