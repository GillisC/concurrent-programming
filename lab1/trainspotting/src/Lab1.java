import TSim.*;
import java.awt.Point;
import java.util.HashMap;
import java.util.concurrent.Semaphore;

public class Lab1 {

  public Lab1(int speed1, int speed2) {

    SensorManager manager = new SensorManager();

    TrainBrain brain1 = new TrainBrain(1, 2, speed1, -1, manager);
    TrainBrain brain2 = new TrainBrain(2, 1, speed2, 1, manager);
    
    // Create two threads which keep track of one train each
    Thread train1 = new Thread(brain1);
    Thread train2 = new Thread(brain2);

    // Start the threads
    train1.start();
    train2.start();
  }

  private class TrainBrain implements Runnable {
    
    private int trainid, otherTrainId, initialSpeed, direction;
    private SensorManager manager;
    
    public TrainBrain(int trainid, int otherTrainId, int initialSpeed, int direction, SensorManager manager ) {
      this.trainid = trainid;
      this.otherTrainId = otherTrainId;
      this.initialSpeed = initialSpeed;
      this.direction = direction;
      this.manager = manager;
    }


    // Shared run routine for both trains
    @Override
    public void run() {
        TSimInterface tsi = TSimInterface.getInstance();

        try {
          tsi.setSpeed(trainid, initialSpeed);
          tsi.setSwitch(15, 9, TSimInterface.SWITCH_RIGHT);
          while (true) { 
            
            SensorEvent event = tsi.getSensor(trainid);
      
            if (event.getStatus() == SensorEvent.ACTIVE) {

              ExtendedSemaphore semaphore = manager.getSemaphore(event.getXpos(), event.getYpos());
              if(!semaphore.tryAcquire(trainid) && semaphore.getHolder() != trainid) {
                System.out.println("Stopping train " + trainid);
                tsi.setSpeed(trainid, 0);
                tsi.getSensor(otherTrainId);
                tsi.setSpeed(trainid, initialSpeed);
              }
                        
            }

            else if (event.getStatus() == SensorEvent.INACTIVE) {
              ExtendedSemaphore semaphore = manager.getSemaphore(event.getXpos(), event.getYpos());
              if (semaphore.getHolder() == trainid && trainid == 1 && direction == -1 && event.getXpos() == 10 && event.getYpos() == 7) {
                semaphore.release();
              }
            }
          }  
        }

        catch (CommandException e) {
          e.printStackTrace();
        }
        catch (InterruptedException e) {
          e.printStackTrace();
        }
    }
  }

  private class SensorManager {
    
    ExtendedSemaphore semaphore1 = new ExtendedSemaphore(1); 
    
    // Given a coordinate of a sensor returns a semaphore that is linked to it
    public ExtendedSemaphore getSemaphore(int x, int y) {
      
      switch(x + "," + y) {
        // The first intersection
        case "6,7":
          return semaphore1;
        case "8,5":
          return semaphore1;
        case "10,8":
          return semaphore1;
        case "10,7":
          return semaphore1; 
        default:
          return null; 
      }
    }
  }
  
  private class ExtendedSemaphore extends Semaphore{

    private volatile int trainHolding = 0; // The id of the train holding the permit

    public ExtendedSemaphore(int permits) {
      super(permits);
    }

    @Override
    public boolean tryAcquire(int trainId) {
      System.out.println("Train " + trainId + " is trying to get permit");
      if(super.tryAcquire()) {
        System.out.println("Train " + trainId + " acquired the permit");
        trainHolding = trainId;
        return true;
      }
      return false;
    }

    @Override
    public void release() {
      System.out.println("Train " + trainHolding + " releasing permit");
      super.release();
      trainHolding = 0;
    }

    public int getHolder() {
      return trainHolding;
    }
  }
}



