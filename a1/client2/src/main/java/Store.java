import io.swagger.client.ApiException;
import io.swagger.client.api.PurchaseApi;
import io.swagger.client.model.Purchase;
import io.swagger.client.model.PurchaseItems;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadLocalRandom;

public class Store extends Thread {
  private StoreInfo storeInfo;
  private int storeId;
  private StoreLatches latches;
  private Client2 client;
  private BlockingQueue q;

  private int count = 0;
  private int successes = 0;
  private int failures = 0;
  private static final int AMOUNT_PER_ITEM = 1;
  private Purchase body;

  public Store(StoreInfo storeInfo, int storeId, StoreLatches latches, Client2 client,
      BlockingQueue q) {
    this.storeInfo = storeInfo;
    this.storeId = storeId;
    this.latches = latches;
    this.client = client;
    this.q = q;
  }

  @Override
  public void run() {
    PurchaseApi apiInstance = new PurchaseApi();

    String basePath = this.generateBasePath();
    apiInstance.getApiClient().setBasePath(basePath);

    for (int i = 0; i < storeInfo.getNumOfPurchasePerHour() * 9; i++) {
      this.count++;
      if (this.count == storeInfo.getNumOfPurchasePerHour() * 3) {
        this.latches.latch1.countDown();
      }
      if (this.count == storeInfo.getNumOfPurchasePerHour() * 5) {
        this.latches.latch2.countDown();
      }

      this.body = new Purchase(); // Purchase | items purchased
      this.addItems();

      try {
        // send request
        long start = System.currentTimeMillis();

        int status = apiInstance.newPurchaseWithHttpInfo(body, this.storeId,
            this.setCustomerID(), this.storeInfo.getDate()).getStatusCode();
        this.successes++;

        long end = System.currentTimeMillis();
        long latency = end - start;
        RecordData data = new RecordData(start, "POST", latency, status);
        
        // add to blocking queue
        q.put(data);
      } catch (InterruptedException | ApiException e) {
//        System.err.println(e.getCode());
        
        System.err.println("Exception when calling PurchaseApi#newPurchase");
        this.failures++;
        // TODO: 2/15/21 add failures 
      }
    }
    // after run, pass on failures and successes
    this.incSuc();
    this.incFail();
    this.latches.latchAll.countDown();
  }

  private String generateBasePath() {
    return "http://" + this.storeInfo.getIPAddress() + "/server_war";
  }

  public synchronized void incSuc() {
    this.client.totalSuccesses += this.getSuccesses();
  }

  public synchronized void incFail() {
    this.client.totalFailures += this.getFailures();
  }

  private int setCustomerID(){
    int r = ThreadLocalRandom.current().nextInt(this.storeId * 1000,
        this.storeId * 1000 + this.storeInfo.getNumOfCustomers());
    return r;
  }

  private void addItems() {
    for (int j = 0; j < this.storeInfo.getNumOfItems(); j++) {
      String itemID = String.valueOf(generateItemID());
      PurchaseItems newItem = generateItems(itemID);
      body.addItemsItem(newItem);
    }
  }

  private PurchaseItems generateItems(String itemID) {
    PurchaseItems item = new PurchaseItems();

    item.itemID(itemID);
    item.numberOfItems(AMOUNT_PER_ITEM);
    return item;
  }

  private int generateItemID() {
    int r = ThreadLocalRandom.current().nextInt(1, this.storeInfo.getMaxItemID());
    return r;
  }

  public int getSuccesses() {
    return successes;
  }

  public int getFailures() {
    return failures;
  }
}
