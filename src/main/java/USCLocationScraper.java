import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.lang.reflect.Type;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.gson.JsonSyntaxException;


public class USCLocationScraper {

    // holds location data
    public static class LocationData {
        String code;
        String name;
        String address;
        double lat = 0.0; // default 0
        double lng = 0.0;

        // constructor
        public LocationData(String code, String name, String address, double lat, double lng) {
            this.code = code;
            this.name = name;
            this.address = address;
            this.lat = lat;
            this.lng = lng;
        }

        // default constructor for gson
        public LocationData() {}

        @Override
        public String toString() {
            // simple string representation
            return String.format("%s: %s @ (%f, %f)", code, address, lat, lng);
        }
    }


    // db stuff - check these match setup
    private static final String DB_URL = "jdbc:mysql://localhost/trojanMapsDB";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "Philbert1108";
    private static final String DB_TABLE = "locations";
    private static final String COL_CODE = "location_code";
    private static final String COL_NAME = "location_name";
    private static final String COL_ADDRESS = "address";
    private static final String COL_LAT = "latitude";
    private static final String COL_LON = "longitude";

    // path to json file
    private static final String JSON_FILE_PATH = "uscmap_backend_structures/MAP/usc_buildings_with_coordinates.json";


    /**
     * reads location data from json file.
     */
    public static List<LocationData> readLocationsFromJson(String jsonFilePath) {
        List<LocationData> locations = new ArrayList<>();
        System.out.println("reading locations from: " + jsonFilePath);

        try {
            String jsonContent = new String(Files.readAllBytes(Paths.get(jsonFilePath)), StandardCharsets.UTF_8);
            Gson gson = new Gson();
            Type listType = new TypeToken<ArrayList<LocationData>>(){}.getType();
            locations = gson.fromJson(jsonContent, listType);

            if (locations == null) {
                locations = new ArrayList<>(); // ensure list not null
                System.err.println("warn: json parsing resulted in null list.");
            } else {
                 // filter out entries with missing data
                 locations.removeIf(loc -> loc.code == null || loc.code.trim().isEmpty() || loc.address == null || loc.address.trim().isEmpty());
                 System.out.println("read " + locations.size() + " valid locations from json.");
            }

        } catch (IOException e) {
            System.err.println("error reading json file '" + jsonFilePath + "': " + e.getMessage());
        } catch (JsonSyntaxException e) {
            System.err.println("error parsing json file '" + jsonFilePath + "': " + e.getMessage());
        } catch (Exception e) { // catch any other errors
            System.err.println("unexpected error reading/parsing json: " + e.getMessage());
        }

        return locations;
    }


    /**
     * saves locations to db.
     * might insert duplicates if run multiple times without db constraints.
     * uses 'lat' and 'lng' from LocationData.
     */
    public static void storeLocationsInDB(List<LocationData> locations) {
        if (locations == null || locations.isEmpty()) {
            System.out.println("no locations to store in db.");
            return;
        }

        // simplified insert statement
        String insertSql = String.format("INSERT INTO %s (%s, %s, %s, %s, %s) VALUES (?, ?, ?, ?, ?)",
                                         DB_TABLE, COL_CODE, COL_NAME, COL_ADDRESS, COL_LAT, COL_LON);

        int insertedCount = 0, skippedCount = 0, errorCount = 0;
        System.out.println("storing " + locations.size() + " locations in db...");

        // use try-with-resources for connection
        try (Connection dbConnection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement insertStmt = dbConnection.prepareStatement(insertSql)) {

            // no transaction/batching, insert one by one

            for (LocationData loc : locations) {
                // check if essential data is present
                if (loc.code == null || loc.code.trim().isEmpty() || loc.address == null || loc.address.trim().isEmpty()) {
                    // System.out.println("skipping record due to missing code or address."); // less verbose
                    skippedCount++;
                    continue;
                }

                // simple check for coordinates (not exactly 0,0)
                if (Math.abs(loc.lat) > 0.0001 || Math.abs(loc.lng) > 0.0001) {
                    try {
                        insertStmt.setString(1, loc.code);
                        insertStmt.setString(2, loc.name != null ? loc.name : ""); // handle null names
                        insertStmt.setString(3, loc.address);
                        insertStmt.setDouble(4, loc.lat);   // use loc.lat
                        insertStmt.setDouble(5, loc.lng);   // use loc.lng
                        int result = insertStmt.executeUpdate(); // execute insert directly
                        if (result > 0) {
                            insertedCount++;
                        } else {
                            // this case might not happen often with simple inserts unless 0 rows affected is possible
                            errorCount++;
                        }
                    } catch (SQLException e) {
                         System.err.println("db insert error for " + loc.code + ": " + e.getMessage());
                         errorCount++;
                    }
                } else {
                    // System.out.println("skipping record with (0,0) coordinates: " + loc.code); // less verbose
                    skippedCount++; // skip non-geocoded or (0,0) coordinates
                }
            } // end loop

        } catch (SQLException e) {
            System.err.println("database connection error: " + e.getMessage());
            // error count might be off if connection fails mid-way
            // e.printStackTrace();
        }

        System.out.printf("db storage done. inserted: %d, skipped: %d, errors: %d%n",
                          insertedCount, skippedCount, errorCount);
    }


    /**
     * main entry point. uses json reading.
     */
    public static void main(String[] args) {
        System.out.println("--- usc location json loader --- start ---");
        long start = System.currentTimeMillis(); // simpler var name

        // load locations from json
        List<LocationData> locations = readLocationsFromJson(JSON_FILE_PATH); // simpler var name

        if (locations != null && !locations.isEmpty()) {
            // store locations in db
            storeLocationsInDB(locations);
        } else {
             System.out.println("no locations loaded from json, skipping db storage.");
        }

        System.out.printf("--- usc location json loader --- done (%d ms) ---%n", (System.currentTimeMillis() - start));
    }
}
