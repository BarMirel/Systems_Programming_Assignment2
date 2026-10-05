package bgu.spl.mics.application.objects;


import java.util.ArrayList;
import java.util.List;


/**
 * Represents a camera sensor on the robot.
 * Responsible for detecting objects in the environment.
 */
public class Camera {
    private final int id;
    private final int frequency;
    private STATUS status;
    private final ArrayList<StampedDetectedObjects> detectedObjects;
    public Camera(int id, int frequency, STATUS status, ArrayList<StampedDetectedObjects> detectedObjects) {
        this.id = id;
        this.frequency = frequency;
        this.status = status;
        this.detectedObjects = detectedObjects;
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

    public ArrayList<StampedDetectedObjects> getDetectedObjectsList() {
        return detectedObjects;
    }

    public void setStatus(STATUS status) {
        this.status = status;
    }

    public ArrayList<StampedDetectedObjects> cameraTick(int time){
        ArrayList<StampedDetectedObjects> service = new ArrayList<>();
        for(StampedDetectedObjects sdo : detectedObjects) {
            if (sdo.getTime() == time){
                if (sdo == detectedObjects.get(detectedObjects.size()-1)){
                    status = STATUS.DOWN;
                }
                service.add(sdo);
            }
        }
        return service;
    }
}