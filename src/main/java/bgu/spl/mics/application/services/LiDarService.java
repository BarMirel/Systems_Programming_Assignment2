package bgu.spl.mics.application.services;


import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.*;
import bgu.spl.mics.application.objects.*;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
/**
 * LiDarService is responsible for processing data from the LiDAR sensor and
 * sending TrackedObjectsEvents to the FusionSLAM service.
 *
 * This service interacts with the LiDarWorkerTracker object to retrieve and process
 * cloud point data and updates the system's StatisticalFolder upon sending its
 * observations.
 */
public class LiDarService extends MicroService {
    private final LiDarWorkerTracker LiDarWorkerTracker;
    private final ConcurrentHashMap<StampedDetectedObjects,Integer> detectedObject;
    private boolean lastTracked;
    /**
     * Constructor for LiDarService.
     *
     * @param LiDarWorkerTracker A LiDAR Tracker worker object that this service will use to process data.
     */
    public LiDarService(LiDarWorkerTracker LiDarWorkerTracker) {
        super("LiDar" + LiDarWorkerTracker.getId());
        this.LiDarWorkerTracker = LiDarWorkerTracker;
        this.detectedObject = new ConcurrentHashMap<>();
        lastTracked = false;
    }
    /**
     * Initializes the LiDarService.
     * Registers the service to handle DetectObjectsEvents and TickBroadcasts,
     * and sets up the necessary callbacks for processing data.
     */
    @Override
    protected void initialize() {
        subscribeEvent(DetectObjectsEvent.class, event ->{
            detectedObject.put(event.getDetectedObjects(), LiDarWorkerTracker.getFrequency() + 1);
        });
        subscribeBroadcast(TerminatedBroadcast.class, broadcast -> {
            if (broadcast.getSenderName().equals("FusionSlam")) terminate();
        });
        subscribeBroadcast(CrashedBroadcast.class, broadcast -> terminate());
        subscribeBroadcast(TickBroadcast.class, broadcast ->{
            ArrayList<TrackedObject> tracked = new ArrayList<>();
            detectedObject.forEach((key, value)-> {
                boolean foundError = false;
                if (key != null) {
                    value--;
                    detectedObject.put(key, value);
                    if (value == 0) {
                        for (TrackedObject a : LiDarWorkerTracker.getTrackedObject()) {
                            if (a.getTime() == key.getTime()){
                                if (a.getId().equals("ERROR")){
                                    foundError = true;
                                    sendBroadcast(new CrashedBroadcast(getName(), a.getDescription()));
                                    terminate();
                                }
                                for(DetectedObject d : key.getDetectedObjects()){
                                    if(a.getId().equals(d.getId())){
                                        tracked.add(a);
                                        StatisticalFolder.getInstance().incrementNumTrackedObjects();
                                        if (a == LiDarWorkerTracker.getTrackedObject().get(LiDarWorkerTracker.getTrackedObject().size() - 1)) {
                                            lastTracked = true;
                                            break;
                                        }
                                    }
                                }
                            }
                            detectedObject.remove(key);
                        }
                        if(!foundError) {
                            LastFrames.getInstance().updateLastTrackedObjects(getName(), tracked);
                            sendEvent(new TrackedObjectsEvent(tracked, LiDarWorkerTracker.getFrequency()));
                        }
                    }
                }
            });
            if (lastTracked && detectedObject.isEmpty()){
                sendBroadcast(new TerminatedBroadcast(getName()));
                terminate();
            }
        });
    }
}