import com.lmax.disruptor.EventFactory;

import java.util.Date;

/**
 * Disruptor event that encapsulates a Tweet
 */
public final class TweetEvent {
    private long id;
    private Date createdAt;
    private Point geo;
    private String text;
    private boolean isHappy;
    private boolean isSad;
    private boolean isNorthAmerica;
    private boolean isEurope;

    public TweetEvent() {
    }

    public TweetEvent(long id, Date createdAt, Point geo, String text) {
        this.id = id;
        this.createdAt = createdAt;
        this.geo = geo;
        this.text = text;
    }

    public void setId(long id) {
        this.id = id;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public void setGeo(Point geo) {
        this.geo = geo;
    }

    public void setText(String text) {
        this.text = text;
    }

    public long getId() {
        return id;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public Point getGeo() {
        return geo;
    }

    public String getText() {
        return text;
    }

    public boolean isHappy() {
        return isHappy;
    }

    public void setHappy() {
        isHappy = true;
    }

    public boolean isSad() {
        return isSad;
    }

    public void setSad() {
        isSad = true;
    }

    public void clear() {
        id = 0;
        geo = null;
        text = null;
        isHappy = false;
        isSad = false;
        isNorthAmerica = false;
        isEurope = false;
    }

    public boolean isNorthAmerica() {
        return isNorthAmerica;
    }

    public boolean isEurope() {
        return isEurope;
    }

    public final static EventFactory<TweetEvent> EVENT_FACTORY = new EventFactory<TweetEvent>()
    {
        public TweetEvent newInstance() {
            return new TweetEvent();
        }
    };

    public void computeGeography() {
        isNorthAmerica = geo != null && geo.isNorthAmerica();
        isEurope = geo != null && geo.isEurope();
    }
}

