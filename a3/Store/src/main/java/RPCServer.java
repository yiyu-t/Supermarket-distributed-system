import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DeliverCallback;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import com.rabbitmq.client.Connection;
import java.util.PriorityQueue;

public class RPCServer {
  private Map<Integer, Map<Integer, Integer>> data1;
  private Map<Integer, Map<Integer, Integer>> data2;
  private final static String RPC_QUEUE = "rpc_queue";
  private final static Boolean DURABLE = false;
  private final int PERSISTENT = 1;
  private final Connection conn;

  public RPCServer(
      Map<Integer, Map<Integer, Integer>> data1,  Map<Integer, Map<Integer, Integer>> data2, Connection conn) throws IOException {
    this.data1 = data1;
    this.data2 = data2;
    this.conn = conn;
  }

  public void respond() throws IOException {
    try {
      final Channel channel = conn.createChannel();
      // consider changing the second parameter (durable) to true
      channel.queueDeclare(RPC_QUEUE, DURABLE, false, false, null);
      channel.queuePurge(RPC_QUEUE);
      channel.basicQos(1);

      System.out.println(" [x] Awaiting RPC requests");

      Object monitor = new Object();
      DeliverCallback deliverCallback = (consumerTag, delivery) -> {
        AMQP.BasicProperties replyProps = new BasicProperties
            .Builder()
            .correlationId(delivery.getProperties().getCorrelationId())
            .deliveryMode(PERSISTENT)
            .build();
        String response = "";

        try {
          String message = new String(delivery.getBody(), "UTF-8");
          System.out.println(message);
          // get the URL
          String[] urlParts = message.split("/");
          // "urlParts looks like this: /store/id(Int)"
          Integer ID = Integer.parseInt(urlParts[2]);
          if (urlParts[1].equals("store")) {
            // call get the top 10 item for the store
            // use data1
            response += "top 10 items for sotre" + ID + ": ";
            response += getTopResult(ID, 10, data1);
          } else {
            // get the top 5 stores for the item
            // use data2
            response += "top 5 stores for item" + ID + ": ";
            response += getTopResult(ID, 5, data2);
            System.out.println(response);
          }
          // null pointer exception from the server
          System.out.println(response);
        } catch (Exception e) {
          System.out.println(" [.] " + e.toString());
          channel.basicReject(delivery.getEnvelope().getDeliveryTag(), true);
        } finally {
          channel.basicPublish("", delivery.getProperties().getReplyTo(), replyProps,
              response.getBytes("UTF-8"));
          channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
          // RabbitMq consumer worker thread notifies the RPC server owner thread
          synchronized (monitor) {
            monitor.notify();
          }
        }
      };
      channel.basicConsume(RPC_QUEUE, false, deliverCallback, (consumerTag -> { }));
      // Wait and be prepared to consume the message from RPC client.
      while (true) {
        synchronized (monitor) {
          try {
            monitor.wait();
          } catch (InterruptedException e) {
            e.printStackTrace();
          }
        }
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
   }

  // min heap
  public String getTopResult(int ID, int topN, Map<Integer, Map<Integer, Integer>> data) {
    Map<Integer, Integer> map = data.get(ID);

//    System.out.println(map + "1");
    PriorityQueue<Map<Integer, Integer>> topResults = new PriorityQueue<Map<Integer, Integer>>(topN, new DataComparator());
    for (Map.Entry<Integer, Integer> entry : map.entrySet()) {
      if (topResults.size() < topN) {
        Map<Integer, Integer> tempMap = new HashMap();
        tempMap.put(entry.getKey(), entry.getValue());
        topResults.offer(tempMap);
      } else {
        Map<Integer, Integer> newMap = new HashMap<>();
        newMap.put(entry.getKey(), entry.getValue());
        assert topResults.peek() != null;
        HashMap<Integer, Integer> top = (HashMap) topResults.peek();

        Integer v1 = null;
        Integer v2 = null;
        for (Map.Entry<Integer, Integer> pair1 : top.entrySet()) {
          v1 = pair1.getValue();
        }
        for (Map.Entry<Integer, Integer> pair2 : newMap.entrySet()) {
          v2 = pair2.getValue();
        }
        if (v1 < v2) {
          topResults.remove();
          topResults.offer(newMap);
        }
      }

    }
    System.out.println(topResults);

    StringBuilder res = new StringBuilder();
    Iterator iterator = topResults.iterator();
    while (iterator.hasNext()) {
      Map<Integer, Integer> resultMap = (Map<Integer, Integer>) iterator.next();
      for (Map.Entry<Integer, Integer> pair1 : resultMap.entrySet()) {
        res.append("{id: " + pair1.getKey() + System.lineSeparator());
        res.append("quantity: " + pair1.getValue() + "}" + System.lineSeparator());
      }
    }
    return res.toString();
  }

}

