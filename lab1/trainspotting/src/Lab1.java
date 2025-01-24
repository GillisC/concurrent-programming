import TSim.*;
import java.awt.Point;
import java.util.HashMap;
import java.util.concurrent.Semaphore;

public class Lab1 {

  public Lab1(int speed1, int speed2) {

    // Create two instances of train controller
    HashMap<Point,Semaphore> semaphores = new HashMap<>();
    // Add 5 semaphores, each representing a critical section of the rail
    Semaphore intersection1 = new Semaphore(1);
    semaphores.put(new Point(6, 7),intersection1);
    semaphores.put(new Point(10, 7),intersection1);
    semaphores.put(new Point(8, 5),intersection1);
    semaphores.put(new Point(10, 8),intersection1);


    TrainBrain brain1 = new TrainBrain(1, speed1, semaphores);
    TrainBrain brain2 = new TrainBrain(2, speed2, semaphores);
    
    // Create two threads which keep track of one train each
    Thread train1 = new Thread(brain1);
    Thread train2 = new Thread(brain2);

    // Start the threads
    train1.start();
    train2.start();
  }

  private class TrainBrain implements Runnable {
    
    private int trainid, initialSpeed;
    private Semaphore intersection1;
    private HashMap<Point, Semaphore> shared_railway;

    public TrainBrain(int trainid, int initialSpeed, HashMap<Point, Semaphore> semaphores ) {
      this.trainid = trainid;
      this.initialSpeed = initialSpeed;
      this.shared_railway = semaphores;
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
            Semaphore semaphore = shared_railway.get(new Point(event.getXpos(), event.getYpos()));
            if(!semaphore.tryAcquire()){
              //Nothing
              tsi.setSpeed(trainid, 0);
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
}



