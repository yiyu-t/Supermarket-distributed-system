import com.google.gson.internal.$Gson$Types;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;
import java.io.IOException;
import java.sql.SQLException;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;
import com.google.gson.Gson;

public class Recv {
  private static final String EXCHANGE_NAME = "logs";
  private static final String HOST_NAME = "3.84.234.107";
  // private static final String DATABASE = "supermarket";
  private static final String USERNAME = "user1";
  private static final String PASSWORD = "newpass";
  private static Boolean DURABLE = false;
  //private static final String  VIRTUALHOST= "host1";
  private final static String QUEUE_NAME = "purchaseQueue";
  private final static int NUM_THREAD = 128;
  private final static PurchaseDao purchaseDao = new PurchaseDao();

  public static void main(String[] argv) throws Exception {
    ConnectionFactory factory = new ConnectionFactory();
    factory.setHost(HOST_NAME);
    factory.setUsername(USERNAME);
    factory.setPassword(PASSWORD);
    final Connection connection = factory.newConnection();

    Runnable runnable = new Runnable() {
      @Override
      public void run() {
        try {
          final Channel channel = connection.createChannel();
          channel.exchangeDeclare(EXCHANGE_NAME, "fanout", DURABLE);
          channel.queueDeclare(QUEUE_NAME, DURABLE, false, false, null);
          // channel.basicQos(1);
          channel.queueBind(QUEUE_NAME, EXCHANGE_NAME, "");
          // max one message per receiver

         // String queueName = channel.queueDeclare().getQueue();
          // channel.queueBind(queueName, EXCHANGE_NAME, "");
          System.out.println(" [*] Thread waiting for messages. To exit press CTRL+C");

          DeliverCallback deliverCallback = (consumerTag, delivery) -> {
            String message = new String(delivery.getBody(), "UTF-8");
            channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
            // System.out.println( "Callback thread ID = " + Thread.currentThread().getId() + " Received '" + message + "'");

            // convert message to Purchase object
            // Write a new Purchase object into database
            Gson gson = new Gson();
            // {"storeId":100,"customerId":108,"date":"20200101","items":{"items":[{"ItemID":"217","numberOfItems:":4},{"ItemID":"100","numberOfItems:":4}]}}
            Purchase newPurchase = gson.fromJson(message, Purchase.class);
            // System.out.println(newPurchase + " 1");

            // Pass it to DAO, write into database
            try {
              purchaseDao.createPurchase(newPurchase);
            } catch (SQLException se){
              System.out.println(se.fillInStackTrace());
            }
          };
          // process messages
          channel.basicConsume(QUEUE_NAME, false, deliverCallback, consumerTag -> { });
        } catch (IOException ex) {
          Logger.getLogger(Recv.class.getName()).log(Level.SEVERE, null, ex);
        }
      }
    };
    // start threads and block to receive messages
    for (int i = 0; i < NUM_THREAD; i++) {
      Thread recv = new Thread(runnable);
      recv.start();
    }
//    connection.close();
  }
}
