    package bgu.spl.mics.application.messages;

    import bgu.spl.mics.Event;
    import bgu.spl.mics.application.objects.DetectedObject;
    import bgu.spl.mics.application.objects.StampedDetectedObjects;

    import java.util.ArrayList;

    public class DetectObjectsEvent implements Event {
        private final StampedDetectedObjects detectedObjects;

        public DetectObjectsEvent(StampedDetectedObjects detectedObjects){
            this.detectedObjects = detectedObjects;
        }

        public StampedDetectedObjects getDetectedObjects() {
            return detectedObjects;
        }
    }
