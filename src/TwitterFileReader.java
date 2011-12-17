import java.io.*;

public class TwitterFileReader extends TwitterReader {

    BufferedReader getJsonReader() throws FileNotFoundException {
        return new BufferedReader(new FileReader(new File("data/world/world-geo-sample.json")));
    }
}
