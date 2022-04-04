import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

//concurrent linked list

class p1{
    public static AtomicBoolean occupied = new AtomicBoolean(false);
    public static AtomicBoolean cupcake = new AtomicBoolean(true);
    public static ArrayList<Boolean> current = new ArrayList<Boolean>();
    public static AtomicInteger count= new AtomicInteger(0);
    public static AtomicInteger idx= new AtomicInteger(0);
    public static void main(String[] args)
    {
        Runnable servant = ()->{
            //while presents are available
            while(true)
            {
                //add present to concurrent ll

                //remove present from concurrent ll

                //scan if present was in concurrent ll
            }
        };

        ArrayList<Thread> threadList = new ArrayList<Thread>();
        int N=4;
        int presents = 500000;
        long start = System.nanoTime();

        for(int i=0;i<N;i++)
        {
            threadList.add(new Thread(servant));
            current.add(false);
            threadList.get(i).start();          
        }

        long end = System.nanoTime();

        if(count.get()==presents)
        {
            System.out.println("All guests have visited");
            System.out.println("The servants took "+String.valueOf((end-start)/1000000000)+" seconds to make thank you cards");
        }
    }
}