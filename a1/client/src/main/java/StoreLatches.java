import java.util.concurrent.CountDownLatch;

public class StoreLatches {
  protected CountDownLatch latch1;
  protected CountDownLatch latch2;
  protected CountDownLatch latchAll;

  public StoreLatches(CountDownLatch latch1, CountDownLatch latch2,
      CountDownLatch latchAll) {
    this.latch1 = latch1;
    this.latch2 = latch2;
    this.latchAll = latchAll;
  }
}
