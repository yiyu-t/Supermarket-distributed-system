import com.google.gson.annotations.SerializedName;

public class Item {
  @SerializedName("ItemID")
  private String ItemID;
  @SerializedName("numberOfItems:")
  private Integer numberOfItems;

  public Item(String ItemID, Integer numberOfItems) {
    this.ItemID = ItemID;
    this.numberOfItems = numberOfItems;
  }

  public String getItemID() {
    return ItemID;
  }

  public Integer getNumberOfItems() {
    return numberOfItems;
  }

  @Override
  public String toString() {
    return "Item{" +
        "ItemID='" + ItemID + '\'' +
        ", numberOfItems=" + numberOfItems +
        '}';
  }
}
