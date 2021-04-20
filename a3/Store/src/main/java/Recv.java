import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import com.google.gson.Gson;


public class Recv {
  private static final String EXCHANGE_NAME = "logs";
  private static final String HOST_NAME = "3.84.234.107";
  private static final String USERNAME = "user1";
  private static final String PASSWORD = "newpass";
  private final static String QUEUE_NAME = "storeQueue";
  private final static Boolean DURABLE = false;
  private final static int NUM_THREAD = 128;
  // 1. What are the **top 10 most purchased items** at Store N
  // 2. Which are the **top 5 stores for sales for item N**
  private static Map<Integer, Map<Integer, Integer>> items = new ConcurrentHashMap<>();
  private static Map<Integer, Map<Integer, Integer>> stores = new ConcurrentHashMap();

  public static void main(String[] argv) throws Exception {
    ConnectionFactory factory = new ConnectionFactory();
    factory.setHost(HOST_NAME);
    factory.setUsername(USERNAME);
    factory.setPassword(PASSWORD);
    final Connection connection = factory.newConnection();

//        Map<Integer, Map<Integer, Integer>> map1 = new HashMap<>();
//        // for items
//        Map<Integer, Map<Integer, Integer>> map2 = new HashMap<>();
//        Map<Integer, Integer> map3 = new HashMap<>();
//        Map<Integer, Integer> map4 = new HashMap<>();
//        Map<Integer, Integer> map5 = new HashMap<>();
//        map3.put(21, 5);
//        map3.put(100, 3);
//        map1.put(100, map3);
//        map4.put(100, 2);
//        map5.put(100, 2);
//        map2.put(100, map4);
//        map2.put(100, map5);
//        System.out.println(map1);
//        System.out.println(map2);
//
//        RPCServer server = new RPCServer(map1, map2, connection);
//        System.out.println(server.getTopResult(100, 5, map1));
//
//      }

    Runnable runnable = new Runnable() {
      @Override
      public void run() {
        try {
          Channel channel = connection.createChannel();
          channel.exchangeDeclare(EXCHANGE_NAME, "fanout", DURABLE);
          channel.queueDeclare(QUEUE_NAME, DURABLE, false, false, null);
          channel.queueBind(QUEUE_NAME, EXCHANGE_NAME, "");
          // max one message per receiver
          channel.basicQos(1);
          System.out.println(" [*] Thread waiting for messages. To exit press CTRL+C");

          DeliverCallback deliverCallback = (consumerTag, delivery) -> {
            String message = new String(delivery.getBody(), "UTF-8");
            channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
//            System.out.println(
//                "Callback thread ID = " + Thread.currentThread().getId() + " Received '" + message
//                    + "'");

            Gson gson = new Gson();
            // {"storeId":100,"customerId":108,"date":"20200101","items":{"items":[{"ItemID":"217","numberOfItems:":4},{"ItemID":"100","numberOfItems:":4}]}}
            Purchase newPurchase = gson.fromJson(message, Purchase.class);
//            System.out.println(newPurchase);
//            System.out.println(newPurchase.getItems().getItems());

            // add to the stores map  {storeId: {itemID: quantity}}
            Integer storeId = newPurchase.getStoreId();
            ArrayList<Item> newItems = newPurchase.getItems().getItems();
            if (stores.containsKey(storeId)) {
              // add to the current store, modify the existing map
              // go through the newItems list.
              // if existing, increment the value; if new, put it in
              Map<Integer, Integer> storeMap = stores.get(storeId);
              for (Item item : newItems) {
                if (storeMap.containsKey(item.getItemID())) {
                  storeMap.computeIfPresent(item.getItemID(),
                      (key, oldValue) -> Integer.valueOf(oldValue + item.getNumberOfItems()));
                } else {
                  storeMap.put(item.getItemID(), item.getNumberOfItems());
                }
              }
            } else {
              // if store doesn't exist yet, create a map, add all the items in
              Map<Integer, Integer> itemsInStore = new ConcurrentHashMap<>();
              for (Item item : newItems) {
                itemsInStore.put(item.getItemID(), item.getNumberOfItems());
                stores.put(storeId, itemsInStore);
              }
            }

            // add to the items map {itemId: {storeID: sales}}
            // go through the list of newItems
            for (Item item : newItems) {
              Integer itemId = item.getItemID();
              // check if the current itemId is in the map or not
              if (items.containsKey(itemId)) {
                // get the map of stores, check if storeId is in it
                Map<Integer, Integer> itemMap = items.get(itemId);
                // if in, increment the storeId value by 1
                if (itemMap.containsKey(storeId)) {
                  itemMap.computeIfPresent(storeId, (key, oldValue) -> ++oldValue);
                } else {
                  // if not, add it in, with the storeId and 1 as the value
                  itemMap.put(storeId, Integer.valueOf(1));
                }
                // if the current item is not in yet, add it in with the storeId and value 1
              } else {
                Map<Integer, Integer> storesInItem = new ConcurrentHashMap<>();
                storesInItem.put(storeId, Integer.valueOf(1));
                items.put(itemId, storesInItem);
              }
            }
//            System.out.println(stores);
//            System.out.println(items);
          };

          // process messages
          channel.basicConsume(QUEUE_NAME, false, deliverCallback, consumerTag -> { });
        } catch (IOException ex) {
          Logger.getLogger(Recv.class.getName()).log(Level.SEVERE, null, ex);
        }

        RPCServer rpcServer = null;
        try {
          rpcServer = new RPCServer(stores, items, connection);
        } catch (IOException e) {
          e.printStackTrace();
        }
        try {
          rpcServer.respond();
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    };
    // start threads and block to receive messages
    for (int i = 0; i < NUM_THREAD; i++) {
      Thread recv = new Thread(runnable);
      recv.start();
    }
  }

}
