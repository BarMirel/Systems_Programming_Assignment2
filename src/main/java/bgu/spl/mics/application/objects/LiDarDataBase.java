package bgu.spl.mics.application.objects;

import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import java.io.FileReader;
import java.lang.reflect.Type;

/**
 * LiDarDataBase is a singleton class responsible for managing LiDAR data.
 * It provides access to cloud point data and other relevant information for tracked objects.
 */
public class LiDarDataBase {
    private static volatile LiDarDataBase instance;
    private ArrayList<StampedCloudPoints> cloudPoints;

    private LiDarDataBase(String filePath){
        this.cloudPoints = new ArrayList<>();
        Gson gson = new Gson();
        FileReader reader = null;
        try {
            reader = new FileReader(filePath);
        }catch (IOException e){System.out.println(e.getMessage());}
        Type listType = new TypeToken<List<JsonObject>>(){}.getType();
        List<JsonObject> jsonObjects = gson.fromJson(reader, listType);
        for (JsonObject jsonObject : jsonObjects) {
            String id = jsonObject.get("id").getAsString();
            int time = jsonObject.get("time").getAsInt();
            ArrayList<CloudPoint> cloudPoints = new ArrayList<>();
            for (JsonElement point : jsonObject.getAsJsonArray("cloudPoints")) {
                double x = point.getAsJsonArray().get(0).getAsDouble();
                double y = point.getAsJsonArray().get(1).getAsDouble();
                cloudPoints.add(new CloudPoint(x, y));
            }
            StampedCloudPoints stampedCloudPoints = new StampedCloudPoints(id, time, cloudPoints);
            this.cloudPoints.add(stampedCloudPoints);
        }
        try {
            reader.close();
        }catch (IOException e){System.out.println(e.getMessage());}
    }
    /**
     * Returns the singleton instance of LiDarDataBase.
     *
     * @param filePath The path to the LiDAR data file.
     * @return The singleton instance of LiDarDataBase.
     */
    public static LiDarDataBase getInstance(String filePath) {
        if (instance == null) {
            synchronized (LiDarDataBase.class) {
                if (instance == null) {
                    instance = new LiDarDataBase(filePath);
                }
            }
        }
        return instance;
    }

    public ArrayList<StampedCloudPoints> getCloudPoints() {
        return cloudPoints;
    }
}
