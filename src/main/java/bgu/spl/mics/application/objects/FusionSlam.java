    package bgu.spl.mics.application.objects;

    import bgu.spl.mics.MessageBusImpl;

    import java.util.ArrayList;

    /**
     * Manages the fusion of sensor data for simultaneous localization and mapping (SLAM).
     * Combines data from multiple sensors (e.g., LiDAR, camera) to build and update a global map.
     * Implements the Singleton pattern to ensure a single instance of FusionSlam exists.
     */
    public class FusionSlam {

        private ArrayList<LandMark> landMarks;
        private ArrayList<Pose> poses;
        private FusionSlam() {
            this.landMarks = new ArrayList<>();
            this.poses = new ArrayList<>();
        }
        // Singleton instance holder
        private static class FusionSlamHolder {
            private static volatile FusionSlam instance;
        }

        public static FusionSlam getInstance() {
            if (FusionSlamHolder.instance == null) {
                synchronized (MessageBusImpl.class) {
                    if (FusionSlamHolder.instance == null) {
                        FusionSlamHolder.instance = new FusionSlam();
                    }
                }
            }
            return FusionSlamHolder.instance;
        }

        public ArrayList<LandMark> getLandMarks() {
            return landMarks;
        }

        public ArrayList<Pose> getPoses() {
            return poses;
        }
        public void AddOrSetLandmark(TrackedObject t, boolean newLandMark, LandMark landMark, Pose currPose){
            if(!newLandMark){
                for (int j = 0; j < t.getCloudPoints().size(); j++) {
                    CloudPoint localPoint = t.getCloudPoints().get(j);
                    double localX = localPoint.getX();
                    double localY = localPoint.getY();
                    double yawInRadians = Math.toRadians(currPose.getYaw());
                    double newX = currPose.getX() + (localX * Math.cos(yawInRadians) - localY * Math.sin(yawInRadians));
                    double newY = currPose.getY() + (localX * Math.sin(yawInRadians) + localY * Math.cos(yawInRadians));
                    if (j < landMark.getCoordinates().size() && landMark.getCoordinates() != null) {
                        newX = (newX + landMark.getCoordinates().get(j).getX()) / 2;
                        newY = (newY + landMark.getCoordinates().get(j).getY()) / 2;
                        landMark.getCoordinates().set(j, new CloudPoint(newX, newY));
                    } else {
                        landMark.getCoordinates().add(new CloudPoint(newX, newY));
                    }
                }
            }
            else {
                LandMark landmark = new LandMark(t.getId(), t.getDescription());
                for (int j = 0; j < t.getCloudPoints().size(); j++) {
                    CloudPoint localPoint = t.getCloudPoints().get(j);
                    double localX = localPoint.getX();
                    double localY = localPoint.getY();
                    double yawInRadians = Math.toRadians(currPose.getYaw());
                    double newX = currPose.getX() + (localX * Math.cos(yawInRadians) - localY * Math.sin(yawInRadians));
                    double newY = currPose.getY() + (localX * Math.sin(yawInRadians) + localY * Math.cos(yawInRadians));
                    landmark.getCoordinates().add(new CloudPoint(newX, newY));
                }
                getLandMarks().add(landmark);
                StatisticalFolder.getInstance().incrementNumLandmarks();
            }
        }
    }