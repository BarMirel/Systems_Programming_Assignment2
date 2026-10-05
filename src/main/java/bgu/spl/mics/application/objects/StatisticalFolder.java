package bgu.spl.mics.application.objects;


import bgu.spl.mics.MessageBusImpl;


import java.util.concurrent.atomic.AtomicInteger;


/**
 * Holds statistical information about the system's operation.
 * This class aggregates metrics such as the runtime of the system,
 * the number of objects detected and tracked, and the number of landmarks identified.
 */
public class StatisticalFolder {
    private static volatile StatisticalFolder instance;
    private AtomicInteger systemRuntime;
    private AtomicInteger numDetectedObjects;
    private AtomicInteger numTrackedObjects;
    private AtomicInteger numLandmarks;
    public StatisticalFolder() {
        this.systemRuntime = new AtomicInteger(0);
        this.numDetectedObjects = new AtomicInteger(0);
        this.numTrackedObjects = new AtomicInteger(0);
        this.numLandmarks = new AtomicInteger(0);
    }
    public static StatisticalFolder getInstance() {
        if (instance == null) {
            synchronized (StatisticalFolder.class) {
                if (instance == null) {
                    instance = new StatisticalFolder();
                }
            }
        }
        return instance;
    }
    public int getSystemRuntime() {
        return systemRuntime.get();
    }
    public synchronized int getNumDetectedObjects() {
        return numDetectedObjects.get();
    }
    public synchronized int getNumTrackedObjects() {
        return numTrackedObjects.get();
    }
    public synchronized int getNumLandmarks() {
        return numLandmarks.get();
    }
    public void incrementSystemRuntime(){
        this.systemRuntime.incrementAndGet();
    }
    public void incrementNumDetectedObjects() {
        numDetectedObjects.incrementAndGet();
    }
    public void incrementNumTrackedObjects() {
        numTrackedObjects.incrementAndGet();
    }
    public void incrementNumLandmarks() {
        numLandmarks.incrementAndGet();
    }
}