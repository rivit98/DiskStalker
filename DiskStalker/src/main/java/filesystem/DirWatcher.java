package filesystem;

import io.reactivex.rxjava3.core.ObservableEmitter;
import model.FileData;

import java.io.IOException;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;

public class DirWatcher {
    private final WatchService watchService;
    private boolean shutdown = false;
    private final Thread monitor;

    public DirWatcher(WatchService watchService) {
        this.watchService = watchService;
        this.monitor = new Thread(this::loop);
    }

    public void stop(){
        shutdown = true;
        monitor.interrupt();
    }

    public void startMonitoring(){
        System.out.println("monitoring started");
        monitor.start();
    }

    private void emitKey(WatchKey key){
        //TODO: register newly created dirs
        System.out.println(key);
        for (final WatchEvent<?> event : key.pollEvents()) {
            System.out.println(event.kind().name() + " " + event.context());
        }

        key.reset();
    }

    private WatchKey takeKey(){
        try{
            return watchService.take();
        } catch (InterruptedException exception) {
            return null;
        }
    }

    private void loop(){
        for(;;){
            var watchKey = takeKey();
            if(watchKey != null){
                emitKey(watchKey);
            }else{ //thread was interrupted, finish work
                System.out.println("close thread");
                try{
                    watchService.close();
                } catch (IOException exception) {
                    //we dont care
                }
                break;
            }
        }
    }

}
