package cs1302.api;

public class OpenMeteoResponse {
     double latitude;
    double longitude;
    String generationtime_ms;
    String utc_offset_seconds;
    String timezone;
    String timezone_abbreviation;
    double elevation;
    Hourly hourly;
    HourlyUnits hourly_units;
    double temperature;
    double windSpeed;
    double precipitation;

    static class Hourly {
        String[] time;
        double[] temperature_2m;  // Array of temperatures
        // Add other hourly weather variables as needed
    }

    static class HourlyUnits {
        String temperature_2m;  // Unit for temperature
        // Add units for other hourly variables as needed
    }


}
