import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.sql.Connection;
import java.sql.SQLException;

public class DataSource {
  private static HikariConfig config = new HikariConfig();
  private static HikariDataSource ds;

  // info
  private static final String HOST_NAME = "a3.cdl7ma7gfbnk.us-east-1.rds.amazonaws.com";
  private static final String PORT = "3306";
  private static final String DATABASE = "supermarket";
  private static final String USERNAME = "admin";
  private static final String PASSWORD = "password";

  static {
    try {
      Class.forName("com.mysql.cj.jdbc.Driver");
    } catch (ClassNotFoundException e) {
      e.printStackTrace();
    }
    String url = String.format("jdbc:mysql://%s:%s/%s?serverTimezone=UTC", HOST_NAME, PORT, DATABASE);
    config.setJdbcUrl(url);
    config.setUsername(USERNAME);
    config.setPassword(PASSWORD);
    config.addDataSourceProperty("cachePrepStmts" , "true");
    config.addDataSourceProperty("prepStmtCacheSize" , "250");
    config.addDataSourceProperty("prepStmtCacheSqlLimit" , "2048");
    config.setMaximumPoolSize(60);
    ds = new HikariDataSource(config);
  }

  private DataSource() {}

  public static Connection getConnection() throws SQLException {return ds.getConnection();}
}
