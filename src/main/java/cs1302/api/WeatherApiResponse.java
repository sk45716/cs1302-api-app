package cs1302.api;

import java.util.List;

/**
 * Represents a response from a weather API containing various weather details.
 */
public class WeatherApiResponse {
    private String temperature;
    private String wind;
    private String description;
    private List<Forecast> forecast;

     /**
     * Returns the current temperature.
     * @return the temperature
     */
    public String getTemperature() {
        return temperature;
    }

    /**
     * Sets the temperature of the weather response.
     *  @param temperature the temperature to set
     */
    public void setTemperature(String temperature) {
        this.temperature = temperature;
    }

    /**
     * Returns the current wind speed and direction.
     * @return the wind details
     */
    public String getWind() {
        return wind;
    }

    /**
     * Sets the wind attribute of the weather response.
     * @param wind the wind details to set
     */
    public void setWind(String wind) {
        this.wind = wind;
    }

    /**
     * Returns the weather description.
     * @return the weather description
     */
    public String getDescription() {
        return description;
    }

     /**
     * Sets the description of the weather.
     * @param description the weather description to set
     */
    public void setDescription(String description) {
        this.description = description;
    }

     /**
     * Returns a list of forecast data.
     * @return the list of forecast details
     */
    public List<Forecast> getForecast() {
        return forecast;
    }

    /**
     * Sets the forecast information for the weather response.
     *  @param forecast the list of forecast details to set
     */
    public void setForecast(List<Forecast> forecast) {
        this.forecast = forecast;
    }

    /**
     *  Represents a forecast entry within the weather API response.
     */
    public static class Forecast {
        private String day;
        private String temperature;
        private String wind;

        /**
         * Returns the day for the forecast.
         * @return the day
         */
        public String getDay() {
            return day;
        }

        /**
         * Sets the day of the forecast.
         *  @param day the day to set
         */
        public void setDay(String day) {
            this.day = day;
        }

         /**
         * Returns the temperature for the specified day.\
         * @return the temperature
         */
        public String getTemperature() {
            return temperature;
        }


        /**
         * Sets the temperature for the forecast day.
         * @param temperature the temperature to set
         */
        public void setTemperature(String temperature) {
            this.temperature = temperature;
        }

         /**
         * Returns the wind information for the forecast day.
         * @return the wind details
         */
        public String getWind() {
            return wind;
        }

         /**
         * Sets the wind attribute for the forecast day.
         * @param wind the wind details to set
         */
        public void setWind(String wind) {
            this.wind = wind;
        }
    }
}
