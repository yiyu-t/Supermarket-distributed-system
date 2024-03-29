import java.util.function.ToDoubleBiFunction;
import org.apache.commons.dbcp2.*;
import org.w3c.dom.ls.LSOutput;

public class DBCPDataSource {
    private static BasicDataSource dataSource;

  private static final String HOST_NAME = System.getProperty("MySQL_IP_ADDRESS");
  private static final String PORT = System.getProperty("MySQL_PORT");
  private static final String DATABASE = "supermarket";
  private static final String USERNAME = System.getProperty("DB_USERNAME");
  private static final String PASSWORD = System.getProperty("DB_PASSWORD");

    static {
    System.out.println(HOST_NAME);
    System.out.println(PORT);
    System.out.println(USERNAME);
    System.out.println(PASSWORD);
      dataSource = new BasicDataSource();
      try {
        Class.forName("com.mysql.cj.jdbc.Driver");
      } catch (ClassNotFoundException e) {
        e.printStackTrace();
      }
      String url = String.format("jdbc:mysql://%s:%s/%s?serverTimezone=UTC", HOST_NAME, PORT, DATABASE);
      dataSource.setUrl(url);
      dataSource.setUsername(USERNAME);
      dataSource.setPassword(PASSWORD);
      dataSource.setInitialSize(10);
      dataSource.setMaxTotal(60);
    }

    public static BasicDataSource getDataSource() {
      return dataSource;
    }
}
