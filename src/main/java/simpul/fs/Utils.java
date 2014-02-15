package simpul.fs;


import simpul.Interfaces;
import simpul.eventloop.EventLoop;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFileAttributes;

public class Utils {

    private final EventLoop eventLoop;


    public Utils(EventLoop eventLoop) {
        this.eventLoop = eventLoop;
    }

    public void size(String path, Interfaces.Callback<Long> cb){
        Path file = Paths.get(path);

        eventLoop.runInBackground(() -> Files.size(file), cb);

    }

    public void stat(String path, Interfaces.Callback<PosixFileAttributes> cb){
        Path file = Paths.get(path);

        eventLoop.runInBackground(() -> Files.readAttributes(file, PosixFileAttributes.class), cb);

    }




}
