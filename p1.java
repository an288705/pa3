import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicMarkableReference;

//concurrent linked list
class p1
{
    public static AtomicMarkableReference<Node> head = new AtomicMarkableReference<Node>(new Node(-1), false);
    public static AtomicInteger presentCount = new AtomicInteger(0);
    public static int numPresents = 500000;
    public static void main(String[] args)
    {
        Runnable servant = ()->{
            //while presents are available
            while(true)
            {
                int present = presentCount.incrementAndGet();

                if(present>10)
                {
                    break;
                }
                else
                {
                    System.out.println("adding "+String.valueOf(present));
                    //spin lock until present is 
                    //added to concurrent ll
                    while(!add(present))
                    {
                        //System.out.println("spinning add");
                    }

                    System.out.println("added "+String.valueOf(present)+" to ll");
                    //spin lock until present is 
                    //removed from concurrent ll
                    // while(!remove(present))
                    // {
                    //     //System.out.println("spinning remove");
                    // }

                    // System.out.println("removed "+String.valueOf(present)+" from ll");
                    //find if present was in concurrent ll
                }
            }
        };

        ArrayList<Thread> threadList = new ArrayList<Thread>();
        head.getReference().next = new AtomicMarkableReference<Node>(new Node(0),false);
        int N=4;
        long start = System.nanoTime();

        for(int i=0;i<N;i++)
        {
            threadList.add(new Thread(servant));
            threadList.get(i).start();          
        }

        while(presentCount.get()<10)
        {
            //wait
        }

        long end = System.nanoTime();

        System.out.println("done");
        printList();
        //System.out.println("The servants took "+String.valueOf((end-start)/1000000000)+" seconds to make thank you cards");
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
        boolean splice;
        while (true) 
        {
            Window window = new Window();
            window.find(head, key);
            Node pred = window.pred, curr = window.curr;
            System.out.println(curr.key);
            if (curr.key == key) 
            {
                //someone else already added it
                return false;
            } 
            else 
            {
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
            if (curr==null || curr.key != key) 
            {
                return false;
            } 
            else 
            {
                //System.out.println(curr);
                Node succ = curr.next.getReference();
                snip = curr.next.compareAndSet(succ, succ, false, true);
                if (!snip) continue;
                //System.out.println(pred);
                pred.next.compareAndSet(curr, succ, false, false);
                
                return true;
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
        AtomicMarkableReference<Node> temp = head.getReference().next;
        Node pred = head.getReference();
        Node curr = head.getReference().next.getReference();
        this.curr=curr;
        this.pred=pred;
        System.out.println("(should be 0) curr="+String.valueOf(curr.key));


        while(temp!=null && temp.getReference().key<=key)
        {
            curr=temp.getReference();
            //store
            this.curr=curr;
            this.pred=pred;

            if(curr.key==key)
            {
                break;
            }
            //adjust
            pred=curr;
            //advance
            temp=curr.next;
        }
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