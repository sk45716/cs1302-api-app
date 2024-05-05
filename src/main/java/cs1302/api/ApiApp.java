package cs1302.api;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.URI;
import java.io.IOException;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.geometry.Insets;
import javafx.concurrent.Task;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import javafx.scene.control.ProgressBar;
import javafx.animation.Timeline;
import javafx.animation.KeyFrame;
import javafx.util.Duration;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Label;
import javafx.scene.control.Dialog;
import javafx.scene.control.ButtonType;
import javafx.geometry.Pos;
import javafx.scene.control.TextField;
import javafx.scene.text.Font;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.text.Text;
import java.util.Optional;
import java.util.Random;
import java.util.List;
import java.util.Map;
import javafx.scene.control.ButtonBar;

/**
 * Main application class that integrates two Restful APIS
 * to display weather and related dog images.
 */
public class ApiApp extends Application {

    private static final String WEATHER_API = "https://goweather.herokuapp.com/weather/";
    private static final String RANDOM_DOG_IMAGE_API = "https://random.dog";
    private static final int MAX_REQUESTS_PER_HOUR = 100;

    private static HttpClient client = HttpClient.newBuilder()
        .version(HttpClient.Version.HTTP_2)
        .followRedirects(HttpClient.Redirect.NORMAL)
        .build();
    private static Gson gson = new GsonBuilder().create();

    private Stage stage;
    private Scene scene;
    private VBox root;
    private Button fetchWeatherButton;
    private Button fetchDogImageButton;
    private ProgressBar progressBar;
    private Label instructionsLabel;
    private Timeline progressBarTimeline;
    private TextField cityInput;
    private ImageView dogImageView;
    private Text imageCaption;
    private String currentWeatherCondition = "";
    private int apiRequestCount = 0;
    private long lastRequestTime = System.currentTimeMillis();
    private Random random = new Random();
    private Map<String, List<Integer>> statusCodesByWeather = Map.of(
        "sunny", List.of(100, 200, 226, 402, 420, 449),
        "rainy", List.of(405, 408, 410, 419, 460, 561),
        "snowy", List.of(530, 508, 428, 300, 207, 525)
        );

    /**
     * Constructs an {@code ApiApp} object. This default (i.e., no argument)
     * constructor is executed in Step 2 of the JavaFX Application Life-Cycle.
     */
    public ApiApp() {
        root = new VBox();
    } // ApiApp

    /**
     * Initiates the process to fetch weather data, managing the progress bar and API rate limits.
     */
    private void performWeatherFetch() {
        fetchWeatherButton.setDisable(true);
        if (checkRateLimit()) {
            return;
        }
        progressBar.setProgress(0);
        progressBarTimeline = new Timeline(new KeyFrame(Duration.millis(40), e -> {
            if (progressBar.getProgress() < 1) {
                progressBar.setProgress(progressBar.getProgress() + 0.01);
            } else {
                progressBarTimeline.stop();
                fetchWeatherData();
            }
        }));
        progressBarTimeline.setCycleCount(Timeline.INDEFINITE);
        progressBarTimeline.play();
    }

    /**
     * Checks if the API request limit has been reached and schedules a retry if necessary.
     * @return true if rate limit exceeded, otherwise false.
     */
    private boolean checkRateLimit() {
        long currentTime = System.currentTimeMillis();
        long timeSinceLastRequest = currentTime - lastRequestTime;

        if (apiRequestCount >= MAX_REQUESTS_PER_HOUR) {
            if (timeSinceLastRequest < 3600000) {
                long waitTime = 3600000 - timeSinceLastRequest;
                instructionsLabel.setText("Rate limit exceeded. Please wait " +
                                          (waitTime / 60000) + " minutes.");
                scheduleRetry(waitTime);
                return true;
            } else {
                resetRateLimitCounters();
            }
        }
        apiRequestCount++;
        return false;
    }

    /**
     * Schedules a retry fetch operation after a delay.
     * @param waitTime the delay time in milliseconds.
     */
    private void scheduleRetry(long waitTime) {
        new java.util.Timer().schedule(new java.util.TimerTask() {
                @Override
                public void run() {
                    Platform.runLater(() -> performWeatherFetch());
                }
            }, waitTime);
    }

    /**
     * Displays the formatted weather data in a pop-up dialog.
     * @param data the weather API response data to be formatted and displayed
     */
    private void showWeatherData(WeatherApiResponse data) {
        String weatherData = formatWeatherData(data);
        showWeatherPopup(weatherData);
    }

    /**
     * Resets the API request counter and the last request timestamp to manage rate limiting.
     */
    private void resetRateLimitCounters() {
        apiRequestCount = 0;
        lastRequestTime = System.currentTimeMillis();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void init() {
        System.out.println("init() called");

        Label titleLabel = new Label("Weather Mood Dog Viewer");
        titleLabel.setFont(new Font("Arial", 24));
        titleLabel.setAlignment(Pos.CENTER);

        instructionsLabel = new Label("Enter a city to see the weather mood"
                                      + " and a matching dog image!");
        instructionsLabel.setFont(new Font("Arial", 16));

        cityInput = new TextField();
        cityInput.setPromptText("Enter city name");

        fetchWeatherButton = new Button("Fetch Weather and Dog Image");
        fetchWeatherButton.setOnAction(e -> performWeatherFetch());

        progressBar = new ProgressBar(0);
        progressBar.setPrefWidth(300);
        progressBar.setPrefHeight(25);

        dogImageView = new ImageView();
        dogImageView.setFitHeight(250);
        dogImageView.setFitWidth(250);
        dogImageView.setPreserveRatio(true);

        imageCaption = new Text();
        imageCaption.setFont(new Font("Arial", 16));

        VBox imageContainer = new VBox(15); // Adjust spacing as needed
        imageContainer.setAlignment(Pos.CENTER);
        imageContainer.getChildren().addAll(dogImageView, imageCaption);

        VBox contentBox = new VBox(30, titleLabel, instructionsLabel, cityInput,
                                   fetchWeatherButton, progressBar, imageContainer);
        contentBox.setAlignment(Pos.CENTER);
        contentBox.setPadding(new Insets(10));

        root = new VBox(contentBox);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(15));
    }

    /**
     * Attempts to fetch weather data and handle the response.
     */
    private void fetchWeatherData() {
        String city = cityInput.getText().trim();
        if (city.isEmpty()) {
            showAlert("Input Error", "No City Entered", "Please enter a city to fetch weather.");
            fetchWeatherButton.setDisable(false);
            return;
        }

        try {
            String encodedCity = URLEncoder.encode(city, StandardCharsets.UTF_8.toString());
            String weatherRequestUrl = WEATHER_API + encodedCity;
            HttpRequest request = HttpRequest.newBuilder().uri(URI.create
                                                               (weatherRequestUrl)).build();
            Task<Void> task = new Task<>() {
                    @Override
                    protected Void call() throws Exception {
                        HttpResponse<String> response = client.send(request,
                                                HttpResponse.BodyHandlers.ofString());
                        if (response.statusCode() == 429) {
                            Optional<String> retryAfter = response.headers().firstValue
                                ("retry-after");
                            if (retryAfter.isPresent()) {
                                int waitTime = Integer.parseInt(retryAfter.get()) * 1000;
                                Platform.runLater(() ->
                                     instructionsLabel.setText("API rate limit exceeded. Waiting "
                                                               + retryAfter.get() + " seconds."));
                                Thread.sleep(waitTime);
                                Platform.runLater(() -> fetchWeatherData());
                            } else {
                                Platform.runLater(() ->
                                    instructionsLabel.setText
                                                  ("API rate limit exceeded. Please wait."));
                            }
                        } else if (response.statusCode() == 200) {
                            WeatherApiResponse weatherApiResponse = gson.fromJson(response.body(),
                                                                          WeatherApiResponse.class);
                            currentWeatherCondition = determineWeatherCondition(weatherApiResponse);
                            Platform.runLater(() -> {
                                showWeatherData(weatherApiResponse);
                                fetchDogImage();
                            });
                        } else {
                            Platform.runLater(() -> showAlert("Fetch Failed",
                                                              "Unable to retrieve weather data",
                                                              "Status Code: "
                                                              + response.statusCode()));
                        }
                        return null;
                    }
                };
            new Thread(task).start();
        } catch (Exception e) {
            showAlert("Error", e.getClass().getSimpleName(), e.getMessage());
        }
    }

    /**
     * Displays weather data in a popup dialog.
     * @param weatherData formatted string of weather data.
     */
    private void showWeatherPopup(String weatherData) {
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Weather Information");
        dialog.setContentText(weatherData);
        ButtonType buttonTypeOk = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().add(buttonTypeOk);
        dialog.showAndWait();
    }

     /**
     * Fetches a random dog image based on the current weather condition and updates the UI.
     */
    private void fetchDogImage() {
        if (currentWeatherCondition.isEmpty()) {
            return;
        }

        List<Integer> statusCodes = statusCodesByWeather.getOrDefault
            (currentWeatherCondition, List.of(100));
        Random random = new Random();
        int randomIndex = random.nextInt(statusCodes.size());
        int statusCode = statusCodes.get(randomIndex);
        String imageUrl = "https://http.dog/" + statusCode + ".jpg";

        Image image = new Image(imageUrl, true);
        dogImageView.setImage(image);
        Platform.runLater(() -> {
            progressBar.setProgress(1);
            fetchWeatherButton.setDisable(false);
            updateImageCaption(currentWeatherCondition);
        });

    }

    /**
     * Fetches a dog image after the progress bar is full, then updates the GUI.
     */
    private void fetchDogImageAfterProgressBarFull() {
        String apiUrl = RANDOM_DOG_IMAGE_API + "/woof.json";
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(apiUrl)).build();
        HttpClient client = HttpClient.newHttpClient();

        client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
            .thenApply(response -> response.body())
            .thenApply(json -> gson.fromJson(json, HttpDogResponse.class))
            .thenAccept(response -> {
                new Thread(() -> {
                    try {
                        final Image image = new Image(response.getUrl(), true);
                        Platform.runLater(() -> {
                            dogImageView.setImage(image);
                            imageCaption.setText("Based on the weather condition, which is "
                                                         + currentWeatherCondition +
                                                         ", here is a relevant dog image.");
                            progressBar.setProgress(1);
                        });
                    } catch (Exception e) {
                        Platform.runLater(() -> showAlert
                                              ("Error", "Image Load Exception", e.getMessage()));
                    }
                }).start();
            })
            .exceptionally(e -> {
                Platform.runLater(() -> showAlert
                                      ("Error", "Failed to load dog image", e.getMessage()));
                return null;
            });
    }

     /**
     * Formats the weather data into a human-readable string.
     * @param data the weather API response data.
     * @return formatted weather data.
     */
    private String formatWeatherData(WeatherApiResponse data) {
        String weatherInfo = String.format(
            "Current Weather:\nTemperature: %s\nWind: %s\nDescription: %s\n\n",
            data.getTemperature(),
            data.getWind(),
            data.getDescription()
            );

        String forecastInfo = "3-Day Forecast:\n";
        for (WeatherApiResponse.Forecast item : data.getForecast()) {
            forecastInfo += String.format("Day %s - Temp: %s, Wind: %s\n",
                                          item.getDay(),
                                          item.getTemperature(),
                                          item.getWind());
        }

        String weatherCondition = determineWeatherCondition(data);
        updateImageCaption(currentWeatherCondition);

        return weatherInfo + forecastInfo + "\nWeather Condition: " + weatherCondition;
    }

      /**
     * Updates the image caption based on the current weather condition.
     * @param condition the current weather condition.
     */
    private void updateImageCaption(String condition) {
        String mood = "enjoying";
        switch (condition) {
        case "sunny":
            mood = "happy";
            break;
        case "rainy":
            mood = "sad";
            break;
        case "snowy":
            mood = "cold";
            break;
        case "cloudy":
            mood = "calm";
            break;
        }
        final String caption = String.format
            ("Based on the weather condition, which is %s, here is an image of a %s dog.",
             condition, mood);
        Platform.runLater(() -> imageCaption.setText(caption));
    }

      /**
     * Determines the weather condition from the API response.
     * @param data the weather API response data.
     * @return a string representing the weather condition.
     */
    private String determineWeatherCondition(WeatherApiResponse data) {
        String description = data.getDescription().toLowerCase();
        double temperature = Double.parseDouble(data.getTemperature().replaceAll("[^\\d.]", ""));
        double windSpeed = Double.parseDouble(data.getWind().replaceAll("[^\\d.]", ""));

        if (description.contains("sunny") || description.contains("clear") && temperature > 18) {
            return "sunny";
        } else if (description.contains("rain") || description.contains("thunderstorm")) {
            return "rainy";
        } else if (description.contains("snow") || temperature <= 0) {
            return "snowy";
        } else if (description.contains("windy") ||  windSpeed > 20) {
            return "windy";
        } else {
            return "cloudy";
        }
    }

      /**
     * Shows an alert dialog in case of an error or information need.
     * @param title the title of the alert.
     * @param header the header text of the alert.
     * @param message the main message of the alert.
     */
    private void showAlert(String title, String header, String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(AlertType.ERROR);
            alert.setTitle(title);
            alert.setHeaderText(header);
            alert.setContentText(message);
            alert.setOnCloseRequest(event -> fetchWeatherButton.setDisable(false));
            alert.showAndWait();
        });
    }

    /** {@inheritDoc} */
    @Override
    public void start(Stage stage) {
        this.stage = stage;
        root.setAlignment(Pos.TOP_CENTER);
        root.setPadding(new Insets(10, 20, 20, 20));
        scene = new Scene(root, 700, 600);
        stage.setTitle("Weather App");
        stage.setScene(scene);
        stage.setResizable(true);
        stage.setOnCloseRequest(event -> Platform.exit());
        stage.sizeToScene();
        stage.show();

    } // start

    /**{@inheritDoc} */
    @Override
    public void stop() {
        System.out.println("Application stopped.");
    }

} // ApiApp
