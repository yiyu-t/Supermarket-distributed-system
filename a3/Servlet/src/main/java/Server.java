import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeoutException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.ObjectPool;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.apache.commons.pool2.impl.GenericObjectPool;

@WebServlet("/Server")
public class Server extends HttpServlet {
  private String msg;
  private static final String EXCHANGE_NAME = "logs";
  private static final String HOST_NAME = "3.84.234.107";
  private static final String RPC_QUEUE = "rpc_queue";
  private static final String USERNAME = "user1";
  private static final String PASSWORD = "newpass";
  private static final Boolean DURABLE = false;
  private Connection conn;
  // private ThreadLocal<Channel> threadLocal;
  private ObjectPool<Channel> channelPool;
  private ConnectionFactory factory;
  private final int PERSISTENT = 1;
  private ChannelFactory channelFactory = new ChannelFactory();
  public class ChannelFactory extends BasePooledObjectFactory<Channel> {

    @Override
    public Channel create() throws Exception {
      return conn.createChannel();
    }

    @Override
    public PooledObject<Channel> wrap(Channel channel) {
      return new DefaultPooledObject<Channel>(channel);
    }
  }

  @Override
  public void init() throws ServletException {
    super.init();

    factory = new ConnectionFactory();
    factory.setUsername(USERNAME);
    factory.setPassword(PASSWORD);
    factory.setHost(HOST_NAME);
    try {
      conn = factory.newConnection();
      channelPool = new GenericObjectPool<>(channelFactory);
      // declare the exchange
      Channel dummy = channelPool.borrowObject();
      dummy.exchangeDeclare(EXCHANGE_NAME, "fanout", DURABLE);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  protected void doPost(HttpServletRequest req, HttpServletResponse res)
      throws ServletException, IOException {
    res.setContentType("text/plain");

    // get things after 'purchases'
    String urlPath = req.getPathInfo();

    if (urlPath == null || urlPath.isEmpty()) {
      res.setStatus(HttpServletResponse.SC_NOT_FOUND);
      res.getWriter().write("404: missing url");
      return;
    }

    // create the string
    BufferedReader reqBodyBuffer = req.getReader();
    StringBuilder reqBody = new StringBuilder();
    String line;
    while ((line = reqBodyBuffer.readLine()) != null) {
      reqBody.append(line);
    }

    String body = reqBody.toString();
    // handle empty
    if (body == null || body.isEmpty()) {
      res.setStatus(HttpServletResponse.SC_NOT_FOUND);
      res.getWriter().write("404: missing input");
      return;
    }

    String[] urlParts = urlPath.split("/");
    // and now validate url path and return the response status code
    // (and maybe also some value if input is valid)
    if (!isPostUrlValid(urlParts)) {
      res.setStatus(HttpServletResponse.SC_NOT_FOUND);
      res.getWriter().write(msg);
      return;
    } else {
      // do any sophisticated processing with urlParts which contains all the url params
      int storeId = Integer.valueOf(urlParts[2]);
      int customerId = Integer.valueOf(urlParts[4]);
      String date = urlParts[6];
      String itemsString = body;

      Gson gson = new GsonBuilder().create();
      Items items = gson.fromJson(itemsString, Items.class);

      // '{"storeId":100,"customerId":108,"date":"20200101","items":{"items":[{"ItemID":"217","numberOfItems:":4},{"ItemID":"100","numberOfItems:":5}]}}'
      Purchase newPurchase = new Purchase(storeId, customerId, date, items);
      String jsonMessage = gson.toJson(newPurchase);
      // System.out.println(jsonMessage);

      Channel channel = null;
      try {
        channel = channelPool.borrowObject();
        // publish to the exchange
        channel.basicPublish(EXCHANGE_NAME, "", null, jsonMessage.getBytes("UTF-8"));
        System.out.println(" [x] Sent '" + jsonMessage + "'");
      } catch (Exception e) {
        e.printStackTrace();
      } finally {
        if (channel != null) {
          try {
            // return the channel
            channelPool.returnObject(channel);
          } catch (Exception e) {
            e.printStackTrace();
          }
        }
      }

//      if (threadLocal == null) {
//        threadLocal = new ThreadLocal<>();
//        threadLocal.set(conn.createChannel());
//      }
//
//      try (final Channel channel = conn.createChannel()) {
//        // make it a subscriber
//        channel.exchangeDeclare(EXCHANGE_NAME, "fanout");
//        //channel.queueDeclare(QUEUE_NAME, true, false, false, null);
//        channel.basicPublish(EXCHANGE_NAME, "", null, jsonMessage.getBytes("UTF-8"));

//        System.out.println(" [x] Sent '" + jsonMessage + "'");
    }
  }

  protected void doGet(HttpServletRequest req, HttpServletResponse res)
      throws ServletException, IOException {
    res.setContentType("text/plain");
    String urlPath = req.getPathInfo();
    System.out.println(urlPath);

    // check we have a URL!
    if (urlPath == null || urlPath.isEmpty()) {
      res.setStatus(HttpServletResponse.SC_NOT_FOUND);
      res.getWriter().write("404: Missing url");
      return;
    }

    String[] urlParts = urlPath.split("/");
    // and now validate url path and return the response status code
    // (and maybe also some value if input is valid)
    if (!isGetUrlValid(urlParts)) {
      res.setStatus(HttpServletResponse.SC_NOT_FOUND);
      res.getWriter().write(msg);
    } else {
      res.setStatus(HttpServletResponse.SC_OK);
      // do any sophisticated processing with urlParts which contains all the url params
      // send it by RPCClient
      final String corrId = UUID.randomUUID().toString();

      try (final Channel channel = conn.createChannel()) {
        // make it a subscriber
        String replyQueueName = channel.queueDeclare().getQueue();
        AMQP.BasicProperties props = new AMQP.BasicProperties
            .Builder()
            .correlationId(corrId)
            .deliveryMode(PERSISTENT)
            .replyTo(replyQueueName)
            .build();

        channel.basicPublish("", RPC_QUEUE, props, urlPath.getBytes("UTF-8"));
//        System.out.println(" [x] Sent '" + urlPath + "'");

        final BlockingQueue<String> response = new ArrayBlockingQueue<>(1);

        String ctag = channel.basicConsume(replyQueueName, true, (consumerTag, delivery) -> {
          if (delivery.getProperties().getCorrelationId().equals(corrId)) {
            response.offer(new String(delivery.getBody(), "UTF-8"));
          }
        }, consumerTag -> {
        });
        String result = response.take();
        System.out.println(result);
        channel.basicCancel(ctag);

        if (result != null && result.length() > 0) {
          res.getWriter().write(result);
        } else {
          res.getWriter().write("sorry, no data available");
        }
      } catch (IOException | TimeoutException | InterruptedException e) {
        e.printStackTrace();
        res.getWriter().write("error occurred!");
      }
    }
  }

  private boolean isGetUrlValid(String[] urlParts) {
    if (urlParts.length < 3) {
      msg = "404: missing some parameters.";
      return false;
    }

    try {
      Integer.parseInt(urlParts[2]);
    } catch (NumberFormatException e) {
      msg = "ID should be an integer";
      return false;
    }
    // LEN == 3
    // urlPath = "/{store or item}/{ID}｜｜item/id(integer)

    return true;
  }

  private boolean isPostUrlValid(String[] urlParts) {
    if (urlParts.length < 7) {
      msg = "404: missing some parameters.";
      return false;
    }

    try {
      Integer.parseInt(urlParts[2]);
    } catch (NumberFormatException e) {
      msg = "store ID should be an integer";
      return false;
    } try {
      Integer.parseInt(urlParts[4]);
    } catch (NumberFormatException e) {
      msg = "customer ID should be an integer";
      return false;
    } try {
      Integer.parseInt(urlParts[6]); // might improve YYYYMMDD
      if (String.valueOf(urlParts[6]).length() != 8) {
        msg = "date must be of length 8";
        return false;
      }
    } catch (NumberFormatException e) {
      msg = "date should be an Integer of length 8";
      return false;
    }
    // urlPath = "/purchase/{storeID}/customer/{custID}/date/{date}"
    // urlParts = [, purchase, storeID (Integer), customer, custID(Integer), date, date(YYYYMMDD)]
    // len == 7
    // the first is empty
    // TEST: http://localhost:8080/server_war/purchase/100/customer/108/date/20200101
//      return (urlParts[0].isEmpty() && urlParts[1].equals("purchase")
//          && urlParts[3].equals("customer") && urlParts[5].equals("date"));
    return true;
  }
}
