package main.java; // Your package

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.io.BufferedReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.ResultSet;

public class USCLocationScraper {

    // Holds the location data
    public static class LocationData {
        String code;
        String name;
        String address;
        double latitude = 0.0; // Default to 0
        double longitude = 0.0;

        public LocationData(String code, String name, String address) {
            this.code = code;
            this.name = name;
            this.address = address;
        }

        @Override
        public String toString() {
            // Simple string representation
            return String.format("%s: %s @ (%f, %f)", code, address, latitude, longitude);
        }
    }

    // --- Config --- 
    // !! CHANGE THESE !!
    private static final String USC_DIRECTORY_URL = "YOUR_USC_DIRECTORY_URL"; // page with the building list idk which
    private static final String GOOGLE_API_KEY = "YOUR_GOOGLE_GEOCODING_API_KEY"; //the Google API key
    private static final String GEOCODING_API_URL = "https://maps.googleapis.com/maps/api/geocode/json?";

    // DB stuff - check these match the setup
    private static final String DB_URL = "jdbc:mysql://localhost:3306/lab_09";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "root"; 
    private static final String DB_TABLE = "Location";
    private static final String COL_CODE = "location_code";
    private static final String COL_NAME = "location_name";
    private static final String COL_ADDRESS = "address";
    private static final String COL_LAT = "latitude";
    private static final String COL_LON = "longitude";


    /**
     * Grabs location data from the USC site.
     *  MUST change the CSS selectors in here !!
     */
    public static List<LocationData> scrapeLocations() {
        List<LocationData> locations = new ArrayList<>();
        if (USC_DIRECTORY_URL == null || USC_DIRECTORY_URL.startsWith("YOUR_")) {
             System.err.println("ERROR: USC Directory URL not set.");
             return locations;
        }
        System.out.println("Scraping: " + USC_DIRECTORY_URL);

        try {
            Document doc = Jsoup.connect(USC_DIRECTORY_URL)
                                .userAgent("Mozilla/5.0 Chrome/90") // Shorter user agent
                                .timeout(10000) // 10 sec timeout
                                .get();

            // !!! NEED CHANGE CSS SELECTORS TO MATCH THE SITE !!!
            // Example: table rows (skip header)
            Elements rows = doc.select("table#directoryTable tr:gt(0)"); // Adjust!

            if (rows.isEmpty()) {
                 System.out.println("WARN: No rows found with selector. Check URL/selector.");
            }

            for (Element row : rows) {
                // Get cells from the row - adjust indices!
                Elements cells = row.select("td");
                if (cells.size() > 2) { // Need at least 3 cells
                    String code = cells.get(0).text().trim();
                    String name = cells.get(1).text().trim();
                    String address = cells.get(2).text().trim();

                    if (!code.isEmpty() && !address.isEmpty()) { // Name can sometimes be empty maybe?
                        locations.add(new LocationData(code, name, address));
                    } else {
                         // System.out.println("Skipping row - empty code/address: " + row.text());
                    }
                } else {
                     // System.out.println("Skipping row - not enough cells: " + row.text());
                }
            }

        } catch (IOException e) {
            System.err.println("Scraping IO Error: " + e.getMessage());
        } catch (Exception e) {
             System.err.println("Scraping Error: " + e.getMessage());
             e.printStackTrace(); // Show stack trace for unexpected errors
        }

        System.out.println("Scraped " + locations.size() + " locations.");
        return locations;
    }

    /**
     * Tries to get lat/lon for addresses using Google Geocoding.
     * Needs GOOGLE_API_KEY to be set.
     */
    public static void geocodeLocations(List<LocationData> locations) {
        if (GOOGLE_API_KEY == null || GOOGLE_API_KEY.startsWith("YOUR_")) {
            System.err.println("ERROR: Google API Key missing. Geocoding skipped.");
            return;
        }
        if (locations == null || locations.isEmpty()) return; // Nothing to do

        System.out.println("Geocoding " + locations.size() + " locations...");
        int successCount = 0, failCount = 0;
        long startTime = System.currentTimeMillis();

        for (LocationData loc : locations) {
            HttpURLConnection conn = null; // Declare outside try for finally block
            try {
                String fullAddress = loc.address;
                // Add city/state if it looks like it's missing
                if (!fullAddress.matches(".*\b(CA|California)\b.*\d{5}.*")) {
                     fullAddress += ", Los Angeles, CA";
                }

                String encodedAddress = URLEncoder.encode(fullAddress, StandardCharsets.UTF_8.toString());
                URL url = new URL(GEOCODING_API_URL + "address=" + encodedAddress + "&key=" + GOOGLE_API_KEY);

                conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setConnectTimeout(5000);
                conn.setReadTimeout(5000);

                int responseCode = conn.getResponseCode();
                StringBuilder response = new StringBuilder();

                // Read response (either OK or error stream)
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                        responseCode == HttpURLConnection.HTTP_OK ? conn.getInputStream() : conn.getErrorStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                }

                if (responseCode == HttpURLConnection.HTTP_OK) {
                    JSONObject jsonResponse = new JSONObject(response.toString());
                    String status = jsonResponse.optString("status");

                    if ("OK".equals(status)) {
                        JSONArray results = jsonResponse.optJSONArray("results");
                        if (results != null && results.length() > 0) {
                            JSONObject location = results.optJSONObject(0)
                                                         .optJSONObject("geometry")
                                                         .optJSONObject("location");
                            if (location != null) {
                                loc.latitude = location.optDouble("lat"); // Defaults to 0.0 if missing
                                loc.longitude = location.optDouble("lng");
                                // Slightly different check than before
                                if (Math.abs(loc.latitude) > 0.0001 || Math.abs(loc.longitude) > 0.0001) {
                                     successCount++;
                                } else {
                                     System.out.println("WARN: Geocoded to (0,0) for: " + loc.address);
                                     failCount++;
                                }
                            } else { failCount++; System.out.println("WARN: No location geometry for: " + loc.address); }
                        } else { failCount++; System.out.println("WARN: No results array for: " + loc.address); }
                    } else {
                        failCount++;
                        System.out.println("API Error: " + status + " for: " + loc.address + " - " + jsonResponse.optString("error_message"));
                    }
                } else {
                    failCount++;
                    System.out.println("HTTP Error: " + responseCode + " for: " + loc.address + " Response: " + response.substring(0, Math.min(response.length(), 100))); // Show partial error
                }

                Thread.sleep(55); // slightly different delay

            } catch (IOException | JSONException | InterruptedException e) {
                failCount++;
                System.err.println("Geocoding Error for " + loc.address + ": " + e.getClass().getSimpleName() + " - " + e.getMessage());
                if (e instanceof InterruptedException) Thread.currentThread().interrupt();
            } catch (Exception e) { // Catch any other unexpected stuff
                 failCount++;
                 System.err.println("Unexpected Geocoding error for " + loc.address);
                 e.printStackTrace();
            } finally {
                 if (conn != null) conn.disconnect(); // Make sure connection is closed
            }
        } // End loop

         System.out.printf("Geocoding done (%d ms). Success: %d, Failed: %d%n",
                           (System.currentTimeMillis() - startTime), successCount, failCount);
    }


    /**
     * Saves the locations to the database.
     * Skips entries that already exist (by code or address).
     */
    public static void storeLocationsInDB(List<LocationData> locations) {
        if (locations == null || locations.isEmpty()) return;

        String checkSql = String.format("SELECT 1 FROM %s WHERE %s = ? OR %s = ? LIMIT 1", DB_TABLE, COL_CODE, COL_ADDRESS);
        String insertSql = String.format("INSERT INTO %s (%s, %s, %s, %s, %s) VALUES (?, ?, ?, ?, ?)",
                                         DB_TABLE, COL_CODE, COL_NAME, COL_ADDRESS, COL_LAT, COL_LON);

        int insertedCount = 0, skippedCount = 0, errorCount = 0;
        System.out.println("Storing " + locations.size() + " locations in DB...");

        // Use try-with-resources for DB connection and statements
        try (Connection dbConnection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD); // Renamed variable
             PreparedStatement checkStmt = dbConnection.prepareStatement(checkSql);
             PreparedStatement insertStmt = dbConnection.prepareStatement(insertSql)) {

            dbConnection.setAutoCommit(false); // Use transaction

            for (LocationData loc : locations) {
                // Only save if geocoded (lat/lon not 0.0 - adjust if 0,0 is valid)
                if (loc.latitude != 0.0 || loc.longitude != 0.0) {
                    boolean exists = false;
                    // Set parameters BEFORE executing the query
                    checkStmt.setString(1, loc.code);
                    checkStmt.setString(2, loc.address);
                    try (ResultSet rs = checkStmt.executeQuery()) { // Check existence
                         exists = rs.next(); // See if we get any result
                    } catch (SQLException e) {
                        System.err.println("DB Check Error for " + loc.code + ": " + e.getMessage());
                        errorCount++;
                        continue; // Skip this record on check error
                    }

                    if (exists) {
                        skippedCount++;
                    } else {
                        // Add to batch
                        try {
                            insertStmt.setString(1, loc.code);
                            insertStmt.setString(2, loc.name);
                            insertStmt.setString(3, loc.address);
                            insertStmt.setDouble(4, loc.latitude);
                            insertStmt.setDouble(5, loc.longitude);
                            insertStmt.addBatch();
                        } catch (SQLException e) {
                             System.err.println("DB Batch Add Error for " + loc.code + ": " + e.getMessage());
                             errorCount++;
                             // Don't add this one to the batch
                        }
                    }
                } else {
                    skippedCount++; // Skip non-geocoded
                }
            } // End loop

            // Execute batch
            System.out.println("Executing DB batch...");
            int[] batchResults = insertStmt.executeBatch();
            dbConnection.commit(); // Commit transaction

            for (int result : batchResults) {
                if (result >= 0 || result == PreparedStatement.SUCCESS_NO_INFO) insertedCount++;
                else errorCount++; // Count failed batch items as errors
            }
            System.out.println("Batch finished.");

        } catch (SQLException e) {
            System.err.println("Database Connection/Transaction Error: " + e.getMessage());
            errorCount += locations.size() - skippedCount - insertedCount; // Estimate remaining as errors
            e.printStackTrace();
            // Rollback is implicit with try-with-resources closing on exception if autoCommit is false
        }

        System.out.printf("DB storage done. Inserted: %d, Skipped: %d, Errors: %d%n",
                          insertedCount, skippedCount, errorCount);
    }


    /**
     * Main entry point.
     */
    public static void main(String[] args) {
        System.out.println("--- USC Location Scraper --- START ---");
        long globalStart = System.currentTimeMillis();

        List<LocationData> scrapedLocations = scrapeLocations();
        if (!scrapedLocations.isEmpty()) {
            geocodeLocations(scrapedLocations);
            storeLocationsInDB(scrapedLocations);
        } else {
             System.out.println("Nothing scraped, skipping geocoding and DB storage.");
        }

        System.out.printf("--- USC Location Scraper --- DONE (%d ms) ---%n", (System.currentTimeMillis() - globalStart));
    }
}
