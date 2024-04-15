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
	
	Object talkCondition;
	Object[] eatCondition;
	
	boolean someoneTalking;

	/**
	 * Constructor
	 */
	public Monitor(int piNumberOfPhilosophers)
	{
		// TODO: set appropriate number of chopsticks based on the # of philosophers
		nbOfChopsticks = piNumberOfPhilosophers; 
		
	    
		//
		
		// Create the status array for the philosophers
		status = new Status[piNumberOfPhilosophers];
		talkCondition = new Object();
		eatCondition = new Object[piNumberOfPhilosophers];
		
		//Set all philosophers to thinking at first, and intialize the condition arrays
		for( int i = 0 ; i< piNumberOfPhilosophers ; i++) {
			status[i] = Status.Thinking;
			
		    eatCondition[i] = new Object();
		}
		//Nobody is talking because all are thinking
		someoneTalking=false;
	}

	/*
	 * -------------------------------
	 * User-defined monitor procedures
	 * -------------------------------
	 */

	/*
	 * -------------------------------
	 * EATING
	 * -------------------------------
	 * Logic:
	 * pickUp(me) : make philosopher hungry , then test neighbours. 
	 * test:  if my left neighbour is not eating , and my right neightbour is not eating, and I am hungry, then eat. 
	 * signal yourself to eat!  myself.signal()   -- if I was waiting before, I will eat, if I was not, it will be lost and okay.
	 * if when I come back from the test I am not eating , aka -- left neighbour or right neighbour is eating or I am not hungry: myself.wait()  
	 **/
	 
	
	//Needed to test neighbours. 
	private synchronized void testEat(int philosopherID) {
		//No need to subtrat 1 since was done when passing as parameter
		int left = (philosopherID -1 + nbOfChopsticks) % nbOfChopsticks;
		int right = (philosopherID + 1 + nbOfChopsticks) % nbOfChopsticks;
		
		//Checking that left and right neighbours are not eating and that I am hungry
		if((status[left] != Status.Eating) && 
			(status[right] != Status.Eating) &&
			(status[philosopherID] == Status.Hungry)){
			
			//Set my status to eating
			status[philosopherID] = Status.Eating;
			
			//Signal to myself. Needs to be synchronized to work with notify();
			synchronized(eatCondition[philosopherID]) {
				// Signal the condition for eating. 
						 eatCondition[philosopherID].notify(); 
			}
		}
	}
	
	//Grants request (returns) to eat when both chopsticks/forks are available.
	//Else forces the philosopher to wait()

	public void pickUp(final int piTID)	
	{
		//To convert philosopher numbering to array indexing
		int self = piTID-1;
		
		
		status[self] = Status.Hungry;
		//test if can start eating, if positive will return with status of Eating
		testEat(self);
		//If I didn't return eating, wait for signal
		if(status[self] != Status.Eating && status[self]==Status.Hungry) {
			try {
				synchronized (eatCondition[self]) {
	                eatCondition[self].wait();
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
		//To convert philosopher numbering to array indexing
		int self = piTID-1;
		//Find my left and right neighbour, add a full round to avoid negative indexes
		int left = (self -1 + nbOfChopsticks) % nbOfChopsticks;
		int right = (self + 1 + nbOfChopsticks) % nbOfChopsticks;
		
		status[self] = Status.Thinking; // set my status back to thinking
		testEat(left);
		testEat(right);
	}

	/*
	 * -------------------------------
	 * TALKING
	 * -------------------------------
	 * Logic: only one philosopher at a time can be talking. If one wants to talk, he becomes Talkative.
	 */
	
	private synchronized void testTalk(int i) { 

		//Check if anyone is talking
		for (int j=0; j<nbOfChopsticks; j++) {
			if (status[j]==Status.Talking){
				someoneTalking=true;
				break;
			}
			else someoneTalking=false;
		}
		
		//Check if I want to talk and no one else is talking
		if (status[i] == Status.Talkative && 	
				someoneTalking != true) 
		{
			status [i] = Status.Talking;
			
			synchronized(talkCondition) {
				talkCondition.notify();
			}
		}
	}
	
	
	public synchronized void requestTalk(final int piTID)
	{
		//Convert philosopher's ID into array ID
		int self = piTID-1;
		
		//Set myself as talkative since I want to talk
		status[self] = Status.Talkative;
		testTalk(self);
		//If the testTalk() was good and no one was talking, I will return as Talking. If not, means I must wait
		if (status[self] != Status.Talking) {
			//Await
			try {
				synchronized (talkCondition) {
	               talkCondition.wait();
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
		
		//Check if anyone else wanted to talk. 
		for (int j=0; j<nbOfChopsticks; j++) {
			if (status[j]==Status.Talkative) 
				testTalk(j);
		}
	}
	

}


