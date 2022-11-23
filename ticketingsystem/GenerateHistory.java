package ticketingsystem;

import java.util.List;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.PrintStream;

class ThreadId {
    // Atomic integer containing the next thread ID to be assigned
    private static final AtomicInteger nextId = new AtomicInteger(0);

    // Thread local variable containing each thread's ID
    private static final ThreadLocal<Integer> threadId =
        new ThreadLocal<Integer>() {
            @Override protected Integer initialValue() {
                return nextId.getAndIncrement();
        }
    };

    // Returns the current thread's unique ID, assigning it if necessary
    public static int get() {
        return threadId.get();
    }
}
class myInt {
    volatile int value;
}

public class GenerateHistory {
	static int threadnum;//input
	static int testnum;//input
	static boolean isSequential;//input
	static int msec = 0;
	static int nsec = 0;
	static int totalPc;
    
	static  AtomicInteger sLock = new AtomicInteger(0); //Synchronization Lock
	static boolean[] fin;

	protected static boolean exOthNotFin(int tNum, int tid) {
		boolean flag = false;
		for (int k = 0; k < tNum; k++) {
			if (k == tid) continue;
			flag = (flag || !(fin[k])); 
		}
		return flag;
	}

    static void SLOCK_TAKE() {
    	while (sLock.compareAndSet(0, 1) == false) {}
    }

    static void SLOCK_GIVE() {
        sLock.set(0);
    }

    static boolean SLOCK_TRY() {
        return (sLock.get() == 0);
    }

/****************Manually Set Testing Information **************/

	static int routenum = 3; // route is designed from 1 to 3
	static int coachnum = 3; // coach is arranged from 1 to 5
	static int seatnum = 5; // seat is allocated from 1 to 20
	static int stationnum = 5; // station is designed from 1 to 5

	static int refRatio = 10; 
	static int buyRatio = 20; 
	static int inqRatio = 30; 


	static TicketingDS tds;
	final static List<String> methodList = new ArrayList<String>();
	final static List<Integer> freqList = new ArrayList<Integer>();
	final static List<Ticket> currentTicket = new ArrayList<Ticket>();
	final static List<String> currentRes = new ArrayList<String>();
    final static ArrayList<List<Ticket>> soldTicket = new ArrayList<List<Ticket>>();
	volatile static boolean initLock = false;
//	final static AtomicInteger tidGen = new AtomicInteger(0);
	final static Random rand = new Random();
	public static void initialization(){
	  tds = new TicketingDS(routenum, coachnum, seatnum, stationnum, threadnum);
	  for(int i = 0; i < threadnum; i++){
		List<Ticket> threadTickets = new ArrayList<Ticket>();
		soldTicket.add(threadTickets);
		currentTicket.add(null);
		currentRes.add("");
	  }
		//method freq is up to	
	  methodList.add("refundTicket");
	  freqList.add(refRatio);
	  methodList.add("buyTicket");
	  freqList.add(refRatio+buyRatio);
	  methodList.add("inquiry");
	  freqList.add(refRatio+buyRatio+inqRatio);
	  totalPc = refRatio+buyRatio+inqRatio;
	}
	public static String getPassengerName() {
		long uid = rand.nextInt(testnum);
		return "passenger" + uid; 
	}

  private static boolean readConfig(String filename) {
	try {
	  Scanner scanner = new Scanner(new File(filename));

	  while (scanner.hasNextLine()) {
			String line = scanner.nextLine();
			//System.out.println(line);
			Scanner linescanner = new Scanner(line);
			if (line.equals("")) {
				linescanner.close();
				continue;
			}
			if (line.substring(0,1).equals("#")) {
				linescanner.close();
				continue;
			}
			routenum = linescanner.nextInt();
			coachnum = linescanner.nextInt();
			seatnum = linescanner.nextInt();
			stationnum = linescanner.nextInt();

			refRatio = linescanner.nextInt();
			buyRatio = linescanner.nextInt();
			inqRatio = linescanner.nextInt();
			//System.out.println("route: " + routenum + ", coach: " + coachnum + ", seatnum: " + seatnum + ", station: " + stationnum + ", refundRatio: " + refRatio + ", buyRatio: " + buyRatio + ", inquiryRatio: " + inqRatio);
			linescanner.close();
	  }
	  scanner.close();
	}catch (FileNotFoundException e) {
	  System.out.println(e);
	}
		return true;
  }

	public static void print(long preTime, long postTime, String actionName){
	  Ticket ticket = currentTicket.get(ThreadId.get());
	  System.out.println(preTime + " " + postTime + " " +  ThreadId.get() + " " + actionName + " " + ticket.tid + " " + ticket.passenger + " " + ticket.route + " " + ticket.coach + " " + ticket.departure + " " + ticket.arrival + " " + ticket.seat + " " + currentRes.get(ThreadId.get()));
	}

	public static boolean execute(int num){
	  int route, departure, arrival;
	  Ticket ticket = new Ticket();;
	  switch(num){
		case 0://refund
		  if(soldTicket.get(ThreadId.get()).size() == 0)
			return false;
		  int n = rand.nextInt(soldTicket.get(ThreadId.get()).size());
		  ticket = soldTicket.get(ThreadId.get()).remove(n);
		  if(ticket == null){
			return false;
		  }
		  currentTicket.set(ThreadId.get(), ticket);
		  boolean flag = tds.refundTicket(ticket);
		  currentRes.set(ThreadId.get(), "true"); 
		  return flag;
		case 1://buy
          String passenger = getPassengerName();
          route = rand.nextInt(routenum) + 1;
          departure = rand.nextInt(stationnum - 1) + 1;
          arrival = departure + rand.nextInt(stationnum - departure) + 1;
		  ticket = tds.buyTicket(passenger, route, departure, arrival);
		  if(ticket == null){
			ticket = new Ticket();
			ticket.passenger = passenger;
			ticket.route = route;
			ticket.departure = departure;
			ticket.arrival = arrival;
			ticket.seat = 0;
			currentTicket.set(ThreadId.get(), ticket);
			currentRes.set(ThreadId.get(), "false");
			return true;
		  }
		  currentTicket.set(ThreadId.get(), ticket);
		  currentRes.set(ThreadId.get(), "true");
		  soldTicket.get(ThreadId.get()).add(ticket);
		  return true;
		case 2:
          ticket.passenger = getPassengerName();
          ticket.route = rand.nextInt(routenum) + 1;
          ticket.departure = rand.nextInt(stationnum - 1) + 1;
          ticket.arrival = ticket.departure + rand.nextInt(stationnum - ticket.departure) + 1; // arrival is always greater than departure
		  ticket.seat = tds.inquiry(ticket.route, ticket.departure, ticket.arrival);
		  currentTicket.set(ThreadId.get(), ticket);
		  currentRes.set(ThreadId.get(), "true"); 
		  return true;
		default:
		  System.out.println("Error in execution.");
		  return false;
	  }
	}
/***********VeriLin***********/
  public static void main(String[] args) throws InterruptedException {
    if(args.length != 5){
	  System.out.println("The arguments of GenerateHistory is threadNum,  testNum, isSequential(0/1), delay(millionsec), delay(nanosec)");
	  return;
	}
	threadnum = Integer.parseInt(args[0]);
	testnum = Integer.parseInt(args[1]);
	if(args[2].equals("0")){
	  isSequential = false;
	}
	else if(args[2].equals("1")){
	  isSequential = true;
	}
	else{
	  System.out.println("The arguments of GenerateHistory is threadNum,  testNum, isSequential(0/1)");
	  return;
	}
	msec = Integer.parseInt(args[3]);
	nsec = Integer.parseInt(args[4]);
	readConfig("TrainConfig");
	Thread[] threads = new Thread[threadnum];
	myInt barrier = new myInt();
	fin = new boolean[threadnum];
	final long startTime = System.nanoTime();
	    
	for (int i = 0; i < threadnum; i++) {
	    	threads[i] = new Thread(new Runnable() {
                public void run() {
					if(ThreadId.get() == 0){
					  initialization();
					  initLock = true;
					}
					else{
					  while(!initLock){
						;
					  }
					}
					for(int k = 0; k < testnum; k++){
					  int sel = rand.nextInt(totalPc);
					  int cnt = 0;
					  if(isSequential){
						while (ThreadId.get() != barrier.value && exOthNotFin(threadnum, ThreadId.get()) == true) {}
	                    SLOCK_TAKE();
					  }

					  for(int j = 0; j < methodList.size(); j++){
						if(sel >= cnt && sel < cnt + freqList.get(j)){
						  if(msec != 0 || nsec != 0){
							try{
							  Thread.sleep(msec, nsec);
							}catch(InterruptedException e){
							  return;
							}
						  }
						  long preTime = System.nanoTime() - startTime;
						  boolean flag = execute(j);
						  long postTime = System.nanoTime() - startTime;
						  if(flag){
							print(preTime, postTime, methodList.get(j));
						  }
						  cnt += freqList.get(j);
						}
					  }

					  if (isSequential) {
						if (k == testnum - 1)
						  fin[ThreadId.get()] = true;
						if (exOthNotFin(threadnum, ThreadId.get()) == true) {
						  barrier.value = rand.nextInt(threadnum);
						  while (fin[barrier.value] == true) {
							barrier.value = rand.nextInt(threadnum);
						  }
						}
						SLOCK_GIVE();
					  }
					}

				}
            });
			threads[i].start();
	  }
	
	  for (int i = 0; i< threadnum; i++) {
		threads[i].join();
	  }	
	}
}
