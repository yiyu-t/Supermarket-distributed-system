import java.io.BufferedReader;
import java.io.IOException;
import java.sql.SQLException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/StoreServer")
public class StoreServer extends HttpServlet {
  private String msg;

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
    if (!isUrlValid(urlParts)) {
      res.setStatus(HttpServletResponse.SC_NOT_FOUND);
      res.getWriter().write(msg);
      return;
    } else {
      // do any sophisticated processing with urlParts which contains all the url params
      int storeId = Integer.valueOf(urlParts[2]);
      int customerId = Integer.valueOf(urlParts[4]);
      String date = urlParts[6];
      String item = body;
      // create a Purchase POJO
      Purchase newPurchase = new Purchase(storeId, customerId, date, item);
      // Pass it to DAO
      try {
        PurchaseDao purchaseDao = new PurchaseDao();
        purchaseDao.createPurchase(newPurchase);
        res.getWriter().write(body);
        res.setStatus(HttpServletResponse.SC_CREATED);
      } catch (SQLException se){
        res.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        se.printStackTrace();
        res.getWriter().write("sql exception!");
      }
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
    if (!isUrlValid(urlParts)) {
      res.setStatus(HttpServletResponse.SC_NOT_FOUND);
      res.getWriter().write(msg);
    } else {
      res.setStatus(HttpServletResponse.SC_OK);
      // do any sophisticated processing with urlParts which contains all the url params
      // TODO: process url params in `urlParts`
      res.getWriter().write("200: Oh Hey! It works!!!!");
    }
  }

    private boolean isUrlValid(String[] urlParts) {
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
