import org.codehaus.jackson.annotate.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown=true)
public class Tweet {
    private long id;
    private Geo geo;
    private String text;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Geo getGeo() {
        return geo;
    }

    public void setGeo(Geo geo) {
        this.geo = geo;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    @JsonIgnoreProperties(ignoreUnknown=true)
    public static class Geo {
        private String type;
        private double[] coordinates;

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public double[] getCoordinates() {
            return coordinates;
        }

        public void setCoordinates(double[] coordinates) {
            this.coordinates = coordinates;
        }

        @Override
        public String toString() {
            return "Geo{" +
                    "type='" + type + '\'' +
                    ", coordinates=" + prettyPrint(coordinates) +
                    '}';
        }

        private String prettyPrint(double[] coordinates) {
            StringBuffer sb = new StringBuffer(24);
            sb.append("{");
            for (int i = 0; i < coordinates.length; i++) {
                if(i > 0) sb.append(",");
                double coordinate = coordinates[i];
                sb.append(coordinate);
            }
            return sb.toString();
        }
    }

    @Override
    public String toString() {
        return "Tweet{" +
                "id=" + id +
                ", geo=" + geo +
                '}';
    }
}
