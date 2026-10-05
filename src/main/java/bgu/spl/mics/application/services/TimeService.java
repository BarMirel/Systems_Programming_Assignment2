package bgu.spl.mics.application.services;


import bgu.spl.mics.Broadcast;
import bgu.spl.mics.MessageBusImpl;
import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.CrashedBroadcast;
import bgu.spl.mics.application.messages.TerminatedBroadcast;
import bgu.spl.mics.application.messages.TickBroadcast;
import bgu.spl.mics.application.objects.StatisticalFolder;


/**
 * TimeService acts as the global timer for the system, broadcasting TickBroadcast messages
 * at regular intervals and controlling the simulation's duration.
 */
public class TimeService extends MicroService {
    private final int tickTime;
    private final int duration;
    private int currentTick;


    /**
     * Constructor for TimeService.
     *
     * @param TickTime  The duration of each tick in milliseconds.
     * @param Duration  The total number of ticks before the service terminates.
     */
    public TimeService(int TickTime, int Duration) {
        super("TimeService");
        this.tickTime = TickTime*1000;
        this.duration = Duration;
        this.currentTick = 1;
    }


    /**
     * Initializes the TimeService.
     * Starts broadcasting TickBroadcast messages and terminates after the specified duration.
     */
    @Override
    protected void initialize() {
        subscribeBroadcast(CrashedBroadcast.class, broadcast -> terminate());
        subscribeBroadcast(TerminatedBroadcast.class, broadcast -> {
            if(broadcast.getSenderName().equals("FusionSlam"))
                terminate();
        });
        subscribeBroadcast(TickBroadcast.class, broadcast -> {
            if(currentTick <= duration){
                try {
                    Thread.sleep(tickTime);
                    sendBroadcast(new TickBroadcast(currentTick));
                    StatisticalFolder.getInstance().incrementSystemRuntime();
                    currentTick++;
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
            else {
                sendBroadcast(new TerminatedBroadcast(this.getName()));
                terminate();
            }
        });
        try {
            Thread.sleep(tickTime);
            sendBroadcast(new TickBroadcast(currentTick));
            StatisticalFolder.getInstance().incrementSystemRuntime();
            currentTick++;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}