package cs1302.api;

public class APIXUResponse {
     Location location;
    Current current;

    public static class Location {
        String name;
        String country;
        String region;
        String lat;
        String lon;
        String timezone_id;
        String localtime;
        long localtime_epoch;
        String utc_offset;
    }

    public static class Current {
        String observation_time;
        int temperature;
        String[] weather_icons;
        String[] weather_descriptions;
        int wind_speed;
        String wind_dir;
        int pressure;
        int precip;
        int humidity;
        int cloudcover;
        int feelslike;
        int uv_index;
        int visibility;
    }
}
