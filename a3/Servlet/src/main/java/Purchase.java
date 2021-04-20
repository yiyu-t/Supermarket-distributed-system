import com.google.gson.annotations.SerializedName;

public class Purchase {
  private int storeId;
  private int customerId;
  private String date;
  @SerializedName("items")
  // todo try it out later
//  private ArrayList<Item> items;
   private Items items;

  public Purchase(int storeId, int customerId, String date, Items items) {
    this.storeId = storeId;
    this.customerId = customerId;
    this.date = date;
    this.items = items;
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

  public Items getItems() {
    return items;
  }

  @Override
  public String toString() {
    return "Purchase{" +
        "storeId=" + storeId +
        ", customerId=" + customerId +
        ", date='" + date + '\'' +
        ", items=" + items +
        '}';
  }
}
