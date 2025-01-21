package lab0;

public class Counter implements Runnable {

    public static int count = 0;


    @Override
    public void run() {
        count++;
    }
    
}
