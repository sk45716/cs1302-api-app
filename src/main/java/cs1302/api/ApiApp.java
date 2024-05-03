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
       private TextField latitudeInput;
    private TextField longitudeInput;

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

    instructionsLabel = new Label("Hello. This is the Weather Data App. Please type in a longitude and latitude "
                                  + "\n" + "for information related to the weather.");
    instructionsLabel.setFont(new Font("Arial", 14));

    // Create the text fields for latitude and longitude inputs
    latitudeInput = new TextField();
    latitudeInput.setPromptText("Enter latitude (-90 to 90)");
    longitudeInput = new TextField();
    longitudeInput.setPromptText("Enter longitude (-180 to 180)");

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
    HBox latBox = new HBox(10, new Label("Latitude:"), latitudeInput);
    latBox.setAlignment(Pos.CENTER);


    HBox lonBox = new HBox(10, new Label("Longitude:"), longitudeInput);
    lonBox.setAlignment(Pos.CENTER);

    // Main content box
    VBox contentBox = new VBox(10, titleLabel, instructionsLabel, latBox, lonBox, fetchWeatherButton, progressBar, weatherDataArea);
    contentBox.setAlignment(Pos.CENTER);
    contentBox.setPadding(new Insets(5));  // Increased padding for aesthetics

    // Setting the main layout container
    root = new VBox(contentBox);
    root.setAlignment(Pos.CENTER);
    root.setPadding(new Insets(10));
    }


    private void fetchWeatherData() {
         try {
        double latitude = Double.parseDouble(latitudeInput.getText());
        double longitude = Double.parseDouble(longitudeInput.getText());

        if (latitude < -90 || latitude > 90 || longitude < -180 || longitude > 180) {
            showAlert("Input Error", "Out of Bounds",
                "The latitude range should be between -90 and 90, and the longitude should be between -180 and 180. The latitude and longitude you provided are out of bounds.");
            return;
        }

        String openMeteoRequestUri = String.format("%s?latitude=%f&longitude=%f", OPEN_METEO_API, latitude, longitude);
        HttpRequest openMeteoRequest = HttpRequest.newBuilder()
            .uri(URI.create(openMeteoRequestUri))
            .build();

        Task<Void> openMeteoTask = new Task<>() {
            @Override
            protected Void call() throws Exception {
                HttpResponse<String> response = client.send(openMeteoRequest, HttpResponse.BodyHandlers.ofString());
                if (response.statusCode() == 200) {
                    OpenMeteoResponse openMeteoData = gson.fromJson(response.body(), OpenMeteoResponse.class);
                    Platform.runLater(() -> {
                        weatherDataArea.setText("Weather Data:\n" + formatWeatherData(openMeteoData));
                        progressBar.setProgress(1);  // Set the progress bar to complete.
                    });

                    if (needsDetailedAnalysis(openMeteoData)) {
                        fetchAdditionalDataFromAPIXU(latitude, longitude);
                    }
                } else {
                    Platform.runLater(() -> {
                        showAlert("Fetch Failed", "Unable to retrieve weather data",
                                  "Please check your network connection or API settings.");
                        progressBar.setProgress(0);  // Reset progress bar in case of failure.
                    });
                }
                return null;
            }
        };
        new Thread(openMeteoTask).start();
    } catch (NumberFormatException e) {
        showAlert("Input Error", "Invalid Input", "Please enter valid numbers for latitude and longitude.");
    }
    }

    private void fetchAdditionalDataFromAPIXU(double latitude, double longitude) {
         String apixuRequestUri = String.format("%s?access_key=%s&query=%f,%f", APIXU_API, API_KEY, latitude, longitude);
    HttpRequest apixuRequest = HttpRequest.newBuilder()
        .uri(URI.create(apixuRequestUri))
        .build();

    Task<Void> apixuTask = new Task<>() {
        @Override
        protected Void call() throws Exception {
            HttpResponse<String> response = client.send(apixuRequest, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                APIXUResponse apixuData = gson.fromJson(response.body(), APIXUResponse.class);
                Platform.runLater(() -> {
                    String existingText = weatherDataArea.getText();
                    weatherDataArea.setText(existingText + "\n\nAPIXU Data:\n" + gson.toJson(apixuData));
                    progressBar.setProgress(1);  // Ensure the progress bar reflects complete status after all data is loaded.
                });
            } else {
                Platform.runLater(() -> {
                    showAlert("Error", "Fetch Failed", "Unable to retrieve weather data from APIXU.");
                    progressBar.setProgress(0);  // Reset or handle progress bar in case of failure.
                });
            }
            return null;
        }
    };
    new Thread(apixuTask).start();

    }

    private String formatWeatherData(OpenMeteoResponse data) {
    return String.format(
        "Latitude: %s\nLongitude: %s\nTemperature: %s°C\nWind Speed: %s km/h\nPrecipitation: %s mm",
        data.latitude, data.longitude, data.temperature, data.windSpeed, data.precipitation
    );
}

    private boolean needsDetailedAnalysis(OpenMeteoResponse data) {
    // Example criteria for deciding if detailed analysis is needed
    boolean highWind = data.windSpeed > 50; // wind speed greater than 50 km/h
    boolean heavyRain = data.precipitation > 10; // more than 10 mm of rain
    boolean extremeTemperature = data.temperature < -10 || data.temperature > 35; // extreme cold or heat

    // Return true if any of the conditions are met
    return highWind || heavyRain || extremeTemperature;
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
