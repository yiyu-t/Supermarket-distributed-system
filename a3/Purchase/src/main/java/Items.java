import com.google.gson.annotations.SerializedName;
import java.util.ArrayList;

public class Items {
  @SerializedName("items")
  private ArrayList<Item> items;

  public Items(ArrayList<Item> items) {
    this.items = items;
  }

  public ArrayList getItems() {
    return items;
  }

  @Override
  public String toString() {
    return "Items{" +
        "items=" + items +
        '}';
  }
}
