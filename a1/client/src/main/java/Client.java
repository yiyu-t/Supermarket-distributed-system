import io.swagger.client.*;

import java.util.*;
import java.util.concurrent.CountDownLatch;

public class Client {
  protected int totalSuccesses;
  protected int totalFailures;

  public int getTotalSuccesses() {
    return this.totalSuccesses;
  }

  public int getTotalFailures() {
    return this.totalFailures;
  }

  public static void main(String[] args) throws ApiException, InterruptedException {
    // takes in the 7 parameters from the command line
    // set base path

    // default values
    int numOfCustomers = Integer.valueOf(args[0]);
    int maxItemID = Integer.valueOf(args[1]);
    int numOfPurchasePerHour = Integer.valueOf(args[2]);
    int numOfItems = Integer.valueOf(args[3]);
    String date = args[4];
    String IPAddress = args[5];
    int maxStore = Integer.valueOf(args[6]);
//    final int numOfCustomers = 1000; // default
//    final int maxItemID = 100000; // default
//    final int numOfPurchasePerHour = 60; // default
//    final int numOfItems = 5; // default
//    final String date = "20210101"; // default
//    final String IPAddress = "localhost:8080";
//    int maxStore = 256;

    Client client = new Client(); // pass it to the threads // or atomonic integers
    CountDownLatch latchAll = new CountDownLatch(maxStore);
    CountDownLatch latch1 = new CountDownLatch(1); // Latch1
    CountDownLatch latch2 = new CountDownLatch(1); // Latch2

    StoreLatches latches = new StoreLatches(latch1, latch2, latchAll);

    int phaseOne = maxStore / 4;
    int phaseTwo = maxStore / 4;
    int phaseThree = maxStore / 2;

    StoreInfo storeInfo = new StoreInfo(numOfCustomers, maxItemID, numOfPurchasePerHour, numOfItems,
        date, IPAddress);

    // create all the threads that are in charge of starting the three phases
    StartThreads startEast = new StartThreads(phaseOne, 1, storeInfo, latches, client);
    StartThreads startCentral = new StartThreads(phaseTwo, 1 + phaseOne, storeInfo, latches,
        client);
    StartThreads startWest = new StartThreads(phaseThree, 1 + phaseOne + phaseTwo, storeInfo,
        latches, client);

    // build all the threads
    startEast.buildThreads();
    startCentral.buildThreads();
    startWest.buildThreads();

    long start = System.currentTimeMillis(); // start time
    startEast.start(); // opens east stores

    // waits for hour 3 and opens central stores
    latch1.await();
    startCentral.start();

    // waits for hour 5 and opens west stores
    latch2.await();
    startWest.start();

    latchAll.await();

    long end = System.currentTimeMillis(); // end time
    long wallTime = end - start;
    int wallTimeSec = (int) (wallTime / 1000);
    int totalReq = client.getTotalSuccesses() + client.getTotalFailures();
    System.out.println("total successes: " + client.getTotalSuccesses());
    System.out.println("total failures: " + client.getTotalFailures());
    System.out.println("wall time (in seconds): " + wallTimeSec);
    System.out.println("throughput: " + totalReq / wallTimeSec);
  }
}
