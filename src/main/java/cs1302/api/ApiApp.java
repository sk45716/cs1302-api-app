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
import java.io.UnsupportedEncodingException;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;
import javafx.scene.control.ButtonBar;
import java.util.Optional;

/**
 * REPLACE WITH NON-SHOUTING DESCRIPTION OF YOUR APP.
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
    private long lastRequestTime = System.currentTimeMillis();;

    /**
     * Constructs an {@code ApiApp} object. This default (i.e., no argument)
     * constructor is executed in Step 2 of the JavaFX Application Life-Cycle.
     */
    public ApiApp() {
        root = new VBox();
    } // ApiApp


        private void performWeatherFetch() {
              fetchWeatherButton.setDisable(true);
            if (apiRequestCount >= MAX_REQUESTS_PER_HOUR) {
        long currentTime = System.currentTimeMillis();
        long timeSinceLastRequest = currentTime - lastRequestTime;
        if (timeSinceLastRequest < 3600000) {  // 1 hour in milliseconds
            long waitTime = 3600000 - timeSinceLastRequest;
            instructionsLabel.setText("Waiting to handle rate limits. Please wait " + (waitTime / 60000) + " minutes.");
            new java.util.Timer().schedule(
                new java.util.TimerTask() {
                    @Override
                    public void run() {
                        Platform.runLater(() -> performWeatherFetch());
                    }
                },
                waitTime
            );
            return; // Prevent further execution until the wait time has passed
        } else {
            apiRequestCount = 0;
            lastRequestTime = System.currentTimeMillis();
        }
    }
    apiRequestCount++;

            progressBar.setProgress(0);
        progressBarTimeline = new Timeline(new KeyFrame(Duration.millis(50), e -> {
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
     * Initalizes the application components and variables.
     */
    @Override
    public void init() {
        System.out.println("init() called");

         Label titleLabel = new Label("Weather Woof");
        titleLabel.setFont(new Font("Arial", 20));
        titleLabel.setAlignment(Pos.CENTER);

        instructionsLabel = new Label("Enter a city name for weather information and the related dog image");
        instructionsLabel.setFont(new Font("Arial", 14));

           cityInput = new TextField();
           cityInput.setPromptText("City");

        fetchWeatherButton = new Button("Fetch Weather");
        fetchWeatherButton.setOnAction(e -> performWeatherFetch());

        fetchDogImageButton = new Button("Fetch Dog Image");
        fetchDogImageButton.setOnAction(e -> fetchDogImage());
        fetchDogImageButton.setDisable(true);


        progressBar = new ProgressBar(0);
        progressBar.setPrefWidth(300);
        progressBar.setPrefHeight(20);

         dogImageView = new ImageView();
        dogImageView.setFitHeight(300);
        dogImageView.setFitWidth(300);
        dogImageView.setPreserveRatio(true);

        imageCaption = new Text();
        imageCaption.setFont(new Font("Arial", 16));
StackPane.setAlignment(imageCaption, Pos.BOTTOM_CENTER);


StackPane imagePane = new StackPane(dogImageView, imageCaption);
StackPane.setMargin(imageCaption, new Insets(10, 0, 10, 0));
StackPane.setAlignment(imageCaption, Pos.BOTTOM_CENTER);

        VBox contentBox = new VBox(10, titleLabel, instructionsLabel, cityInput, fetchWeatherButton, fetchDogImageButton, progressBar,  imagePane);
        contentBox.setAlignment(Pos.CENTER);
        contentBox.setPadding(new Insets(10));

        root = new VBox(contentBox);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(20));
    }


    private void fetchWeatherData() {
        String city = cityInput.getText().trim();
        if (city.isEmpty()) {
            showAlert("Input Error", "No City Entered", "Please enter a city to fetch weather.");
            return;
        }


    try {
        String encodedCity = URLEncoder.encode(city, StandardCharsets.UTF_8.toString());
        String weatherRequestUrl = WEATHER_API + encodedCity;
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(weatherRequestUrl)).build();

        Task<Void> task = new Task<>() {
            @Override
            protected Void call() throws Exception {
                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                  if (response.statusCode() == 429) {
                      Optional<String> retryAfter = response.headers().firstValue("retry-after");
                      if (retryAfter.isPresent()) {
                        int waitTime = Integer.parseInt(retryAfter.get()) * 1000; // Convert seconds to milliseconds
                        Platform.runLater(() -> instructionsLabel.setText("API rate limit exceeded. Waiting " + retryAfter.get() + " seconds."));
                        Thread.sleep(waitTime);
                        Platform.runLater(() -> fetchWeatherData()); // Attempt to fetch data again after waiting
                    } else {
                        Platform.runLater(() -> instructionsLabel.setText("API rate limit exceeded. Please wait."));
                    }
                  }else if (response.statusCode() == 200) {
                    WeatherApiResponse weatherApiResponse = gson.fromJson(response.body(), WeatherApiResponse.class);
                    String weatherData = formatWeatherData(weatherApiResponse);
                    currentWeatherCondition = determineWeatherCondition(weatherApiResponse); // Store the condition
                    Platform.runLater(() -> {
                        showWeatherPopup(weatherData);
                        fetchDogImageButton.setDisable(false);
                        progressBar.setProgress(1);
                    });
                } else {
                    Platform.runLater(() -> {
                        showAlert("Fetch Failed", "Unable to retrieve weather data", "Please check your network connection or API settings.");
                        progressBar.setProgress(0);
                    });
                }
                return null;
            }
        };
        new Thread(task).start();
    } catch (UnsupportedEncodingException e) {
        showAlert("Error", "Unsupported Encoding", e.getMessage());
    } catch (IOException e) {
        showAlert("Error", "IOException", e.getMessage());
    }
    }

     private void showWeatherPopup(String weatherData) {
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Weather Information");
        dialog.setContentText(weatherData);
        ButtonType buttonTypeOk = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().add(buttonTypeOk);
        dialog.showAndWait();
    }

    private void fetchDogImage() {
       progressBar.setProgress(0);
       progressBarTimeline = new Timeline(new KeyFrame(Duration.millis(50), ev -> {
        if (progressBar.getProgress() < 1.0) {
            progressBar.setProgress(progressBar.getProgress() + 0.01);
        }
        else {
            progressBarTimeline.stop(); // Stop the timeline when 100% is reached
            fetchDogImageAfterProgressBarFull(); // Fetch image after progress bar is full
        }
    }));
    progressBarTimeline.setCycleCount(Timeline.INDEFINITE);
    progressBarTimeline.play();

    }

    private void fetchDogImageAfterProgressBarFull() {
        String apiUrl = RANDOM_DOG_IMAGE_API + "/woof.json";
    HttpRequest request = HttpRequest.newBuilder().uri(URI.create(apiUrl)).build();
    HttpClient client = HttpClient.newHttpClient();

    client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
          .thenApply(response -> response.body())
          .thenApply(json -> gson.fromJson(json, RandomDogResponse.class))
          .thenAccept(response -> {
              Platform.runLater(() -> {
                  try {
                      Image image = new Image(response.getUrl(), true); // Load image from URL in the JSON
                      dogImageView.setImage(image);
                      imageCaption.setText(getCaptionForWeather(currentWeatherCondition)); // Update caption based on weather
                      progressBar.setProgress(1);
                  } catch (Exception e) {
                      showAlert("Error", "Image Load Exception", e.getMessage());
                      progressBar.setProgress(0);
                  }
              });
          })
          .exceptionally(e -> {
              showAlert("Error", "Failed to load dog image", e.getMessage());
              return null;
          });
}

    private String formatWeatherData(WeatherApiResponse data) {
         // Main weather information
    String weatherInfo = String.format(
        "Current Weather:\nTemperature: %s\nWind: %s\nDescription: %s\n\n",
        data.getTemperature(), data.getWind(), data.getDescription()
    );

    // Forecast summary
    String forecastInfo = "3-Day Forecast:\n";
    for (WeatherApiResponse.Forecast item : data.getForecast()) {
        forecastInfo += String.format("Day %d - Temp: %s, Wind: %s\n",
                                      item.getDay(), item.getTemperature(), item.getWind());
    }

    // Determine weather condition based on description
    String weatherCondition = determineWeatherCondition(data);

    // Combine and return the full weather information with condition at the end
    return weatherInfo + forecastInfo + "\nWeather Condition: " + weatherCondition;
    }

   private String determineWeatherCondition(WeatherApiResponse data) {
       String description = data.getDescription().toLowerCase();
       double temperature = Double.parseDouble(data.getTemperature().replaceAll("[^\\d.]", ""));
       double windSpeed = Double.parseDouble(data.getWind().replaceAll("[^\\d.]", ""));

       if (description.contains("sunny") && temperature > 25) {
           return "sunny";
       } else if (description.contains("rain") || description.contains("thunderstorm")) {
           return "rainy";
       } else if (description.contains("snow") || temperature <= 0) {
           return "snowy";
       } else if (description.contains("windy") || windSpeed > 20) {
           return "windy";
    } else {
        return "cloudy";  // Default condition if no others are met
    }
}

    private String getCaptionForWeather(String weather) {
    switch (weather.toLowerCase()) {
        case "sunny":
            return "Perfect day for a walk with your dog!";
        case "rainy":
            return "It's wet outside, stay dry!";
        case "snowy":
            return "Bundle up, it's snowy!";
        case "windy":
            return "Hold onto your hat, it's windy!";
        case "cloudy":
            return "A bit gloomy, but great for a walk!";
        default:
            return "Enjoy the day with your furry friend!";
    }
}


    private void showAlert(String title, String header, String message) {
        Platform.runLater(() -> {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(message);
        alert.showAndWait();
    });
}
    /** {@inheritDoc} */
    @Override
    public void start(Stage stage) {
        this.stage = stage;
        root.setAlignment(Pos.TOP_CENTER);
    root.setPadding(new Insets(10, 20, 20, 20));
        scene = new Scene(root,500, 600);

        // setup stage
        stage.setTitle("Weather App");
        stage.setScene(scene);
        stage.setResizable(true);
        stage.setOnCloseRequest(event -> Platform.exit());
        stage.sizeToScene();
        stage.show();

    } // start

     @Override
    public void stop() {
        System.out.println("Application stopped.");
    }

} // ApiApp
