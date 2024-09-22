package edu.miu.bdt;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import edu.miu.bdt.USCities.City;
 
public class WeatherDataFetcherScheduled {
	private static final String SPOOL_DIR = "/home/cloudera/spooldir";
    private static final String API_KEY = "7f4d2a03feffcb8acc6417115de43eac";
    private static final String OUTPUT_FILE = "weather_data.txt";
    private static final long FETCH_INTERVAL = 5 * 60 * 1000; // 5 minutes in milliseconds
 
    public static void main(String[] args) {
        while (true) {
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(OUTPUT_FILE, true))) {
                for (City city : USCities.cities) {
                    String weatherData = fetchWeatherData(city);
                    if (weatherData != null) {
                        writer.write(weatherData);
                        writer.newLine(); // Ensure each JSON record is on a new line
                        System.out.println("Fetched weather data for: " + city.name + " data=" + weatherData);
                    }
                    // Sleep to respect API rate limits
                    Thread.sleep(1000); // 1 second delay between requests
                }
                writer.flush();
                System.out.println("Completed a full cycle of data fetching.");
                System.out.println("Moving weather_data.txt to spooldir further processing by Flume.");
                String newFileName = "weather_data_" + System.currentTimeMillis() + ".txt";
                Files.move(Paths.get("weather_data.txt"), Paths.get(SPOOL_DIR, newFileName), StandardCopyOption.REPLACE_EXISTING);
            } catch (Exception e) {
                e.printStackTrace();
            }
 
            try {
                Thread.sleep(FETCH_INTERVAL);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
 
    private static String fetchWeatherData(City city) {
    	String apiUrl = String.format(
    			"https://api.openweathermap.org/data/2.5/weather?lat=%.4f&lon=%.4f&appid=%s&units=imperial",
    			city.lat, city.lon, API_KEY
    	);
    	try {    		
		    URL url = new URL(apiUrl);
		    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		    conn.setRequestMethod("GET");
		    int responseCode = conn.getResponseCode();
		    if (responseCode != 200) {
		      System.err.println("Failed to fetch data for " + city.name + ": " + responseCode);
		      return null;
		    }
		    BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
		    String inputLine;
		    StringBuilder content = new StringBuilder();
		    while ((inputLine = in.readLine()) != null) {
		      content.append(inputLine);
		    }
		    in.close();
		    return content.toString();
    	} catch (Exception e) {
    		System.err.println("Exception while fetching data for " + city.name);
    		e.printStackTrace();
    		return null;
    	}
    }
}
