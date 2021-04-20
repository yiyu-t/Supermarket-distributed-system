public class RecordData {
  private long startTime;
  private String requestType;
  private long latency;
  private int statusCode;

  public RecordData(long startTime, String requestType, long latency, int statusCode) {
    this.startTime = startTime;
    this.requestType = requestType;
    this.latency = latency;
    this.statusCode = statusCode;
  }

  public long getStartTime() {
    return startTime;
  }

  public String getRequestType() {
    return requestType;
  }

  public long getLatency() {
    return latency;
  }

  public int getStatusCode() {
    return statusCode;
  }
}
