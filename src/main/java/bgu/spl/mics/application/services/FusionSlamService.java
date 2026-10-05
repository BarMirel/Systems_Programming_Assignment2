package bgu.spl.mics.application.services;


import bgu.spl.mics.application.messages.*;
import bgu.spl.mics.application.objects.*;
import com.google.gson.*;


import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import bgu.spl.mics.MicroService;


/**
 * FusionSlamService integrates data from multiple sensors to build and update
 * the robot's global map.
 *
 * This service receives TrackedObjectsEvents from LiDAR workers and PoseEvents from the PoseService,
 * transforming and updating the map with new landmarks.
 */
public class FusionSlamService extends MicroService {
    private int activeServices;
    private int handledIndex;
    private final FusionSlam fusionSlam;
    private ArrayList<ArrayList<TrackedObject>> trackedObjects;
    private final String folder;
    /**
     * Constructor for FusionSlamService.
     *
     * @param fusionSlam The FusionSLAM object responsible for managing the global map.
     */
    public FusionSlamService(FusionSlam fusionSlam, int activeServices, String folder) {
        super("FusionSlam");
        this.activeServices = activeServices;
        this.handledIndex = 0;
        this.fusionSlam = fusionSlam;
        this.trackedObjects = new ArrayList<>();
        this.folder = folder;
    }


    /**
     * Initializes the FusionSlamService.
     * Registers the service to handle TrackedObjectsEvents, PoseEvents, and TickBroadcasts,
     * and sets up callbacks for updating the global map.
     */
    @Override
    protected void initialize() {
        subscribeBroadcast(TickBroadcast.class, brodcast -> {
            for (int i = handledIndex; i < trackedObjects.size(); i++) {
                for (TrackedObject t : trackedObjects.get(i)) {
                    Pose currPose = null;
                    for (Pose p : fusionSlam.getPoses()) {
                        if (p.getTime() == t.getTime()) {
                            currPose = p;
                            break;
                        }
                    }
                    if(currPose != null){
                        boolean newLandMark = true;
                        LandMark landMark = null;
                        for (LandMark l : fusionSlam.getLandMarks()) {
                            if (t.getId().equals(l.getId())) {
                                newLandMark = false;
                                landMark = l;
                                break;
                            }
                        }
                        fusionSlam.AddOrSetLandmark(t, newLandMark, landMark, currPose);
                        handledIndex = i + 1;
                    }
                }
            }
            if(activeServices == 0 && handledIndex == trackedObjects.size()){
                outputProperJson();
                sendBroadcast(new TerminatedBroadcast(getName()));
                terminate();
            }
        });
        subscribeEvent(TrackedObjectsEvent.class, event ->{
            trackedObjects.add(event.getTrackedObjects());

        });
        subscribeEvent(PoseEvent.class, event -> {
                    fusionSlam.getPoses().add(event.getPose());
                }
        );
        subscribeBroadcast(TerminatedBroadcast.class, broadcast -> {
            activeServices--;
            if(broadcast.getSenderName().equals("TimeService")) {
                outputProperJson();
                terminate();
            }
        });
        subscribeBroadcast(CrashedBroadcast.class, broadcast -> {
            JsonObject outputData = getStatistics();
            outputData.addProperty("Error", broadcast.getDescription());
            outputData.addProperty("faultySensor", broadcast.getSenderName());
            LastFrames.getInstance().getLastDetectedObjects().forEach((name, sdo) -> {
                if(name != null) {
                    JsonArray lastDetectedObjects = new JsonArray();
                    for (DetectedObject detectedObject : sdo.getDetectedObjects()){
                        JsonObject d = new JsonObject();
                        d.addProperty("id", detectedObject.getId());
                        d.addProperty("description", detectedObject.getDescription());
                        lastDetectedObjects.add(d);
                    }
                    outputData.add(name + " lastDetectedObjects", lastDetectedObjects);
                }
            });
            LastFrames.getInstance().getLastTrackedObjects().forEach((name, trackedObjects) -> {
                if(name != null) {
                    JsonArray lastTrackedObjects = new JsonArray();
                    for (TrackedObject trackedObject : trackedObjects){
                        JsonObject t = new JsonObject();
                        t.addProperty("id", trackedObject.getId());
                        t.addProperty("description", trackedObject.getDescription());
                        JsonArray coordinates = new JsonArray();
                        for (CloudPoint cloudPoint : trackedObject.getCloudPoints()) {
                            JsonObject point = new JsonObject();
                            point.addProperty("x", cloudPoint.getX());
                            point.addProperty("y", cloudPoint.getY());
                            coordinates.add(point);
                        }
                        t.add("coordinates", coordinates);
                        lastTrackedObjects.add(t);
                    }
                    outputData.add(name + " CloudPoints", lastTrackedObjects);
                }
            });
            JsonArray prevPoses = new JsonArray();
            for(Pose currentPose : fusionSlam.getPoses()){
                JsonObject p = new JsonObject();
                p.addProperty("time", currentPose.getTime());
                p.addProperty("x", currentPose.getX());
                p.addProperty("y", currentPose.getY());
                p.addProperty("yaw", currentPose.getYaw());
                prevPoses.add(p);
            }
            outputData.add("prevPoses", prevPoses);
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            String jsonOutput = gson.toJson(outputData);
            try (FileWriter writer = new FileWriter(folder + "/output_file.json")) {
                writer.write(jsonOutput);
            } catch (IOException e) {
                System.out.println(e.getMessage());
            }
            sendBroadcast(new TerminatedBroadcast(getName()));
            terminate();
        });
    }
    private JsonObject getStatistics(){
        JsonObject outputData = new JsonObject();
        outputData.addProperty("systemRuntime", StatisticalFolder.getInstance().getSystemRuntime());
        outputData.addProperty("numDetectedObjects", StatisticalFolder.getInstance().getNumDetectedObjects());
        outputData.addProperty("numTrackedObjects", StatisticalFolder.getInstance().getNumTrackedObjects());
        outputData.addProperty("numLandmarks", StatisticalFolder.getInstance().getNumLandmarks());
        return outputData;
    }

    private void outputProperJson(){
        JsonObject outputData = getStatistics();
        JsonArray landmarks = new JsonArray();
        for (LandMark landMark : FusionSlam.getInstance().getLandMarks()){
            JsonArray coordinates = new JsonArray();
            for (CloudPoint cloudPoint : landMark.getCoordinates()) {
                JsonObject point = new JsonObject();
                point.addProperty("x", cloudPoint.getX());
                point.addProperty("y", cloudPoint.getY());
                coordinates.add(point);
            }
            JsonObject landmark = new JsonObject();
            landmark.addProperty("id", landMark.getId());
            landmark.addProperty("description", landMark.getDescription());
            landmark.add("coordinates", coordinates);
            landmarks.add(landmark);
        }
        outputData.add("landMarks", landmarks);
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String jsonOutput = gson.toJson(outputData);
        try (FileWriter writer = new FileWriter(folder + "/output_file.json")) {
            writer.write(jsonOutput);
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }
}