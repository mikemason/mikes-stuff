import com.lmax.disruptor.*;
import com.lmax.disruptor.dsl.Disruptor;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartFrame;
import org.jfree.chart.JFreeChart;
import org.jfree.data.general.DefaultPieDataset;

import java.io.IOException;
import java.util.Locale;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class Main {
    private static final int RING_SIZE = 8192;
    private static final int THREAD_POOL_INITIAL_SIZE = 8;
    private static final int THREAD_POOL_MAX_SIZE = 8;
    private static final int ARRAY_BLOCKING_QUEUE_SIZE = 8;

    static long happyAmericaCount = 0;
    static long sadAmericaCount = 0;
    static long happyEuropeCount = 0;
    static long sadEuropeCount = 0;
    private static ChartFrame chartFrame;

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

        final long[] nextTimeToPrintOutput = {0};
        final EventHandler<TweetEvent> outputDisplayHandler = new EventHandler<TweetEvent>() {
            public void onEvent(final TweetEvent event, final long sequence, final boolean endOfBatch) throws Exception {
                if(System.currentTimeMillis() > nextTimeToPrintOutput[0])
                {
                    displayCounts();
                    nextTimeToPrintOutput[0] = System.currentTimeMillis() + 5000;
                }
            }
        };

        ThreadPoolExecutor executor = new ThreadPoolExecutor(
                THREAD_POOL_INITIAL_SIZE, THREAD_POOL_MAX_SIZE,
                5, TimeUnit.SECONDS,
                new ArrayBlockingQueue<Runnable>(ARRAY_BLOCKING_QUEUE_SIZE));

        Disruptor<TweetEvent> disruptor =
                new Disruptor<TweetEvent>(TweetEvent.EVENT_FACTORY, executor,
                        new MultiThreadedClaimStrategy(RING_SIZE), new SleepingWaitStrategy());

        //noinspection unchecked
        disruptor.handleEventsWith(HappinessDetector.moodComputingHandler, geoLocationHandler)
                .then(moodTrackingHandler)
                .then(outputDisplayHandler);
        final RingBuffer<TweetEvent> ringBuffer = disruptor.start();

        new TwitterFileReader().publishLotsOfEvents(ringBuffer, 200000);
//        new TwitterWebReader().publishLotsOfEvents(ringBuffer, 10000000);

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
        long americaPercentage = happyAmericaCount + sadAmericaCount > 0 ?
                (100 * happyAmericaCount) / (happyAmericaCount + sadAmericaCount) : 100;
        long europePercentage = happyEuropeCount + sadEuropeCount > 0 ?
                (100 * happyEuropeCount) / (happyEuropeCount + sadEuropeCount) : 0;

        System.out.println("America is " + americaPercentage + "% happy.");
        System.out.println("Europe is  " + europePercentage + "% happy.");

        DefaultPieDataset data = new DefaultPieDataset();
        data.setValue("Happy American", happyAmericaCount);
        data.setValue("Sad American", sadAmericaCount);
        data.setValue("Happy European", happyEuropeCount);
        data.setValue("Sad European", sadEuropeCount);

        JFreeChart chart = ChartFactory.createPieChart("Happy People", data, true, false, Locale.getDefault());

        if(chartFrame != null)
            chartFrame.setVisible(false);

        chartFrame = new ChartFrame("Happiness", chart);
        chartFrame.pack();
        chartFrame.setVisible(true);
    }
}

