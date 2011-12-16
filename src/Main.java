import com.lmax.disruptor.*;
import com.lmax.disruptor.dsl.Disruptor;
import org.codehaus.jackson.map.ObjectMapper;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class Main {
    private static final int RING_SIZE = 8192;

    public static void main(String[] args) throws InterruptedException, IOException {

        final EventHandler<TweetEvent> moodTrackingHandler = new EventHandler<TweetEvent>() {
            public void onEvent(final TweetEvent event, final long sequence, final boolean endOfBatch) throws Exception {
                System.out.println("Processing Tweet ID " + event.getId());
            }
        };

        final EventHandler<TweetEvent> outputDisplayHandler = new EventHandler<TweetEvent>() {
            public void onEvent(final TweetEvent event, final long sequence, final boolean endOfBatch) throws Exception {
                System.out.println("Outputting Tweet ID " + event.getId());
            }
        };

        ThreadPoolExecutor executor = new ThreadPoolExecutor(2, 2, 5, TimeUnit.SECONDS, new ArrayBlockingQueue<Runnable>(8));

        Disruptor<TweetEvent> disruptor =
                new Disruptor<TweetEvent>(TweetEvent.EVENT_FACTORY, executor,
                        new SingleThreadedClaimStrategy(RING_SIZE), new SleepingWaitStrategy());

        disruptor.handleEventsWith(moodTrackingHandler).then(outputDisplayHandler);
        final RingBuffer<TweetEvent> ringBuffer = disruptor.start();

        publishLotsOfEvents(ringBuffer);

        Thread.sleep(500);
        System.out.println("Shutting down...");
        disruptor.halt();
        executor.shutdown();
        executor.awaitTermination(5, TimeUnit.SECONDS);
    }

    private static void publishLotsOfEvents(RingBuffer<TweetEvent> ringBuffer) throws IOException {

        ObjectMapper mapper = new ObjectMapper();

        BufferedReader br = new BufferedReader(new FileReader(new File("/Users/mgm/work/hack_day/TwitterStreamAdapter/data/world/world-geo-1.json")));
        String line = br.readLine();

        while(line != null) {

            Tweet tweet = mapper.readValue(line, Tweet.class);
            System.out.println("tweet = " + tweet);

            // Publishers claim events in sequence
            long sequence = ringBuffer.next();
            TweetEvent event = ringBuffer.get(sequence);

            event.setId(tweet.getId());
            if(tweet.getGeo() != null) {
                final Tweet.Geo geo = tweet.getGeo();
                event.setGeo(new Point(geo.getCoordinates()[0], geo.getCoordinates()[1]));
            }
            event.setText(tweet.getText());

            // make the event available to EventProcessors
            ringBuffer.publish(sequence);

            line = br.readLine();
        }

    }
}

