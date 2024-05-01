package cs1302.api;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.io.IOException;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.layout.HBox;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.control.TextField;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.geometry.Pos;
import javafx.geometry.Insets;
import javafx.concurrent.Task;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import javafx.scene.control.TextArea;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import java.util.HashSet;
import java.util.Set;
import java.util.ArrayList;
import java.util.List;
import javafx.scene.control.ProgressBar;
import javafx.animation.Timeline;
import javafx.animation.KeyFrame;
import javafx.util.Duration;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

/**
 * REPLACE WITH NON-SHOUTING DESCRIPTION OF YOUR APP.
 */
public class ApiApp extends Application {

    private static final String CAT_IMAGE_API = "https://cataas.com/cat";
    private static final String CAT_FACTS_API = "https://catfact.ninja/fact";

    private static HttpClient client = HttpClient.newBuilder()
        .version(HttpClient.Version.HTTP_2)
        .followRedirects(HttpClient.Redirect.NORMAL)
        .build();
    private static Gson gson = new GsonBuilder().create();

     private Stage stage;
    private Scene scene;
    private VBox root;
    private Button generateButton;
    private ImageView catImageView;
    private GridPane imageGrid;
    private TextArea catFactArea;
    private ProgressBar progressBar;
    private Timeline progressBarTimeline;
     private Label instructionsLabel;
    /**
     * Constructs an {@code ApiApp} object. This default (i.e., no argument)
     * constructor is executed in Step 2 of the JavaFX Application Life-Cycle.
     */
    public ApiApp() {
        root = new VBox();
    } // ApiApp

    private void createComponents() {
        generateButton = new Button("Generate");
        generateButton.setOnAction(e -> generateCatContent());

        catImageView = new ImageView();
        catImageView.setFitWidth(300);
        catImageView.setFitHeight(300);
        catImageView.setPreserveRatio(true);

        catFactArea = new TextArea();
        catFactArea.setEditable(false);
        catFactArea.setWrapText(true);
        catFactArea.setPrefHeight(100);

        progressBar = new ProgressBar(0);
        progressBar.setPrefWidth(400);

        instructionsLabel = new Label("Hello, there. Please click the generate button below to generate an cat image.");
        instructionsLabel.setWrapText(true);

        VBox contentBox = new VBox(10, instructionsLabel, generateButton, catImageView, progressBar, catFactArea);
        contentBox.setAlignment(Pos.CENTER);
        contentBox.setPadding(new Insets(10));

        root = new VBox(contentBox);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(10));
    }


    /**
     * Initalizes the application components and variables.
     */
    @Override
    public void init() {
        System.out.println("init() called");
        createComponents();
    }

    private void generateCatContent() {
    progressBar.setProgress(ProgressBar.INDETERMINATE_PROGRESS);
    fetchCatImage(catImageView);
    fetchCatFact(0);
}



    private void fetchCatImage(ImageView imageView) {
        Image image = new Image(CAT_IMAGE_API, true);
        imageView.setImage(image);

    image.progressProperty().addListener((obs, oldProgress, newProgress) -> {
        if (newProgress.doubleValue() == 1.0) {
            progressBar.setProgress(1.0);
            fetchCatFact(0);  // Fetch the cat fact once the image is fully loaded
        }
    });

    image.exceptionProperty().addListener((observable, oldValue, exception) -> {
        if (exception != null) {
            Platform.runLater(() -> {
                imageView.setImage(new Image("file:resources/default.png"));  // Fallback image
                showAlert("Error", "Image Load Failed", "Failed to load cat image.");
                progressBar.setProgress(0);
            });
        }
    });
}

    private void fetchCatFact( int index) {
        HttpRequest request = HttpRequest.newBuilder()
        .uri(URI.create(CAT_FACTS_API))
        .header("Accept", "application/json")
        .build();

    Task<String> task = new Task<>() {
        @Override
        protected String call() throws Exception {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                CatFactResponse factResponse = gson.fromJson(response.body(), CatFactResponse.class);
                return factResponse.fact;
            } else {
                throw new IOException("Failed to fetch cat fact: " + response.statusCode());
            }
        }
    };

    task.setOnSucceeded(e -> Platform.runLater(() -> {
                progressBar.setProgress(1);
        catFactArea.setText(task.getValue());
    }));

    task.setOnFailed(e -> Platform.runLater(() -> {
        showAlert("Error", "Fetch Failed", "Unable to retrieve cat fact.");
        progressBar.setProgress(0);
    }));

    new Thread(task).start();
}

    private void showAlert(String title, String header, String message) {
    Alert alert = new Alert(AlertType.ERROR);
    alert.setTitle(title);
    alert.setHeaderText(header);
    alert.setContentText(message);
    alert.showAndWait();
}
    /** {@inheritDoc} */
    @Override
    public void start(Stage stage) {

        this.stage = stage;

        scene = new Scene(root,800, 600);

        // setup stage
        stage.setTitle("Random Cat Image App");
        stage.setScene(scene);
        stage.setOnCloseRequest(event -> Platform.exit());
        stage.sizeToScene();
        stage.show();

    } // start

     @Override
    public void stop() {
        System.out.println("stop() called");
    }

} // ApiApp
