package edu.miu.bdt;

public class USCities {
	 public static final City[] cities = {
        new City("New York City", 40.7128, -74.0060),
        new City("Los Angeles", 34.0522, -118.2437),
        new City("Chicago", 41.8781, -87.6298),
        new City("Houston", 29.7604, -95.3698),
        new City("Phoenix", 33.4484, -112.0740),
        new City("Philadelphia", 39.9526, -75.1652),
        new City("San Antonio", 29.4241, -98.4936),
        new City("San Diego", 32.7157, -117.1611),
        new City("Dallas", 32.7767, -96.7970),
        new City("San Jose", 37.3382, -121.8863),
        new City("Austin", 30.2672, -97.7431),
        new City("Jacksonville", 30.3322, -81.6557),
        new City("Fort Worth", 32.7555, -97.3308),
        new City("Columbus", 39.9612, -82.9988),
        new City("Indianapolis", 39.7684, -86.1581),
        new City("Charlotte", 35.2271, -80.8431),
        new City("San Francisco", 37.7749, -122.4194),
        new City("Seattle", 47.6062, -122.3321),
        new City("Denver", 39.7392, -104.9903),
        new City("Washington, D.C.", 38.9072, -77.0369)
    };
	 
	static class City {
		String name;
	    double lat;
	    double lon;
	 
	    City(String name, double lat, double lon) {
	    	this.name = name;
	        this.lat = lat;
	        this.lon = lon;
	    }
	}
}
