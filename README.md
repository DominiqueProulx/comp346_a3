# comp346_a3

------------------------------------------------------------------------------------
Dominique Proulx - 40177566
Katya 
  
COMP 346
Programming Assignment 3 
Due date : April 14th 2024
 
----------------------------------------------------------------------------------
  
  
  
 Questions/Todo  :
 
 - need to capture user input for number of philosophers
 - Need to create the philosophers

  -------------------------------------
 Information 
  -------------------------------------

  
  
Files distributed with the assignment requirements:
- common/BaseThread.java - unchanged
- DiningPhilosophers.java - the main()
- Philosopher.java - extends from BaseThread
       - eat() : puts thread to sleep for a time
       - think() - does nothing
       - talk() , invokes saySomething()
       
- Monitor.java - the monitor for the system
- Makefile - take a look

 
 -------------------------------------
 Pseudocode
 -------------------------------------
 THINKING ->HUNGRY-> EATING-> TALKATIVE-> TALKING 
 await if HUNGRY and cannot eat.
 await if TALKATIVE and cannot talk.
 
  In the monitor : 
  
  variables: 
  - number of philosophers n 
  - monitor variables : possible states of philosophers {thinking, hungry, eating, talkative,talking}. make an array of size n , the array can hold any of these values form the set of possible status.
  - condition : status[me] 
 
 ****** To eat :******
 1) my status need to be hungry
 2) neither of my neighbors need to be eating
 2) I am not eating
 
 my left neighbour : i + (n-1) % n  
 my right neighnour : i +(n+1) % n
 
status :  {thinking, hungry,eating,talkative,talking}
 condition myself_E  // myself Eat
 condition myself_T  // myself Talk. needed a different condition or else I will be signaled to eat when I want to talk.
 

Initialize everybody to thinking.  : in monitor since the status array is variable being manipulated by the synchronized operations.


pickup(me) : make philosopher hungry , then test neightbours. 
in the test:  if my left neighbour is not eating , and my right neightbour is not eating, and I am hungry, then eat. 
signal yourself to eat!  myself.signal()   -- if I was waiting before, I will eat, if I was not, it will be lost and okay.
if when I come back from the test I am not eating , aka -- left neighbour or right neighbour is eating or I am not hungry: myself.wait()  

put down the fork(me) : change my state to thinking. 
call test() for both my neighbours. since I am passing the id of my neighbour to test, if it signals , it will signal my neigjbour.


 Initialization. At first , everybody is thinking:
  for i = 0  to n-1 do
  	status[i] = 'Thinking';

pickUp(i)
status[i] = "Hungry"
test(i) // will return eating after this if it could
if status[i] NOT eating
	myself_E.await() 

test(i)
 if i + (n-1) % n  NOT eating 
 AND
 if i + (n+1) % n NOT eating
 AND
 status[i] = 'Hungry'
 	status[i] = 'Eating'
 	myself.signal()	  //wake myself up
 
putDown(i)
 status[i]='Thinking'
 test( i +(n-1) % n)
 test( i + (n+1) % n)


***** to talk : ******

no need to re-initialized everybody to thinking.

requestTalk()
status[i] = 'Talkative'
//cannot talk if anybody is talking. 
test2()  // will test if anybody is else is "talking", if not , will return from this with status "talking", if not , it will have to wait.
if (status[i] NOT 'Talking') myself_t.await() 

 
  	
test2(i) // iterate through array and see if anybody is talking
 boolean isSomebodyTalking = false;
 for i = 0 to n-1 do
   if status[i] == 'Talking'       // I'm not doing anything to skip over myself as I know I am "Talkative"
   		isSomebodyTalking = true
   		break;
   if isSomebodyTalking == false 
   		status[i] = 'Talking'
   		mySelf_T.signal()  // signal myself to talk
// will return from test 2 talking if I could.

endTalk(i)
  status[i] = 'thinking'
  //notify any other thread that was waiting to talk   // signal.All()?
  for i= 0 to n-1 do
    test2(i)  // tell everybody to test themselves. Since I am thinking it will do nothing for me.
 	
 
 
 
 