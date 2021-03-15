import io.swagger.client.*;

import java.io.IOException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;

public class Client2 {
  protected int totalSuccesses;
  protected int totalFailures;

  public int getTotalSuccesses() {
    return this.totalSuccesses;
  }

  public int getTotalFailures() {
    return this.totalFailures;
  }

  public static void main(String[] args) throws ApiException, InterruptedException, IOException {

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

    Client2 client = new Client2(); // pass it to the threads // or atomonic integers
    CountDownLatch latchAll = new CountDownLatch(maxStore);
    CountDownLatch latch1 = new CountDownLatch(1); // Latch1
    CountDownLatch latch2 = new CountDownLatch(1); // Latch2
    CountDownLatch latch3 = new CountDownLatch(1); // Latch3
    StoreLatches latches = new StoreLatches(latch1, latch2, latchAll);

    int phaseOne = maxStore / 4;
    int phaseTwo = maxStore / 4;
    int phaseThree = maxStore / 2;

    StoreInfo storeInfo = new StoreInfo(numOfCustomers, maxItemID, numOfPurchasePerHour, numOfItems,
        date, IPAddress);

    BlockingQueue q = new ArrayBlockingQueue(maxStore);

    // create all the threads that are in charge of starting the three phases
    StartThreads startEast = new StartThreads(phaseOne, 1, storeInfo, latches, client, q);
    StartThreads startCentral = new StartThreads(phaseTwo, 1 + phaseOne, storeInfo, latches,
        client, q);
    StartThreads startWest = new StartThreads(phaseThree, 1 + phaseOne + phaseTwo, storeInfo,
        latches, client, q);

    // build all the threads
    startEast.buildThreads();
    startCentral.buildThreads();
    startWest.buildThreads();

    long start = System.currentTimeMillis(); // start time
    startEast.start(); // opens east stores

    String path = "latency_data.csv";
    RecordWriter writer = new RecordWriter(q, path, latch3); // start writer
    writer.start();
    // waits for hour 3 and opens central stores
    latch1.await();
    startCentral.start();

    // waits for hour 5 and opens west stores
    latch2.await();
    startWest.start();

    latchAll.await();
    q.add(String.valueOf("finished"));
    latch3.await();

    // todo after everything is done, queue should have a stop signal
    long end = System.currentTimeMillis(); // end time
    long wallTime = end - start;
    int wallTimeSec = (int) (wallTime / 1000);
    int totalReq = client.getTotalSuccesses() + client.getTotalFailures();
    System.out.println("total successes: " + client.getTotalSuccesses());
    System.out.println("total failures: " + client.getTotalFailures());
    System.out.println("wall time (in seconds): " + wallTimeSec);
    System.out.println("throughput: " + totalReq / wallTimeSec);

    DataProcessor processor = new DataProcessor(path);
    processor.readFromFile();
    System.out.println("mean response time: " + processor.getMean());
    System.out.println("median response time: " + processor.getMedian());
    System.out.println("p99: " + processor.getP99());
    System.out.println("max response time: " + processor.getMax());
  }
}
