package bgu.spl.mics.application.services;

import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.CrashedBroadcast;
import bgu.spl.mics.application.messages.PoseEvent;
import bgu.spl.mics.application.messages.TerminatedBroadcast;
import bgu.spl.mics.application.messages.TickBroadcast;
import bgu.spl.mics.application.objects.GPSIMU;
import bgu.spl.mics.application.objects.Pose;

/**
 * PoseService is responsible for maintaining the robot's current pose (position and orientation)
 * and broadcasting PoseEvents at every tick.
 */
public class PoseService extends MicroService {

    private final GPSIMU gpsimu;

    /**
     * Constructor for PoseService.
     *
     * @param gpsimu The GPSIMU object that provides the robot's pose data.
     */
    public PoseService(GPSIMU gpsimu) {
        super("GPSIMU");
        this.gpsimu = gpsimu;
    }

    /**
     * Initializes the PoseService.
     * Subscribes to TickBroadcast and sends PoseEvents at every tick based on the current pose.
     */
    @Override
    protected void initialize() {
        subscribeBroadcast(CrashedBroadcast.class, broadcast -> terminate());
        subscribeBroadcast(TerminatedBroadcast.class, broadcast -> {
            if (broadcast.getSenderName().equals("FusionSlam")) terminate();
        });
        subscribeBroadcast(TickBroadcast.class, broadcast -> {
            gpsimu.setCurrentTick(broadcast.getCurrentTick());
            sendEvent(new PoseEvent(gpsimu.getcurrentPose()));
            if (gpsimu.getCurrentTick() == gpsimu.getPoseArrayList().size()){
                sendBroadcast(new TerminatedBroadcast(getName()));
                terminate();
            }
        });
    }
}
