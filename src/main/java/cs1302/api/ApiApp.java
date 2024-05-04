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
import javafx.scene.control.TextArea;
import javafx.scene.control.ProgressBar;
import javafx.animation.Timeline;
import javafx.animation.KeyFrame;
import javafx.util.Duration;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Label;
import javafx.geometry.Pos;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.text.Font;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.io.UnsupportedEncodingException;

/**
 * REPLACE WITH NON-SHOUTING DESCRIPTION OF YOUR APP.
 */
public class ApiApp extends Application {

     private static final String OPEN_METEO_API = "https://api.open-meteo.com/v1/forecast";
    private static final String APIXU_API = "https://api.weatherstack.com/current";
    private static final String API_KEY = "cb56b32aba9025625bb9db9a302b88fd";

    private static HttpClient client = HttpClient.newBuilder()
        .version(HttpClient.Version.HTTP_2)
        .followRedirects(HttpClient.Redirect.NORMAL)
        .build();
    private static Gson gson = new GsonBuilder().create();

     private Stage stage;
    private Scene scene;
    private VBox root;
    private Button fetchWeatherButton;
    private TextArea weatherDataArea;
    private ProgressBar progressBar;
    private Label instructionsLabel;
    private Timeline progressBarTimeline;
    private TextField cityInput;

    /**
     * Constructs an {@code ApiApp} object. This default (i.e., no argument)
     * constructor is executed in Step 2 of the JavaFX Application Life-Cycle.
     */
    public ApiApp() {
        root = new VBox();
    } // ApiApp


        private void performWeatherFetch() {
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
         // Create labels for fields and buttons
    Label titleLabel = new Label("Weather App");
    titleLabel.setFont(new Font("Arial", 20));
    titleLabel.setAlignment(Pos.CENTER);

    instructionsLabel = new Label("Hello. This is the Weather Data App. Please type in a city name for weather information.");
    instructionsLabel.setFont(new Font("Arial", 14));

        cityInput = new TextField();
        cityInput.setPromptText("Enter city name");

    // Create the button to fetch weather
    fetchWeatherButton = new Button("Fetch Weather");
    fetchWeatherButton.setOnAction(e -> performWeatherFetch());

    // Create the TextArea for displaying the weather data
    weatherDataArea = new TextArea();
    weatherDataArea.setEditable(false);
    weatherDataArea.setPrefHeight(200); // Enough height to display all data
    weatherDataArea.setWrapText(true);

    // Create a ProgressBar to indicate loading
    progressBar = new ProgressBar(0);
    progressBar.setPrefWidth(300);

    // Arrange latitude and longitude inputs horizontally
    HBox cityBox = new HBox(10, new Label("City:"), cityInput);
    cityBox.setAlignment(Pos.CENTER);


    // Main content box
    VBox contentBox = new VBox(10, titleLabel, instructionsLabel, cityBox, fetchWeatherButton, progressBar, weatherDataArea);
    contentBox.setAlignment(Pos.CENTER);
    contentBox.setPadding(new Insets(5));  // Increased padding for aesthetics

    // Setting the main layout container
    root = new VBox(contentBox);
    root.setAlignment(Pos.CENTER);
    root.setPadding(new Insets(10));
    }


    private void fetchWeatherData() {
         String cityName = cityInput.getText().trim();
        if (cityName.isEmpty()) {
            showAlert("Input Error", "No City Entered", "Please enter a city name to fetch weather.");
            return;
        }
         try {
        // URL encode the city name to handle cities with spaces
        String encodedCityName = URLEncoder.encode(cityName, StandardCharsets.UTF_8.toString());

        String openMeteoRequestUri = OPEN_METEO_API + "?city=" + encodedCityName +
            "&daily=temperature_2m_max,temperature_2m_min&timezone=auto";
        HttpRequest openMeteoRequest = HttpRequest.newBuilder().uri(URI.create(openMeteoRequestUri)).build();

        Task<Void> openMeteoTask = new Task<>() {
            @Override
            protected Void call() throws Exception {
                HttpResponse<String> response = client.send(openMeteoRequest, HttpResponse.BodyHandlers.ofString());
                if (response.statusCode() == 200) {
                    OpenMeteoResponse openMeteoData = gson.fromJson(response.body(), OpenMeteoResponse.class);
                    Platform.runLater(() -> {
                        weatherDataArea.setText("Fetching additional data...");
                        fetchAdditionalDataFromAPIXU(encodedCityName, openMeteoData);
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
        new Thread(openMeteoTask).start();
    } catch (UnsupportedEncodingException e) {
        showAlert("Error", "Encoding Error", "Error encoding the URL.");
    }
    }

    private void fetchAdditionalDataFromAPIXU(String cityName, OpenMeteoResponse openMeteoData) {
          String apixuRequestUri = APIXU_API + "?access_key=" + API_KEY + "&query=" + cityName;
        HttpRequest apixuRequest = HttpRequest.newBuilder().uri(URI.create(apixuRequestUri)).build();

    Task<Void> apixuTask = new Task<>() {
        @Override
        protected Void call() throws Exception {
            HttpResponse<String> response = client.send(apixuRequest, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                APIXUResponse apixuData = gson.fromJson(response.body(), APIXUResponse.class);
                Platform.runLater(() -> {
                    String formattedWeatherData = formatWeatherData(openMeteoData, apixuData);
                    weatherDataArea.setText(formattedWeatherData);
                    progressBar.setProgress(1);
                });
            } else {
                Platform.runLater(() -> {
                    showAlert("Error", "Fetch Failed", "Unable to retrieve weather data from APIXU.");
                    progressBar.setProgress(0);
                });
            }
            return null;
        }
    };
    new Thread(apixuTask).start();
    }


    private String formatWeatherData(OpenMeteoResponse openMeteoData, APIXUResponse apixuData) {
         String output = "Weather Data:\n";

    if (openMeteoData != null && openMeteoData.daily != null) {
        output += "Daily Max/Min Temperature:\n";
        output += String.format("Max: %.1f°C, Min: %.1f°C\n",
                openMeteoData.daily.temperature_2m_max[0],
                openMeteoData.daily.temperature_2m_min[0]);
    } else {
        output += "No daily temperature data available.\n";
    }

    if (apixuData != null && apixuData.current != null) {
        output += "\nCurrent Conditions:\n";
        if (apixuData.current.weather_descriptions != null) {
            output += String.format("Temperature: %d°C, Weather: %s, Wind: %d km/h %s, Humidity: %d%%, Visibility: %d km\n",
                    apixuData.current.temperature,
                    String.join(", ", apixuData.current.weather_descriptions),
                    apixuData.current.wind_speed,
                    apixuData.current.wind_dir,
                    apixuData.current.humidity,
                    apixuData.current.visibility);
        }
    }
    return output;
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
        scene = new Scene(root,600, 450);

        // setup stage
        stage.setTitle("Weather Data App");
        stage.setScene(scene);
        stage.setOnCloseRequest(event -> Platform.exit());
        stage.sizeToScene();
        stage.show();

    } // start

     @Override
    public void stop() {
        System.out.println("Application stopped.");
    }

} // ApiApp
