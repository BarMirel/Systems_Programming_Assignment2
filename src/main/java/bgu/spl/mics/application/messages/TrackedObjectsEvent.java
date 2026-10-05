package bgu.spl.mics.application.messages;

import bgu.spl.mics.Event;
import bgu.spl.mics.application.objects.DetectedObject;
import bgu.spl.mics.application.objects.TrackedObject;

import java.util.ArrayList;

public class TrackedObjectsEvent implements Event {
    private final ArrayList<TrackedObject> trackedObjects;
    private final int lidarFrequency;

    public TrackedObjectsEvent(ArrayList<TrackedObject> trackedObjects, int lidarFrequency){
        this.trackedObjects = trackedObjects;
        this.lidarFrequency = lidarFrequency;
    }

    public ArrayList<TrackedObject> getTrackedObjects() {
        return trackedObjects;
    }

    public int getLidarFrequency() {
        return lidarFrequency;
    }
}
