package bgu.spl.mics.application.objects;


import java.util.ArrayList;


/**
 * Represents the robot's GPS and IMU system.
 * Provides information about the robot's position and movement.
 */
public class GPSIMU {
    private int currentTick;
    private STATUS status;
    private final ArrayList<Pose> poseArrayList;


    public GPSIMU(int currentTick, STATUS status, ArrayList<Pose> poseArrayList){
        this.currentTick = currentTick;
        this.status = status;
        this.poseArrayList = poseArrayList;
    }
    public int getCurrentTick(){
        return currentTick;
    }
    public STATUS getStatus(){
        return status;
    }
    public void setCurrentTick(int currentTick) {
        this.currentTick = currentTick;
    }
    public void setStatus(STATUS status) {
        this.status = status;
    }
    public ArrayList<Pose> getPoseArrayList() {
        return poseArrayList;
    }
    public Pose getcurrentPose(){
        int index = 0;
        for (int i = 0; i < poseArrayList.size() ; i++)
            if(poseArrayList.get(i).getTime() == currentTick) {
                index = i;
                break;
            }
        return poseArrayList.get(index);
    }
}