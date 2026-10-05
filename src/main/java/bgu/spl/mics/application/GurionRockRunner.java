package bgu.spl.mics.application;

import bgu.spl.mics.application.objects.*;
import bgu.spl.mics.application.services.*;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonArray;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * The main entry point for the GurionRock Pro Max Ultra Over 9000 simulation.
 * <p>
 * This class initializes the system and starts the simulation by setting up
 * services, objects, and configurations.
 * </p>
 */
public class GurionRockRunner {

    /**
     * The main method of the simulation.
     * This method sets up the necessary components, parses configuration files,
     * initializes services, and starts the simulation.
     *
     * @param args Command-line arguments. The first argument is expected to be the path to the configuration file.
     */
    public static void main(String[] args) {
        if (args.length != 1) throw new RuntimeException("Argument incorrect");
        else {
            String filePath = args[0];
            Path configPath = Paths.get(args[0]);
            String folder = configPath.getParent().toString();
            try (FileReader reader = new FileReader(filePath)) {
                Gson gson = new Gson();
                JsonObject jsonObject = gson.fromJson(reader, JsonObject.class);
                //Cameras Creation
                String cameraDataPath = folder + jsonObject.getAsJsonObject("Cameras").get("camera_datas_path").getAsString().substring(1);
                ArrayList<Camera> cameras = new ArrayList<>();
                HashMap<String, String> idToDescription = new HashMap<>();
                JsonArray camerasArray = jsonObject.getAsJsonObject("Cameras").getAsJsonArray("CamerasConfigurations");
                for (int i = 0; i < camerasArray.size(); i++) {
                    JsonObject cameraObj = camerasArray.get(i).getAsJsonObject();
                    int id = cameraObj.get("id").getAsInt();
                    int frequency = cameraObj.get("frequency").getAsInt();
                    String cameraKey = cameraObj.get("camera_key").getAsString();
                    ArrayList<StampedDetectedObjects> stampedDetectedObjects = parseCameraData(cameraDataPath, cameraKey);
                    for (StampedDetectedObjects s : stampedDetectedObjects)
                        for (DetectedObject d : s.getDetectedObjects())
                            idToDescription.putIfAbsent(d.getId(), d.getDescription());
                    cameras.add(new Camera(id, frequency, STATUS.UP, stampedDetectedObjects));
                }
                //LiDars Creation
                String lidarDataPath = folder + jsonObject.getAsJsonObject("LiDarWorkers").get("lidars_data_path").getAsString().substring(1);
                LiDarDataBase liDarDataBase = LiDarDataBase.getInstance(lidarDataPath);
                ArrayList<TrackedObject> trackedObjects = new ArrayList<>();
                for (StampedCloudPoints s : liDarDataBase.getCloudPoints())
                    trackedObjects.add(new TrackedObject(s.getId(), s.getTime(), idToDescription.get(s.getId()), s.getCloudPoints()));
                ArrayList<LiDarWorkerTracker> lidars = new ArrayList<>();
                JsonArray lidarsArray = jsonObject.getAsJsonObject("LiDarWorkers").getAsJsonArray("LidarConfigurations");
                for (int i = 0; i < lidarsArray.size(); i++) {
                    JsonObject lidarObj = lidarsArray.get(i).getAsJsonObject();
                    int id = lidarObj.get("id").getAsInt();
                    int frequency = lidarObj.get("frequency").getAsInt();
                    lidars.add(new LiDarWorkerTracker(id, frequency, STATUS.UP, trackedObjects));
                }
                //GPSIMU Creation
                String posesDataPath = folder + jsonObject.get("poseJsonFile").getAsString().substring(1);
                ArrayList<Pose> poses = getPosesData(posesDataPath);
                GPSIMU gpsimu = new GPSIMU(0, STATUS.UP, poses);
                //FusionSlam Creation
                FusionSlam fusionSlam = FusionSlam.getInstance();

                //MicroServices Creation
                ArrayList<Thread> cameraServices = new ArrayList<>();
                for (Camera c : cameras)
                    cameraServices.add(new Thread(new CameraService(c)));
                ArrayList<Thread> liDarServices = new ArrayList<>();
                for (LiDarWorkerTracker l : lidars)
                    liDarServices.add(new Thread(new LiDarService(l)));
                Thread poseService = new Thread(new PoseService(gpsimu));
                Thread fusionSlamService = new Thread(new FusionSlamService(fusionSlam, cameraServices.size() + liDarServices.size() + 1, folder));
                int tickTime = jsonObject.get("TickTime").getAsInt();
                int duration = jsonObject.get("Duration").getAsInt();
                Thread timeService = new Thread(new TimeService(tickTime, duration));
                //Starting the Simulation
                for (Thread t : cameraServices) t.start();
                for (Thread t : liDarServices) t.start();
                poseService.start();
                fusionSlamService.start();
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ignored) {
                }
                timeService.start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static ArrayList<StampedDetectedObjects> parseCameraData(String path, String cameraKey){
        try (FileReader reader = new FileReader(path)) {
            Gson gson = new Gson();
            JsonObject jsonObject = gson.fromJson(reader, JsonObject.class);
            ArrayList<StampedDetectedObjects> stampedDetectedObjects = new ArrayList<>();
            JsonArray camera = jsonObject.getAsJsonArray(cameraKey);
            for (int i = 0; i < camera.size(); i++) {
                JsonObject stampedDetectedObj = camera.get(i).getAsJsonObject();
                int time = stampedDetectedObj.get("time").getAsInt();
                ArrayList<DetectedObject> detectedObjects = new ArrayList<>();
                JsonArray detectedObj = stampedDetectedObj.getAsJsonArray("detectedObjects");
                for (int j = 0; j < detectedObj.size(); j++){
                    JsonObject decObj = detectedObj.get(j).getAsJsonObject();
                    String id = decObj.get("id").getAsString();
                    String description = decObj.get("description").getAsString();
                    detectedObjects.add(new DetectedObject(id, description));
                }
                stampedDetectedObjects.add(new StampedDetectedObjects(time, detectedObjects));
            }
            return stampedDetectedObjects;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static ArrayList<Pose> getPosesData(String path){
        try (FileReader reader = new FileReader(path)) {
            Gson gson = new Gson();
            JsonArray jsonArray = gson.fromJson(reader, JsonArray.class);
            ArrayList<Pose> poses = new ArrayList<>();
            for (int i = 0; i < jsonArray.size(); i++) {
                JsonObject pose = jsonArray.get(i).getAsJsonObject();
                int time = pose.get("time").getAsInt();
                float x = pose.get("x").getAsFloat();
                float y = pose.get("y").getAsFloat();
                float yaw = pose.get("yaw").getAsFloat();
                poses.add(new Pose(x, y, yaw, time));
            }
            return poses;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
