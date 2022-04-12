
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
//to min(time of rewrite - 10,0) through 
//min(time of rewrite,10). this guarantees interval 
//of 10

//since there are 4 heaps+1 queue, the heaps
//will have one thread lock and execute crud 
//operation. the queue will be concurrent with
//1 adding thread and one removing thread. update
//class variable then unlock

//main lock will unlock every .5 seconds then lock
//once all operations are done. 

public class p2 {
    
}

// [1 2 3 4 5 6 7 8 9 9]
// heaps:
// 1   9
// now check
// at time=9, (9-1)=8 diff rewritten.
// record=min(9-10,0):min(9,10)=0:10

// [2 3 4 5 6 7 8 9 9 100]
// heaps:
// 2   100
// now check
// at time=11, (100-2)=98 diff rewritten. 
// record=min(11-10,0):min(11,10)=1:11

//make it go like a switch, where setting=true is 
//setting phase, and setting=false is crud phase


// setting=true //main in charge of tracking time and making setting=true
// //main in charge of setting
// set tasks=0
// set tasksdone=0
// set new
// set old
// setting=false
// while tasksDone.getAndIncrement<6
//     switch tasks.getAndIncrement
//     //each case takes place concurrently
//         case 0
//         adding q
//         case 1
//         remove
//         case 2
//         adding maxh
//         remove maxh
//         case 3
//         add minh
//         remove minh
//         case 4 
//         add max10
//         remove max10
//         case 5
//         add min10
//         remove min10
// //now update results