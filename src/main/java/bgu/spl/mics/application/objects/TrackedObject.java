package bgu.spl.mics.application.objects;

import java.util.ArrayList;

/**
 * Represents an object tracked by the LiDAR.
 * This object includes information about the tracked object's ID, description, 
 * time of tracking, and coordinates in the environment.
 */
public class TrackedObject {
    private final String id;
    private final int time;
    private final String description;
    private final ArrayList<CloudPoint> cloudPoints;


    public TrackedObject(String id, int time, String description, ArrayList<CloudPoint> cloudPoints) {
        this.id = id;
        this.time = time;
        this.description = description;
        this.cloudPoints = cloudPoints;
    }

    public String getId() {
        return id;
    }

    public int getTime() {
        return time;
    }

    public String getDescription() {
        return description;
    }

    public ArrayList<CloudPoint> getCloudPoints() {
        return cloudPoints;
    }
}
