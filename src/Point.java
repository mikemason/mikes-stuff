public class Point {
    private final double latitude;
    private final double longitude;

    public Point(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public boolean isNorthAmerica() {
        return latitude > 21 && latitude < 66 && longitude > -158 && longitude < -58;
    }

    public boolean isEurope() {
        return latitude > 37 && latitude < 58 && longitude > -8 && longitude < 28;
    }
}
