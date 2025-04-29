
public class Location {
    private int locationID;
    private String name;
    private String category;
    private String address;
    private double latitude;
    private double longitude;
    
    public Location(int locationID, String name, String category, String address, double latitude, double longitude) {
        this.locationID = locationID;
        this.name = name;
        this.category = category;
        this.address = address;
        this.latitude = latitude;
        this.longitude = longitude;
    }
    
    public int getLocationID() {
        return locationID;
        }

    public String getName() {
        return name;
        }

    public String getCategory() {
        return category;
        }

    public String getAddress() {
        return address;
        }

    public double getLatitude() {
        return latitude;
        }

    public double getLongitude() {
        return longitude;
        }
} 