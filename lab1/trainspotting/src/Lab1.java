import TSim.*;

public class Lab1 {

  public Lab1(int speed1, int speed2) {

    // Create two instances of train controller
    TrainBrain brain1 = new TrainBrain(1, speed1);
    TrainBrain brain2 = new TrainBrain(2, speed2);
    
    // Create two threads which keep track of one train each
    Thread train1 = new Thread(brain1);
    Thread train2 = new Thread(brain2);

    // Start the threads
    train1.start();
    train2.start();
  }

  private class TrainBrain implements Runnable {
    
    private int trainid, initialSpeed;

    public TrainBrain(int trainid, int initialSpeed) {
      this.trainid = trainid;
      this.initialSpeed = initialSpeed;
    }


    // Shared run routine for both trains
    @Override
    public void run() {
        TSimInterface tsi = TSimInterface.getInstance();

        try {
          tsi.setSpeed(trainid, initialSpeed);
          tsi.setSpeed(trainid, 0);
          
          tsi.getSensor(1);
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



