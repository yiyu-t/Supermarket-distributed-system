import com.zaxxer.hikari.HikariDataSource;
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
        preparedStatement.setString(4, newPurchase.getItem());

        // execute insert SQL statement
        preparedStatement.executeUpdate();
      }
    }
  }
