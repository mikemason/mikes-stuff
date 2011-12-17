import java.io.*;

public class TwitterFileReader extends TwitterReader {

    BufferedReader getJsonReader() throws FileNotFoundException {
        return new BufferedReader(new FileReader(new File("/Users/mgm/work/hack_day/TwitterStreamAdapter/data/world/world-geo-1.json")));
    }
}
