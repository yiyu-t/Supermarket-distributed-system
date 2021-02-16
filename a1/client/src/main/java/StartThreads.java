import java.util.ArrayList;

public class StartThreads extends Thread {
  private int numOfStores;
  private int startID;
  private StoreInfo storeInfo;
  private StoreLatches latches;
  private Client client;

  private ArrayList<Store> storeList = new ArrayList<>();

  public StartThreads(int numOfStores, int startID, StoreInfo storeInfo,
      StoreLatches latches, Client client) {
    this.numOfStores = numOfStores;
    this.startID = startID;
    this.storeInfo = storeInfo;
    this.latches = latches;
    this.client = client;
  }

  public void buildThreads() {
    for (int i = 0; i < this.numOfStores; i++) {
      Store store = new Store(storeInfo, startID + i, latches, client);
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
