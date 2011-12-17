import com.lmax.disruptor.EventHandler;

import java.util.regex.Pattern;

public class HappinessDetector {

    static final Pattern happy = Pattern.compile(".*(happy|love|cheerful|glad|gleeful|jolly|joyous|merry|overjoyed|pleased|thrilled|upbeat).*");
    static final Pattern sad = Pattern.compile(".*(sad|cry|unhappy|depressed|dejected|despair|distressed|downcast|forlorn|gloomy|glum|heartbroken|melancholy|morbid|morose|pensive|somber|sorrowful|wistful).*");

    public static final EventHandler<TweetEvent> moodComputingHandler = new EventHandler<TweetEvent>() {

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

}
