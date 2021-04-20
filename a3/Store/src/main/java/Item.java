import com.google.gson.annotations.SerializedName;

public class Item {
  @SerializedName("ItemID")
  private Integer ItemID;
  @SerializedName("numberOfItems:")
  private Integer numberOfItems;

  public Item(Integer itemID, Integer numberOfItems) {
    ItemID = itemID;
    this.numberOfItems = numberOfItems;
  }

  public Integer getItemID() {
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
