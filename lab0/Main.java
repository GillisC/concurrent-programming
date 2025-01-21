package lab0;

public class Main {

    public static void main(String[] args) {
        Counter u = new Counter();
        Counter v = new Counter();

        Thread thread1 = new Thread(u);
        Thread thread2 = new Thread(v);
        
        thread1.start();
        thread2.start();

        System.out.println(Counter.count);
    }
    
}

