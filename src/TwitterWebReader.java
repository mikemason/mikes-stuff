import java.io.*;
import java.net.Authenticator;
import java.net.MalformedURLException;
import java.net.PasswordAuthentication;
import java.net.URL;

public class TwitterWebReader extends TwitterReader {

    BufferedReader getJsonReader() throws IOException {

        Authenticator.setDefault(new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication("mikemasonca", "k5bt72h3gpng".toCharArray());
            }
        });
        URL url = new URL("https://stream.twitter.com/1/statuses/filter.json?locations=-179,-89,178,89");

        return new BufferedReader(new InputStreamReader(url.openStream()));
    }
}
