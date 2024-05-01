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


    /**
     * Class to handle the cat fact JSON response.
     */
    private static class CatFactResponse {
        String fact;
    }

    private static final int NUM_IMAGES = 5;
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
        progressBar.setPrefWidth(300);

        imageGrid = new GridPane();
        imageGrid.setPadding(new Insets(5));
        imageGrid.setHgap(10);
        imageGrid.setVgap(10);
        root.getChildren().add(imageGrid);

        HBox topBar = new HBox(generateButton);
        topBar.setAlignment(Pos.CENTER);
        topBar.setPadding(new Insets(10));

        VBox contentBox = new VBox(20, catImageView, progressBar, catFactArea);
        contentBox.setAlignment(Pos.CENTER);
        contentBox.setPadding(new Insets(5));

        root = new VBox(10, topBar, contentBox);
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
    fetchCatFact(0);
}

    /**
     * Fetches cat images and a cat fact.
     */
    private void fetchCatContent() {
          // Clear existing content in the grid
    imageGrid.getChildren().clear();

    // Generate new cat images and facts
    for (int i = 0; i < 5; i++) {
        final int index = i;
        ImageView imageView = new ImageView();
        imageView.setFitWidth(200);  // Adjust width as per UI needs
        imageView.setFitHeight(200); // Adjust height as per UI needs

        // Fetch and set cat image
        Image image = new Image(CAT_IMAGE_API + "?random=" + Math.random(), true);
        imageView.setImage(image);

        // Fetch cat fact and display it
        fetchCatFact(index);

        final ImageView finalImageView = imageView;
        // Add the ImageView to the grid
        Platform.runLater(() -> imageGrid.add(finalImageView, index % 5, index / 5));
    }
    }


    private void fetchCatFact(final int index) {
          HttpRequest request = HttpRequest.newBuilder()
        .uri(URI.create(CAT_FACTS_API))
        .build();

    Task<String> task = new Task<>() {
        @Override
        protected String call() throws Exception {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                Gson gson = new Gson();
                CatFactResponse catFactResponse = gson.fromJson(response.body(), CatFactResponse.class);
                return catFactResponse.fact;
            } else {
                throw new IOException("Failed to fetch cat fact: " + response.statusCode());
            }
        }
    };

    task.setOnSucceeded(e -> {
        final String fact = task.getValue(); // Declare 'fact' as final so it can be used inside lambda
        Platform.runLater(() -> {
            // Update the TextArea for displaying the fact
            catFactArea.setText(fact);

            // Create a label to show the fact below the corresponding image
            Label factLabel = new Label(fact);
            factLabel.setWrapText(true);
            // Assuming each row in the grid is reserved for an image and its fact label
            imageGrid.add(factLabel, index % 5, (index / 5) * 2 + 1);

            // Set progress to indicate completion
            progressBar.setProgress(1.0);
        });
        });

    task.setOnFailed(e -> {
        final String errorMessage = "Failed to load cat fact."; // Declare 'errorMessage' as final
        Platform.runLater(() -> {
            catFactArea.setText(errorMessage);
            showAlert("Error", "Fetch Failed", "Unable to retrieve cat fact.");
            progressBar.setProgress(0);
        });
    });

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
        stage.setTitle("Anime Search App");
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
