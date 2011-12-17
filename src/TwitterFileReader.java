import com.lmax.disruptor.RingBuffer;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.ObjectMapper;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class TwitterFileReader {
    static void publishLotsOfEvents(RingBuffer<TweetEvent> ringBuffer) throws IOException {

        ObjectMapper mapper = new ObjectMapper();

        BufferedReader br = new BufferedReader(new FileReader(new File("/Users/mgm/work/hack_day/TwitterStreamAdapter/data/world/world-geo-1.json")));
        String line = br.readLine();

        long start = System.currentTimeMillis();
        long tweetsProcessed = 0;
        while(line != null) {

            Tweet tweet;
            try {
                tweet = mapper.readValue(line, Tweet.class);
            } catch (JsonParseException e) {
                System.out.println("Warning, bad line in JSON: " + line);
                line = br.readLine();
                continue;
            }

            // Publishers claim events in sequence
            long sequence = ringBuffer.next();
            TweetEvent event = ringBuffer.get(sequence);

            event.clear();
            event.setId(tweet.getId());
            if(tweet.getGeo() != null) {
                final Tweet.Geo geo = tweet.getGeo();
                event.setGeo(new Point(geo.getCoordinates()[0], geo.getCoordinates()[1]));
            }
            event.setText(tweet.getText());

            // make the event available to EventProcessors
            ringBuffer.publish(sequence);
            tweetsProcessed++;

            line = br.readLine();
        }

        double secondsTaken = (double)((System.currentTimeMillis() - start)) / 1000.0;
        double tweetsPerSecond = (double) tweetsProcessed / secondsTaken;
        System.out.println("tweetsPerSecond = " + (int) tweetsPerSecond);
    }
}
