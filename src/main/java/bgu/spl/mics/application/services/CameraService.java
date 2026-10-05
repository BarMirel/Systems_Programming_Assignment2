package bgu.spl.mics.application.services;

import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.CrashedBroadcast;
import bgu.spl.mics.application.messages.DetectObjectsEvent;
import bgu.spl.mics.application.messages.TerminatedBroadcast;
import bgu.spl.mics.application.messages.TickBroadcast;
import bgu.spl.mics.application.objects.*;

import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

/**
 * CameraService is responsible for processing data from the camera and
 * sending DetectObjectsEvents to LiDAR workers.
 * 
 * This service interacts with the Camera object to detect objects and updates
 * the system's StatisticalFolder upon sending its observations.
 */
public class CameraService extends MicroService {

    private final Camera camera;
    private ConcurrentHashMap<StampedDetectedObjects, Integer> countDown;
    private boolean lastDetected;
    /**
     * Constructor for CameraService.
     *
     * @param camera The Camera object that this service will use to detect objects.
     */
    public CameraService(Camera camera) {
        super("Camera" + camera.getId());
        this.camera = camera;
        this.countDown = new ConcurrentHashMap<>();
        this.lastDetected = false;
    }

    /**
     * Initializes the CameraService.
     * Registers the service to handle TickBroadcasts and sets up callbacks for sending
     * DetectObjectsEvents.
     */
    @Override
    protected void initialize() {
        subscribeBroadcast(TerminatedBroadcast.class, broadcast -> {
            if (broadcast.getSenderName().equals("FusionSlam")) terminate();
        });
        subscribeBroadcast(CrashedBroadcast.class, broadcast -> terminate());
        subscribeBroadcast(TickBroadcast.class, broadcast ->{
            for(StampedDetectedObjects sdo : camera.getDetectedObjectsList()) {
                if (sdo.getTime() == broadcast.getCurrentTick()) {
                    boolean foundError = false;
                    if(sdo == camera.getDetectedObjectsList().get(camera.getDetectedObjectsList().size() - 1))
                        lastDetected = true;
                    for (DetectedObject d : sdo.getDetectedObjects())
                        if (d.getId().equals("ERROR")){
                            foundError = true;
                            sendBroadcast(new CrashedBroadcast(getName(), d.getDescription()));
                            terminate();
                        }
                    if(!foundError) {
                        countDown.put(sdo, camera.getFrequency() + 1);
                        LastFrames.getInstance().updateLastDetectedObjects(getName(), sdo);
                    }
                }
            }
            countDown.forEach((key, value)->{
                if(key != null){
                    value--;
                    countDown.put(key, value);
                    if(value == 0){
                        sendEvent(new DetectObjectsEvent(key));
                        for (int i = 0; i < key.getDetectedObjects().size(); i++)
                            StatisticalFolder.getInstance().incrementNumDetectedObjects();
                        countDown.remove(key);
                    }
                }
            });
            if(lastDetected && countDown.isEmpty()){
                sendBroadcast(new TerminatedBroadcast(getName()));
                terminate();
            }
        });
    }
}
