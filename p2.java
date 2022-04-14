import java.util.ArrayList;
import java.util.LinkedList;
import java.util.PriorityQueue;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

//keep all readings from the past hour in 
//an array and clear when time % 60 == 0. 
//let a minute on mars be .5 seconds on earth.
//your top 5 arrays are priority queues.
//max over 10 minutes interval is the maximum
//10 min temp difference within the hour report.
//For 10 min temp diff, create a 10-min queue with
//max heap and min heap. on each deque, remove 
//deque val from the heaps. record the temp diff
//of these two heaps, and update the time interval
//to max(time of rewrite - 10,0) through 
//max(time of rewrite,10). this guarantees interval 
//of 10

//since there are 4 heaps+1 queue, the heaps
//will have one thread lock and execute crud 
//operation. the queue will be concurrent with
//1 adding thread and one removing thread. update
//class variable then unlock

//main lock will unlock every .5 seconds then lock
//once all operations are done. 


public class p2 {
    public static WaitFreeQueue queue = new WaitFreeQueue(10);
    public static PriorityQueue<Integer> minh = new PriorityQueue<Integer>();
    public static PriorityQueue<Integer> maxh = new PriorityQueue<Integer>((x, y) -> Integer.compare(y, x));
    public static PriorityQueue<Integer> minten = new PriorityQueue<Integer>();
    public static PriorityQueue<Integer> maxten = new PriorityQueue<Integer>((x, y) -> Integer.compare(y, x));
    public static AtomicBoolean sensing = new AtomicBoolean(true);
    public static AtomicBoolean reporting = new AtomicBoolean(false);
    public static AtomicInteger tasks = new AtomicInteger(0);
    public static AtomicInteger tasksDone = new AtomicInteger(0);
    public static int time=0;
    public static int prev=-1000; //init to 'null' val
    public static int curr;
    public static int diff = 0;
    public static int interval=10;

    public static void main(String[] args)
    {
        Runnable reporter = ()->{
            while(true)
            {
                if(reporting.get())
                {
                    //report if time to report
                    long start = System.nanoTime();

                    System.out.print("Top 5 highest of the hour: ");
                    for(int i=0;i<5;i++)
                    {
                        System.out.print(maxh.poll());
                        System.out.print(" ");
                    }
                    System.out.print("\nTop 5 lowest of the hour: ");
                    for(int i=0;i<5;i++)
                    {
                        System.out.print(minh.poll());
                        System.out.print(" ");
                    }
                    System.out.print("\nThe largest temperature difference occured from "+String.valueOf(interval-10)+" to "+String.valueOf(interval)+" minutes. The difference was "+String.valueOf(diff));

                    //empty data
                    diff=0;
                    minh = new PriorityQueue<Integer>();
                    maxh = new PriorityQueue<Integer>((x, y) -> Integer.compare(y, x));
                    minten = new PriorityQueue<Integer>();
                    maxten = new PriorityQueue<Integer>((x, y) -> Integer.compare(y, x));

                    long end = System.nanoTime();
                    System.out.println("\nReport compiled in "+String.valueOf((end-start)/1000000)+" milliseconds\n");
                    reporting.set(false);      
                }
            }
        };

        Runnable sensor = ()->{
            while(true)
            {
                if(!sensing.get() && !reporting.get())
                {
                    while(tasksDone.get()<4)
                    {
                        int task = tasks.getAndIncrement();

                        switch(task)
                        {
                            case 0:
                                //add to maxh
                                maxh.add(curr);
                                tasksDone.getAndIncrement();
                                break;
                            case 1:
                                //add to minh
                                minh.add(curr);
                                tasksDone.getAndIncrement();
                                break;
                            case 2:
                                //add and remove maxten
                                maxten.add(curr);
                                maxten.remove(prev);
                                tasksDone.getAndIncrement();
                                break;
                            case 3:
                                //add and remove minten
                                minten.add(curr);
                                minten.remove(prev);
                                tasksDone.getAndIncrement();
                                break;
                            default:
                                //wait
                                break;
                        }
                    }

                    if(maxten.peek()-minten.peek()>diff)
                    {
                        diff=maxten.peek()-minten.peek();
                        interval = Math.max(time,60*(time/60)+10);
                    }

                    //everything is done, exit
                    sensing.set(true);
                }   
            }
        };

        ArrayList<Thread> threadList = new ArrayList<Thread>();
        int N=8;
        Random random = new Random();

        threadList.add(new Thread(reporter));
        threadList.get(0).start(); 

        for(int i=1;i<N;i++)
        {
            threadList.add(new Thread(sensor));
            threadList.get(i).start();    
            
            //for testing
            // try{
            //     threadList.get(i).join();  
            // }
            // catch(Exception e){
            //     System.out.println(e);
            // }
        }

        while(true)
        {
            //set setting=true every .1 seconds
            try {
                time+=1;
                
                if(time%60==0)
                {
                    System.out.println("Time = "+String.valueOf(time)+" mins");
                    reporting.set(true);
                }

                sensing.set(true);
                curr = random.nextInt(171)-100;
                queue.enq(curr);
                prev = queue.deq();
                tasks.set(0);
                tasksDone.set(0);
                sensing.set(false);
                TimeUnit.MILLISECONDS.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}

class WaitFreeQueue 
{
    private int head;
    private int tail; 
    private int capacity;
    private int[] items;

    WaitFreeQueue(int capacity)
    {
        this.head=0;
        this.tail=0;
        this.capacity=capacity;
        this.items = new int[capacity];
    }
  
    public void enq(int x) 
    {
      if (this.tail-this.head == this.capacity)
      {
          System.out.println("enq error");
          return;
      }
      this.items[tail % capacity] = x; 
      tail++;
      //System.out.println("added "+String.valueOf(x)+" to queue");
    }
    public int deq() 
    {
       if (this.tail == this.head || this.tail-this.head != this.capacity) return -1000;
       int item = this.items[head % capacity]; 
       head++;

       return item;
    }
    public void printQueue()
    {
        for(int i=0;i<this.capacity;i++)
        {
            System.out.print(this.items[i]);
            System.out.print(" ");
        }
        System.out.println("");
    }
}