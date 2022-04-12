import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicMarkableReference;

//concurrent linked list
class p1
{
    public static AtomicMarkableReference<Node> head = new AtomicMarkableReference<Node>(new Node(0), false);
    public static AtomicInteger presentCount = new AtomicInteger(0);
    public static AtomicInteger presentsDone = new AtomicInteger(0);
    public static int numPresents = 500000;
    public static void main(String[] args)
    {
        Runnable servant = ()->{
            //while presents are available
            while(presentsDone.getAndIncrement()<numPresents)
            {
                int present = presentCount.incrementAndGet();
                add(present);
                remove(present);

                //find if present was in concurrent ll

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
            
            //for testing
            // try{
            //     threadList.get(i).join();  
            // }
            // catch(Exception e){
            //     System.out.println(e);
            // }
        }

        long end = System.nanoTime();

        while(presentsDone.get()<numPresents)
        {
            //wait
        }

        System.out.println("The servants took "+String.valueOf((end-start)/1000000)+" milliseconds to make thank you cards");
        //printList();
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
            Window window = find(head.getReference(),key);
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
                // System.out.println(node.key);
                // System.out.println(pred.key);
                node.next = new AtomicMarkableReference<Node>(curr, false);

                if(pred.next.compareAndSet(curr, node, false, false)) 
                {
                    //System.out.println("added "+String.valueOf(node.key));
                    return true;
                }
                else
                {
                    //System.out.println("curr "+String.valueOf(curr.key)+" expected "+String.valueOf(pred.next.getReference().key)+" new "+String.valueOf(node.key));
                }
            }
        }
    }

    public static boolean remove(int key) 
    {
        boolean snip; 
        while (true) 
        {
            Window window = find(head.getReference(),key);
            Node pred = window.pred, curr = window.curr;
            //System.out.println(curr.key); //1000000 means not added yet
    
            if (curr==null || curr.key != key) 
            {
                //someone already removed it
                //System.out.println("already removed");
                return false;
            } 
            else 
            {
                Node succ = curr.next.getReference();

                if(curr.next.compareAndSet(succ, succ, false, true)) 
                {
                    /*
                    delete if not marked or already deleted. 
                    if expectedReference is not curr, then someone
                    already deleted curr. if expected mark is
                    not false, then someone is already in 
                    proccess of deleting 
                    */
                    pred.next.compareAndSet(curr, succ, false, false);
                    return true; 
                }
            }
        }
    } 
    public static Window find(Node head, int key) 
    {
        Node pred = null, curr = null, succ = null;
        boolean[] marked = {false}; 
        boolean snip;

        retry: while (true) 
        {
            pred = head;
            curr = head.next.getReference(); 
            
            while (curr.key<key) 
            {
                //check if next node marked
                succ = curr.next.get(marked); 
                while (marked[0]) 
                {
                    /*delete all marked nodes before 
                    finding. if already deleted, 
                    the list was changed and you 
                    need to traverse again*/
                    snip = pred.next.compareAndSet(curr, succ, false, false);
                    if (!snip) continue retry; 
                    curr = succ;
                    if(curr.key!=1000000)
                    {
                        //advance if not end of list and not marked
                        succ = curr.next.get(marked); 
                        //System.out.println(marked[0]);
                    }
                    else
                    {
                        //deleted all nodes up until end
                        //of list. return window
                        //System.out.println(String.valueOf(pred.key)+" "+String.valueOf(curr.key));
                        return new Window(pred, curr);
                    }
                }       
                pred = curr;
                curr = succ;
            }

            return new Window(pred,curr);
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
    public Node pred;
    public Node curr;
    
    Window(Node pred, Node curr){
        this.pred = pred;
        this.curr = curr;
    }

    // public void find(AtomicMarkableReference<Node> head, int key)
    // {
    //     Node pred = head.getReference();
    //     Node curr = head.getReference().next.getReference();
    //     //System.out.println("(should be 0) curr="+String.valueOf(curr.key));


    //     while(curr.key<key)
    //     {
    //         //System.out.println("curr visiting "+String.valueOf(curr.key));
    //         //adjust
    //         pred=curr;
    //         //advance
    //         curr=curr.next.getReference();
    //     }

    //     //store
    //     this.curr=curr;
    //     this.pred=pred;

    //     //System.out.println("curr after advance="+String.valueOf(this.curr.key));
    // }
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