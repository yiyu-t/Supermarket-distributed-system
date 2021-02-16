import java.util.ArrayList;
import java.util.concurrent.BlockingQueue;

public class StartThreads extends Thread {
  private int numOfStores;
  private int startID;
  private StoreInfo storeInfo;
  private StoreLatches latches;
  private Client2 client;
  private BlockingQueue q;

  private ArrayList<Store> storeList = new ArrayList<>();

  public StartThreads(int numOfStores, int startID, StoreInfo storeInfo,
      StoreLatches latches, Client2 client, BlockingQueue q) {
    this.numOfStores = numOfStores;
    this.startID = startID;
    this.storeInfo = storeInfo;
    this.latches = latches;
    this.client = client;
    this.q = q;
  }

  public void buildThreads() {
    for (int i = 0; i < this.numOfStores; i++) {
      Store store = new Store(storeInfo, startID + i, latches, client, q);
      storeList.add(store);
    }
  }

  @Override
  public void run() {
    for (Store store : storeList) {
      store.start();
    }
  }

}
