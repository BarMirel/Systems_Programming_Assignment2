package bgu.spl.mics.application.objects;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

public class LastFrames {
    private static volatile LastFrames instance;
    private ConcurrentHashMap<String, StampedDetectedObjects> lastDetectedObjects;
    private ConcurrentHashMap<String, ArrayList<TrackedObject>> lastTrackedObjects;
    public LastFrames() {
        lastDetectedObjects = new ConcurrentHashMap<>();
        lastTrackedObjects = new ConcurrentHashMap<>();
    }
    public static LastFrames getInstance() {
        if (instance == null) {
            synchronized (LastFrames.class) {
                if (instance == null) {
                    instance = new LastFrames();
                }
            }
        }
        return instance;
    }

    public ConcurrentHashMap<String, StampedDetectedObjects> getLastDetectedObjects() {
        return lastDetectedObjects;
    }

    public ConcurrentHashMap<String, ArrayList<TrackedObject>> getLastTrackedObjects() {
        return lastTrackedObjects;
    }

    public void updateLastDetectedObjects(String cameraService, StampedDetectedObjects stampedDetectedObjects){
        lastDetectedObjects.remove(cameraService);
        lastDetectedObjects.put(cameraService, stampedDetectedObjects);
    }

    public void updateLastTrackedObjects(String lidarService, ArrayList<TrackedObject> trackedObjects){
        lastTrackedObjects.remove(lidarService);
        lastTrackedObjects.put(lidarService, trackedObjects);
    }
}
