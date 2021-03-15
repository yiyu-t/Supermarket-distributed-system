public class Purchase {
  private int storeId;
  private int customerId;
  private String date;
  private String item;

  public Purchase(int storeId, int customerId, String date, String item) {
    this.storeId = storeId;
    this.customerId = customerId;
    this.date = date;
    this.item = item;
  }

  public int getStoreId() {
    return storeId;
  }

  public int getCustomerId() {
    return customerId;
  }

  public String getDate() {
    return date;
  }

  public String getItem() {
    return item;
  }
}
