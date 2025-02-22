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
    
    // Note that direction has been choosen so that positive is a train that is moving upwards and negative that is traveling downwards
    private int trainid, currentSpeed, direction;
    private SensorManager manager;
    
    public TrainBrain(int trainid, int otherTrainId, int initialSpeed, int direction, SensorManager manager ) {
      this.trainid = trainid;
      this.currentSpeed = initialSpeed;
      this.direction = direction;
      this.manager = manager;
    }

    private void turnAround() {
      TSimInterface tsi = TSimInterface.getInstance();
      System.out.println("Stopping train " + trainid);
      try {
        tsi.setSpeed(this.trainid, 0);
        Thread.sleep(1000 + (20 * Math.abs(this.currentSpeed)));
        this.direction *= -1; // Reverse direction
        tsi.setSpeed(this.trainid, this.currentSpeed * -1);
      } catch (CommandException e) {
        e.printStackTrace();

      } catch (InterruptedException e) {
        e.printStackTrace();
      }
      this.currentSpeed *= -1;
    }


    // Shared run routine for both trains
    @Override
    public void run() {
        TSimInterface tsi = TSimInterface.getInstance();

        try {
          tsi.setSpeed(trainid, currentSpeed);
          
          while (true) { 
            
            SensorEvent event = tsi.getSensor(trainid);
            int sensorX = event.getXpos();
            int sensorY = event.getYpos();
            ExtendedSemaphore semaphore = manager.getSemaphore(sensorX, sensorY);
      
            if (event.getStatus() == SensorEvent.ACTIVE) {

              if(!semaphore.tryAcquire(trainid, direction, sensorX, sensorY) && semaphore.getHolder() != trainid) {
                tsi.setSpeed(trainid, 0);
                // This will pause the thread until a permit can be aquired
                semaphore.acquire(trainid, direction, sensorX, sensorY);
                tsi.setSpeed(trainid, currentSpeed);
              }               
            }

            else if (event.getStatus() == SensorEvent.INACTIVE) {
              // Leaving intersection 1
              if (semaphore.getHolder() == trainid && trainid == 1 && direction == -1 && sensorX == 10 && sensorY == 7) {
                semaphore.release();
              }              
              else if (semaphore.getHolder() == trainid && trainid == 2 && direction == -1 && sensorX == 10 && sensorY == 8) {
                semaphore.release();
              }
              else if (semaphore.getHolder() == trainid && trainid == 2 && direction == 1 && sensorX == 8 && sensorY == 5) {
                semaphore.release();
                turnAround();
              }
              else if (semaphore.getHolder() == trainid && trainid == 1 && direction == 1 && sensorX == 6 && sensorY == 7) {
                semaphore.release();
                // The train should stop here and turn around
                turnAround();
              }
            
              // Leaving intersection 2
              else if (trainid == 2 && direction == 1 && sensorX == 15 && sensorY == 8) {
                semaphore.release();
              }
              else if (trainid == 1 && direction == 1 && sensorX == 15 && sensorY == 7) {
                semaphore.release();
              }
              else if (direction == -1 && sensorX == 13 && (sensorY == 9 || sensorY == 10)) {
                semaphore.release();
              }
              
              // Overtake lane
              else if (direction == 1 && sensorX == 17 && sensorY == 9) {
                semaphore.release();
              }
              else if (direction == -1 && sensorX == 2 && sensorY == 9) {
                semaphore.release();
              }
              // Intersection 4
              else if (direction == 1 && sensorX == 6 && (sensorY == 9 || sensorY == 10)) {
                semaphore.release();
              }
              else if (direction == -1 && sensorX == 3 && sensorY == 13) {
                semaphore.release();
                turnAround();
              }
              else if (trainid == 2 && direction == -1 && sensorX == 5 && sensorY == 11) {
                semaphore.release();  
                turnAround();
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
    ExtendedSemaphore semaphore2 = new ExtendedSemaphore(1); 
    ExtendedSemaphore semaphore3 = new ExtendedSemaphore(1); 
    ExtendedSemaphore semaphore4 = new ExtendedSemaphore(1); 
    
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
        // Intersection 2
        case "15,7":
          return semaphore2;
        case "15,8":
          return semaphore2;
        case "13,9":
          return semaphore2;
        case "13,10":
          return semaphore2;

        // Omkörnings sektion
        case "17,9":
          return semaphore3;
        case "2,9":
          return semaphore3;
        // Section 4
        case "6,9":
          return semaphore4;
        case "6,10":
          return semaphore4;
        case "3,13":
          return semaphore4;
        case "5,11":
          return semaphore4;

        default:
          return null; 
        }
    }
  }
  
  private class ExtendedSemaphore extends Semaphore {

    private volatile int trainHolding = 0; // The id of the train holding the permit

    public ExtendedSemaphore(int permits) {
      super(permits);
    }

    
    public void acquire(int trainId, int direction, int sensorX, int sensorY) {
      try {
        super.acquire();
        trainHolding = trainId;
        alterTrack(trainId, direction, sensorX, sensorY);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }

    
    public boolean tryAcquire(int trainId, int direction, int sensorX, int sensorY) {
      TSimInterface tsi = TSimInterface.getInstance();

      System.out.println("Train " + trainId + " is trying to get permit, direction: " + direction);

      if(super.tryAcquire()) {
        System.out.println("Train " + trainId + " acquired the permit");
        trainHolding = trainId;
        alterTrack(trainId, direction, sensorX, sensorY);
        return true;
      } 
      else if (sensorX == 17 && sensorY == 9 && direction == -1) { 
        System.out.println("Train: " + trainId + " traveling: " + direction);
        try {
          tsi.setSwitch(15, 9, TSimInterface.SWITCH_LEFT);

        } catch (CommandException e) {
          e.printStackTrace();
        }
        return true;
      }
      else if (sensorX == 17 && sensorY == 9 && direction == 1) {
        return true;
      }
      else if (sensorX == 2 && sensorY == 9 && direction == 1) { 
        try {
          tsi.setSwitch(4, 9, TSimInterface.SWITCH_RIGHT);

        } catch (CommandException e) {
          e.printStackTrace();
        }
        return true;
      }
      else if (sensorX == 2 && sensorY == 9 && direction == -1) {
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

    private void alterTrack(int trainId, int direction, int sensorX, int sensorY) {
      TSimInterface tsi = TSimInterface.getInstance();
      // This method is called after a train acquires a permit to make alterations to the track
      try {  
        // Intersection 1 requires no alterations after acquiring a permit

        // Intersection 2
        if (trainId == 2 && sensorX == 19 && sensorY == 7 && direction == 1) {
          tsi.setSwitch(17, 7, TSimInterface.SWITCH_LEFT);
        }
        else if (trainId == 2 && sensorX == 15 && sensorY == 8 && direction == -1) {
          tsi.setSwitch(17, 7, TSimInterface.SWITCH_LEFT);
          tsi.setSwitch(15, 9, TSimInterface.SWITCH_RIGHT);
        }
        else if (trainId == 1 && sensorX == 15 && sensorY == 7 && direction == -1) {
          tsi.setSwitch(17, 7, TSimInterface.SWITCH_RIGHT);
          tsi.setSwitch(15, 9, TSimInterface.SWITCH_RIGHT);
        }
        // Omkörningsväg
        else if (sensorX == 13 && (sensorY == 9 || sensorY == 10) && direction == 1) {
          if (trainId == 1) {
            if (sensorY == 9) {
              tsi.setSwitch(15, 9, TSimInterface.SWITCH_RIGHT);
            } else {
              tsi.setSwitch(15, 9, TSimInterface.SWITCH_LEFT);
            }
            tsi.setSwitch(17, 7, TSimInterface.SWITCH_RIGHT);
            
          } else if (trainId == 2) {
            if (sensorY == 9) {
              tsi.setSwitch(15, 9, TSimInterface.SWITCH_RIGHT);
            } else {
              tsi.setSwitch(15, 9, TSimInterface.SWITCH_LEFT);
            }
            tsi.setSwitch(17, 7, TSimInterface.SWITCH_LEFT);
          }
        }

        // Intersection 3
        else if (trainId == 1 && sensorX == 6 && (sensorY == 9 || sensorY == 10) && direction == -1) {
          if (sensorY == 9) {
            tsi.setSwitch(4, 9, TSimInterface.SWITCH_LEFT);
          }
          else {
            tsi.setSwitch(4, 9, TSimInterface.SWITCH_RIGHT);
          }
          tsi.setSwitch(3, 11, TSimInterface.SWITCH_RIGHT);
        }
        else if (trainId == 2 && sensorX == 6 && (sensorY == 9 || sensorY == 10) && direction == -1) {
          if (sensorY == 9) {
            tsi.setSwitch(4, 9, TSimInterface.SWITCH_LEFT);
          }
          else {
            tsi.setSwitch(4, 9, TSimInterface.SWITCH_RIGHT);
          }
          tsi.setSwitch(3, 11, TSimInterface.SWITCH_LEFT);
        }
        else if (trainId == 1 && sensorX == 3 && sensorY == 13 && direction == 1) {
          tsi.setSwitch(3, 11, TSimInterface.SWITCH_RIGHT);
          tsi.setSwitch(4, 9, TSimInterface.SWITCH_LEFT);
        }
        else if (trainId == 2 && sensorX == 5 && sensorY == 11 && direction == 1) {
          tsi.setSwitch(3, 11, TSimInterface.SWITCH_LEFT);
          tsi.setSwitch(4, 9, TSimInterface.SWITCH_LEFT);
        }

      } 
      catch (CommandException e) {
        e.printStackTrace();
      }
       
    }
  }
}



