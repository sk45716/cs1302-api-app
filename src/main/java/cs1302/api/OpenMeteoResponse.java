package cs1302.api;

public class OpenMeteoResponse {
    double latitude;
    double longitude;
    String timezone;
    String timezone_abbreviation;
    Hourly hourly;
    Daily daily;  // Adding a Daily inner class for daily data

    static class Hourly {
        String[] time;  // Time points for the data
        double[] temperature_2m;  // Temperatures at each time point
        double[] windSpeed;  // Wind speed at each time point
        double[] precipitation;  // Precipitation at each time point
    }

    static class Daily {
        double[] temperature_2m_max;  // Maximum daily temperatures
        double[] temperature_2m_min;  // Minimum daily temperatures
    }
}
