import com.lmax.disruptor.*;
import com.lmax.disruptor.dsl.Disruptor;
import java.io.IOException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class Main {
    private static final int RING_SIZE = 8192;
    private static final int THREAD_POOL_INITIAL_SIZE = 4;
    private static final int THREAD_POOL_MAX_SIZE = 4;
    private static final int ARRAY_BLOCKING_QUEUE_SIZE = 8;

    static long happyAmericaCount = 0;
    static long sadAmericaCount = 0;
    static long happyEuropeCount = 0;
    static long sadEuropeCount = 0;

    public static void main(String[] args) throws InterruptedException, IOException {

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

        ThreadPoolExecutor executor = new ThreadPoolExecutor(
                THREAD_POOL_INITIAL_SIZE, THREAD_POOL_MAX_SIZE,
                5, TimeUnit.SECONDS,
                new ArrayBlockingQueue<Runnable>(ARRAY_BLOCKING_QUEUE_SIZE));

        Disruptor<TweetEvent> disruptor =
                new Disruptor<TweetEvent>(TweetEvent.EVENT_FACTORY, executor,
                        new SingleThreadedClaimStrategy(RING_SIZE), new SleepingWaitStrategy());

        //noinspection unchecked
        disruptor.handleEventsWith(HappinessDetector.moodComputingHandler, geoLocationHandler)
                .then(moodTrackingHandler)
                .then(outputDisplayHandler);
        final RingBuffer<TweetEvent> ringBuffer = disruptor.start();

        TwitterFileReader.publishLotsOfEvents(ringBuffer);

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
}

