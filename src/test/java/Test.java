import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;

public class Test {

  @org.junit.Test
  public void test() {

    HttpURLConnection conn = null;
    InputStream in = null;
    URL url;
    try {
      url = new URL("https://buildserver.labs.intellij.net/trackerAnnounce.html?info_hash=8d%3D%1AhR%B0GA%DCmu%CD%10%86r_%B6%EC%FE&peer_id=-TO0042-dc345ac4bedc&port=6881&uploaded=0&downloaded=0&left=959686704&compact=1&no_peer_id=0&ip=172.20.240.135");
    } catch (Exception e) {
      System.out.println("cannot create url");
      e.printStackTrace();
      return;
    }

    try {
      System.out.println(url);
      conn = (HttpURLConnection) openConnectionCheckRedirects(url);
      System.out.println("ok, connection is initialized");
      in = conn.getInputStream();
    } catch (IOException ioe) {
      System.out.println("oops, exception");
      ioe.printStackTrace();
      if (conn != null) {
        in = conn.getErrorStream();
      }
    }

    // At this point if the input stream is null it means we have neither a
    // response body nor an error stream from the server. No point in going
    // any further.
    if (in == null) {
      System.out.println("input stream is null");
      return;
    }
    try {

      System.out.println(conn.getHeaderFields());

      byte[] data = new byte[in.available()];
      in.read(data);
      System.out.println(new String(data, StandardCharsets.UTF_8));

      System.out.println();
    } catch (IOException e) {
      e.printStackTrace();
    }


  }

  private URLConnection openConnectionCheckRedirects(URL url) throws IOException {
    boolean needRedirect;
    int redirects = 0;
    URLConnection connection = url.openConnection();
    do {
      needRedirect = false;
      connection.setConnectTimeout(10000);
      connection.setReadTimeout(10000);
      HttpURLConnection http = null;
      if (connection instanceof HttpURLConnection) {
        http = (HttpURLConnection) connection;
        http.setInstanceFollowRedirects(false);
      }
      if (http != null) {
        int stat = http.getResponseCode();
        if (stat >= 300 && stat <= 307 && stat != 306 &&
                stat != HttpURLConnection.HTTP_NOT_MODIFIED) {
          URL base = http.getURL();
          String newLocation = http.getHeaderField("Location");
          System.out.println("redirect, new location is " + newLocation);
          URL target = newLocation == null ? null : new URL(base, newLocation);
          http.disconnect();
          // Redirection should be allowed only for HTTP and HTTPS
          // and should be limited to 5 redirections at most.
          if (redirects >= 5) {
            throw new IOException("too many redirects");
          }
          if (target == null || !(target.getProtocol().equals("http")
                  || target.getProtocol().equals("https"))) {
            throw new IOException("illegal URL redirect or protocol");
          }
          needRedirect = true;
          connection = target.openConnection();
          redirects++;
        }
      }
    }
    while (needRedirect);
    return connection;
  }


}
