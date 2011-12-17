import com.lmax.disruptor.*;
import com.lmax.disruptor.dsl.Disruptor;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.ObjectMapper;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

public class Main {
    private static final int RING_SIZE = 8192;

    static long happyAmericaCount = 0;
    static long sadAmericaCount = 0;
    static long happyEuropeCount = 0;
    static long sadEuropeCount = 0;

    public static void main(String[] args) throws InterruptedException, IOException {

        final Pattern happy = Pattern.compile(".*(happy|love|cheerful|glad|gleeful|jolly|joyous|merry|overjoyed|pleased|thrilled|upbeat).*");
        final Pattern sad = Pattern.compile(".*(sad|cry|unhappy|depressed|dejected|despair|distressed|downcast|forlorn|gloomy|glum|heartbroken|melancholy|morbid|morose|pensive|somber|sorrowful|wistful).*");

        final EventHandler<TweetEvent> moodComputingHandler = new EventHandler<TweetEvent>() {

            public void onEvent(final TweetEvent event, final long sequence, final boolean endOfBatch) throws Exception {
                String text = event.getText();
                if(text == null) return;
                text = text.toLowerCase();

                if(happy.matcher(text).matches())
                    event.setHappy();
                if(sad.matcher(text).matches())
                    event.setSad();
            }
        };

        final EventHandler<TweetEvent> geoLocationHandler = new EventHandler<TweetEvent>() {

            public void onEvent(final TweetEvent event, final long sequence, final boolean endOfBatch) throws Exception {
                event.computeGeography();
            }
        };

        final EventHandler<TweetEvent> moodTrackingHandler = new EventHandler<TweetEvent>() {

            public void onEvent(final TweetEvent event, final long sequence, final boolean endOfBatch) throws Exception {
                if(event.isHappy() && event.isNorthAmerica())
                    happyAmericaCount++;
                else if(event.isHappy() && event.isEurope())
                    happyEuropeCount++;
                if(event.isSad() && event.isNorthAmerica())
                    sadAmericaCount++;
                else if(event.isSad() && event.isEurope())
                    sadEuropeCount++;
            }
        };

        final EventHandler<TweetEvent> outputDisplayHandler = new EventHandler<TweetEvent>() {
            public void onEvent(final TweetEvent event, final long sequence, final boolean endOfBatch) throws Exception {
//                if(event.getId() % 100000 == 0L) {
//                    displayCounts();
//                }
            }
        };

        ThreadPoolExecutor executor = new ThreadPoolExecutor(4, 4, 5, TimeUnit.SECONDS, new ArrayBlockingQueue<Runnable>(8));

        Disruptor<TweetEvent> disruptor =
                new Disruptor<TweetEvent>(TweetEvent.EVENT_FACTORY, executor,
                        new SingleThreadedClaimStrategy(RING_SIZE), new SleepingWaitStrategy());

        //noinspection unchecked
        disruptor.handleEventsWith(moodComputingHandler, geoLocationHandler)
                .then(moodTrackingHandler)
                .then(outputDisplayHandler);
        final RingBuffer<TweetEvent> ringBuffer = disruptor.start();

        publishLotsOfEvents(ringBuffer);

        Thread.sleep(500);
        System.out.println("Shutting down...");
        disruptor.halt();
        executor.shutdown();
        executor.awaitTermination(5, TimeUnit.SECONDS);
        displayCounts();
    }

    private static void displayCounts() {
        System.out.println("Happy America: " + happyAmericaCount + "\tSad America: " + sadAmericaCount);
        System.out.println("Happy Europe:  " + happyEuropeCount +  "\tSad Europe:  " + sadEuropeCount);
        System.out.println("America is " + (100 * happyAmericaCount) / (happyAmericaCount + sadAmericaCount) + "% happy.");
        System.out.println("Europe is  " + (100 * happyEuropeCount) / (happyEuropeCount + sadEuropeCount) + "% happy.");
    }

    private static void publishLotsOfEvents(RingBuffer<TweetEvent> ringBuffer) throws IOException {

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
//            System.out.println("tweet = " + tweet);

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

