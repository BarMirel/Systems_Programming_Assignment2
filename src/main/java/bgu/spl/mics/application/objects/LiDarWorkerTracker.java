package bgu.spl.mics.application.objects;


import java.util.ArrayList;
import java.util.List;


/**
 * LiDarWorkerTracker is responsible for managing a LiDAR worker.
 * It processes DetectObjectsEvents and generates TrackedObjectsEvents by using data from the LiDarDataBase.
 * Each worker tracks objects and sends observations to the FusionSlam service.
 */
public class LiDarWorkerTracker {
    private final int id;
    private final int frequency;
    private STATUS status;
    private final ArrayList<TrackedObject> trackedObjects;
    public LiDarWorkerTracker(int id, int frequency, STATUS status, ArrayList<TrackedObject> trackedObjects) {
        this.id = id;
        this.frequency = frequency;
        this.status = status;
        this.trackedObjects = trackedObjects;
    }


    public int getId() {
        return id;
    }
    public int getFrequency() {
        return frequency;
    }
    public STATUS getStatus() {
        return status;
    }
    public ArrayList<TrackedObject> getTrackedObject() {
        return trackedObjects;
    }
    public void setStatus(STATUS status) {
        this.status = status;
    }
}