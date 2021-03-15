public class StoreInfo {
  private int numOfCustomers;
  private int maxItemID;
  private int numOfPurchasePerHour;
  private int numOfItems;
  private String date;
  private String IPAddress;

  public StoreInfo(int numOfCustomers, int maxItemID, int numOfPurchasePerHour, int numOfItems,
      String date, String IPAddress) {
    this.numOfCustomers = numOfCustomers;
    this.maxItemID = maxItemID;
    this.numOfPurchasePerHour = numOfPurchasePerHour;
    this.numOfItems = numOfItems;
    this.date = date;
    this.IPAddress = IPAddress;
  }

  public int getNumOfCustomers() {
    return numOfCustomers;
  }

  public int getMaxItemID() {
    return maxItemID;
  }

  public int getNumOfPurchasePerHour() {
    return numOfPurchasePerHour;
  }

  public int getNumOfItems() {
    return numOfItems;
  }

  public String getDate() {
    return date;
  }

  public String getIPAddress() {
    return IPAddress;
  }
}
