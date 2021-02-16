import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;

public class RecordWriter extends Thread {
  private BlockingQueue q;
  private String path;
  private BufferedWriter outputFile;
  private CountDownLatch latch;
  private boolean finished;

  public RecordWriter(BlockingQueue q, String path, CountDownLatch latch) throws IOException {
    this.q = q;
    this.path = path;
    this.latch = latch;
    this.outputFile = new BufferedWriter(new FileWriter(this.path));
    this.finished = false;
  }

  @Override
  public void run() {
    while (!this.finished) {
      try {
        this.consume(q.take());
      } catch (InterruptedException | IOException ex) {
        ex.printStackTrace();
      }
    }
  }

  public void consume(Object data) throws IOException {
    if (!(data instanceof RecordData)) {
      this.finished = true;
      latch.countDown();
      outputFile.close();
    } else {
      StringBuilder line = new StringBuilder();
      RecordData recordData = (RecordData) data;
      line.append(String.valueOf(recordData.getStartTime()) + ',');
      line.append(recordData.getRequestType() + ',');
      line.append(String.valueOf(recordData.getLatency()) + ',');
      line.append(recordData.getStatusCode());
      line.toString();
      outputFile.write(line + System.lineSeparator());
    }
  }

}
