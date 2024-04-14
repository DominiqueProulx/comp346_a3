/**
 * Class Monitor
 * To synchronize dining philosophers.
 *
 * @author Serguei A. Mokhov, mokhov@cs.concordia.ca
 */

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.*;

public class Monitor
{
	/*
	 * ------------
	 * Data members
	 * ------------
	 */
	//Possible status for the philosophers:
	enum Status {
		  Hungry,
		  Eating,
		  Talkative,
		  Talking,
		  Thinking
		};
		
	Status[] status;  // will hold the philosophers status
	
	int nbOfChopsticks;  // there are as many chopsticks as there are philosophers sitting at the table.
		
	//Monitor conditions implemented as object

	Object myself_T;
	Object myself_E;
	
	boolean someoneTalking;

	/**
	 * Constructor
	 */
	public Monitor(int piNumberOfPhilosophers)
	{
		// TODO: set appropriate number of chopsticks based on the # of philosophers
		nbOfChopsticks = piNumberOfPhilosophers; 
		myself_T = new Object();
	    myself_E = new Object();
	    
		//
		
		// Create the status array for the philosophers
		status = new Status[piNumberOfPhilosophers];
		
		//Set all philosophers to thinking at first
		for( int i = 0 ; i< piNumberOfPhilosophers ; i++) {
			status[i] = Status.Thinking;
		}
		//Nobody is talking because all are thinking
		boolean someoneTalking=false;
	}

	/*
	 * -------------------------------
	 * User-defined monitor procedures
	 * -------------------------------
	 */

	/**
	 * Grants request (returns) to eat when both chopsticks/forks are available.
	 * Else forces the philosopher to wait()
	 */
	public synchronized void pickUp(final int piTID)   //
	/**pickup(me) : make philosopher hungry , then test neightbours. 
	in the test:  if my left neighbour is not eating , and my right neightbour is not eating, and I am hungry, then eat. 
	signal yourself to eat!  myself.signal()   -- if I was waiting before, I will eat, if I was not, it will be lost and okay.
	if when I come back from the test I am not eating , aka -- left neighbour or right neighbour is eating or I am not hungry: myself.wait()  
	**/
	{
		status[piTID-1] = Status.Hungry; //Set my status to hungry
		test(piTID-1); // will return eating after this if it could
		if(status[piTID-1] != Status.Hungry) {    //If I didnt return hungry , await for signal.
			try {
				synchronized (myself_E) {
	                myself_E.wait();
				}
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
	}

	/**
	 * When a given philosopher's done eating, they put the chopstiks/forks down
	 * and let others know they are available.
	 */
	public synchronized void putDown(final int piTID)
	{
		status[piTID-1] = Status.Thinking; // set my status back to thinking
		test((piTID-1 + (nbOfChopsticks -1)) % nbOfChopsticks);  //make my left neighbour test
		test((piTID-1 + (nbOfChopsticks +1)) % nbOfChopsticks); //make my right neighbour test
	}

	/**
	 * Only one philosopher at a time is allowed to philosophy
	 * (while she is not eating).
	 */
	public synchronized void requestTalk(final int piTID)
	{
		status[piTID-1] = Status.Talkative;
		testTalk(piTID-1);
		if (status[piTID-1] != Status.Talking) {
			//Await
			try {
				synchronized (myself_T) {
	                myself_T.wait();
				}
			}
			catch (InterruptedException e){
				e.printStackTrace();
			}
		}
		
	}

	/**
	 * When one philosopher is done talking stuff, others
	 * can feel free to start talking.
	 */
	public synchronized void endTalk(final int piTID)
	{
		status[piTID-1] = Status.Thinking;
		for (int j=0; j<nbOfChopsticks; j++) {
			if (status[j]==Status.Talkative) 
				testTalk(j);
		}
	}
	//Do not remove 1 for the array index as it was removed when calling the method.
	private synchronized void test(int philosopherID) {  // added to test neighbors philosophers for eating
		if((status[ (philosopherID + (nbOfChopsticks - 1 ))% nbOfChopsticks] != Status.Eating) &&  // left neighbour not eating
				(status[(philosopherID + (nbOfChopsticks + 1) )% nbOfChopsticks] != Status.Eating) && //right neighbour not eating
				(status[philosopherID] == Status.Hungry)) //and i'm hungry
		{
			status[philosopherID] = Status.Eating; // set my status to eating
			 synchronized (myself_E) {
				 myself_E.notify(); // Signal the condition for eating. 
			 }
		}
	}
	
	private synchronized void testTalk(int i) { 

		
		for (int j=0; j<nbOfChopsticks; j++) {
			if (status[j]==Status.Talking){
				someoneTalking=true;
				break;
			}
			else someoneTalking=false;
		}
			
		if (status[i] == Status.Talkative && 	//I want to talk
				someoneTalking != true){		//and no one is talking
				
			status [i] = Status.Talking;
			synchronized(myself_T) {
				myself_T.notify();
			}
		}
	}
}


