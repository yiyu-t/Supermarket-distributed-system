import com.google.gson.Gson;
import java.sql.*;

public class PurchaseDao {

  public void createPurchase (Purchase newPurchase) throws SQLException {
    String insertQueryStatement = "INSERT INTO Purchase (StoreId, CustomerId, Date, Item) " +
        "VALUES (?,?,?,?)";

    // try with resource
    try (Connection conn = DataSource.getConnection();
        PreparedStatement preparedStatement = conn.prepareStatement(insertQueryStatement);) {
      preparedStatement.setInt(1, newPurchase.getStoreId());
      preparedStatement.setInt(2, newPurchase.getCustomerId());
      preparedStatement.setString(3, newPurchase.getDate());

      Gson gson = new Gson();
      String items = gson.toJson(newPurchase.getItems());
      preparedStatement.setString(4, items);

      // execute insert SQL statement
      preparedStatement.executeUpdate();
    }
  }
}
