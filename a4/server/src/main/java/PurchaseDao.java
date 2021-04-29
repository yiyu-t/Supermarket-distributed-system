import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class PurchaseDao {

  private final int group;
  // private Connection conn;

  public PurchaseDao(int group) {
    this.group = group;
  }

  public void createPurchase(Purchase newPurchase) throws SQLException {
    String insertQueryStatement = "INSERT INTO Purchase (StoreId, CustomerId, Date, Item) " +
        "VALUES (?,?,?,?)";

    if (this.group == 1) {
      try (Connection conn = DataSource1.getConnection();
          PreparedStatement preparedStatement = conn.prepareStatement(insertQueryStatement);) {

        preparedStatement.setInt(1, newPurchase.getStoreId());
        preparedStatement.setInt(2, newPurchase.getCustomerId());
        preparedStatement.setString(3, newPurchase.getDate());
        preparedStatement.setString(4, newPurchase.getItem());

        // execute insert SQL statement
        preparedStatement.executeUpdate();

      }

    } else if (this.group == 2) {
      try (Connection conn = DataSource2.getConnection();
          PreparedStatement preparedStatement = conn.prepareStatement(insertQueryStatement);) {
        preparedStatement.setInt(1, newPurchase.getStoreId());
        preparedStatement.setInt(2, newPurchase.getCustomerId());
        preparedStatement.setString(3, newPurchase.getDate());
        preparedStatement.setString(4, newPurchase.getItem());

        // execute insert SQL statement
        preparedStatement.executeUpdate();
      }
    } else {
      try (Connection conn = DataSource3.getConnection();
          PreparedStatement preparedStatement = conn.prepareStatement(insertQueryStatement);) {
        preparedStatement.setInt(1, newPurchase.getStoreId());
        preparedStatement.setInt(2, newPurchase.getCustomerId());
        preparedStatement.setString(3, newPurchase.getDate());
        preparedStatement.setString(4, newPurchase.getItem());

        // execute insert SQL statement
        preparedStatement.executeUpdate();
      }
    }
  }
}
