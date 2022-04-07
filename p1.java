import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicMarkableReference;

//concurrent linked list
class p1
{
    public static AtomicMarkableReference<Node> head = new AtomicMarkableReference<Node>(new Node(0), false);
    public static AtomicInteger presentCount = new AtomicInteger(0);
    public static int numPresents = 800;
    public static void main(String[] args)
    {
        Runnable servant = ()->{
            //while presents are available
            while(true)
            {
                int present = presentCount.incrementAndGet();

                if(present>numPresents)
                {
                    break;
                }
                else
                {
                    //System.out.println("adding "+String.valueOf(present));
                    //spin lock until present is 
                    //added to concurrent ll
                    // while(!add(present))
                    // {
                    //     //System.out.println("spinning add");
                    // }

                    //System.out.println("added "+String.valueOf(present)+" to ll");
                    //spin lock until present is 
                    //removed from concurrent ll
                    while(!remove(present))
                    {
                        //System.out.println("spinning remove");
                    }

                    // System.out.println("removed "+String.valueOf(present)+" from ll");
                    //find if present was in concurrent ll
                }
            }
        };

        ArrayList<Thread> threadList = new ArrayList<Thread>();
        head.getReference().next = new AtomicMarkableReference<Node>(new Node(1000000),false);
        int N=4;
        long start = System.nanoTime();

        for(int i=0;i<N;i++)
        {
            threadList.add(new Thread(servant));
            threadList.get(i).start();          
        }

        for(int i=1;i<numPresents+1;i++)
        {
            add(i);
        }

        while(presentCount.get()<=numPresents)
        {
            //wait
        }

        long end = System.nanoTime();

        System.out.println("The servants took "+String.valueOf((end-start)/1000000000)+" seconds to make thank you cards");
        printList();
    }

    public static void printList()
    {
        AtomicMarkableReference<Node> temp = head;
        Node curr = head.getReference();

        while(temp!=null)
        {
            curr=temp.getReference();
            System.out.println(curr.key);
            temp=curr.next;
        }
    }

    public static boolean add(int key) 
    {
        while (true) 
        {
            Window window = new Window();
            window.find(head, key);
            Node pred = window.pred, curr = window.curr;
            //System.out.println(curr.key);
            if (curr.key == key) 
            {
                //someone else already added it
                return false;
            } 
            else 
            {
                //curr should be greater than new
                Node node = new Node(key);
                //System.out.println(node);
                //System.out.println(pred);
                node.next = new AtomicMarkableReference<Node>(curr, false);

                if (pred.next.compareAndSet(curr, node, false, false)) 
                {
                    return true;
                }
            }
        }
    }

    public static boolean remove(int key) 
    {
        boolean snip; 
        while (true) 
        {
            Window window = new Window();
            window.find(head, key);
            Node pred = window.pred, curr = window.curr;
            //System.out.println(curr.key);
            if (curr==null || curr.key != key) 
            {
                //someone already removed it
                return false;
            } 
            else 
            {
                Node succ = curr.next.getReference();
                snip = curr.next.compareAndSet(succ, succ, false, true);
                if (!snip) continue;
                //System.out.println(pred);
                if(pred.next.compareAndSet(curr, succ, false, false));
                {
                    //System.out.println("removed "+String.valueOf(curr.key));
                    return true;
                }
                        
            }
        }
    }          
}

class Node{
    public int key;
    public AtomicMarkableReference<Node> next;

    Node(int key){
        this.key = key;
        this.next = null;
    }
}

class Window{
    public Node curr;
    public Node pred;
    
    Window(){
        this.curr = null;
        this.pred = null;
    }

    public void find(AtomicMarkableReference<Node> head, int key)
    {
        Node pred = head.getReference();
        Node curr = head.getReference().next.getReference();
        //System.out.println("(should be 0) curr="+String.valueOf(curr.key));


        while(curr.key<key)
        {
            //System.out.println("curr visiting "+String.valueOf(curr.key));
            //adjust
            pred=curr;
            //advance
            curr=curr.next.getReference();
        }

        //store
        this.curr=curr;
        this.pred=pred;

        //System.out.println("curr after advance="+String.valueOf(this.curr.key));
    }
}

// add 1
// 0 

// new atomicreference(new node(num),false)

// atomicreference
// -----
// get -> node -> key
//             -> next -> atomicreference
//                         -------
//                         get -> node -> ...
//                                     -> ...
//                         -------
// ----