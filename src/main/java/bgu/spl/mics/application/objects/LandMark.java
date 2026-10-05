package bgu.spl.mics.application.objects;


import java.util.ArrayList;


/**
 * Represents a landmark in the environment map.
 * Landmarks are identified and updated by the FusionSlam service.
 */
public class LandMark {
    private final String id;
    private final String description;
    private ArrayList<CloudPoint> Coordinates;


    public LandMark(String id, String description) {
        this.id = id;
        this.description = description;
        this.Coordinates = new ArrayList<CloudPoint>();
    }
    public String getId() {
        return id;
    }
    public String getDescription() {
        return description;
    }
    public ArrayList<CloudPoint> getCoordinates(){
        return Coordinates;
    }
    public void setCoordinates(ArrayList<CloudPoint> Coordinates){
        this.Coordinates = Coordinates;
    }
}