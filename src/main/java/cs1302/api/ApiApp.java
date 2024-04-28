package cs1302.api;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import com.google.gson.Gson;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * REPLACE WITH NON-SHOUTING DESCRIPTION OF YOUR APP.
 */
public class ApiApp extends Application {
    Stage stage;
    Scene scene;
    VBox root;


    private static final String ANIME_API_KEY = "your-anime-api-key-here";
    private static final String JOKE_API_KEY = "your-joke-api-key-here";

    /**
     * Constructs an {@code ApiApp} object. This default (i.e., no argument)
     * constructor is executed in Step 2 of the JavaFX Application Life-Cycle.
     */
    public ApiApp() {
        root = new VBox();
    } // ApiApp

   private void performApiRequests(String searchTerm) {
    String animeApiUri = buildAnimeApiUri(searchTerm);
    HttpClient client = HttpClient.newHttpClient();
    HttpRequest request = HttpRequest.newBuilder()
                                      .uri(URI.create(animeApiUri))
                                      .header("Authorization", "Bearer " + ANIME_API_KEY)
                                      .build();

    client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
          .thenApply(response -> response.body())
          .thenApply(jsonResponse -> parseAnimeApiResponse(jsonResponse))
          .thenAccept(animeResponse -> {
              String jokeApiUri = buildJokeApiUri(animeResponse);
              HttpRequest secondRequest = HttpRequest.newBuilder()
                                                      .uri(URI.create(jokeApiUri))
                                                      .header("Authorization", "Bearer " + JOKE_API_KEY)
                                                      .build();

              client.sendAsync(secondRequest, HttpResponse.BodyHandlers.ofString())
                    .thenApply(response -> response.body())
                    .thenApply(jsonResponse -> parseJokeApiResponse(jsonResponse))
                    .thenAccept(jokeResponse -> Platform.runLater(() -> {
                        updateUiWithApiResponse(animeResponse, jokeResponse);
                    }));
          });
}

    private String buildFirstApiUri(String searchTerm) {
        return "http://example.com/api/first?search=" + searchTerm;
    }

    private String buildSecondApiUri(FirstApiResponse response) {
        return "http://example.com/api/second?data= " + response.getSomeData();
    }

    private FirstApiResponse parseFirstApiResponse(String jsonResponse) {
        return new Gson().fromJson(jsonResponse, FirstApiResponse.class);
    }
    privae SecondApiResponse parseSecondApiResponse(String jsonResponse) {
        return new Gson().fromJson(jsonResponse, SecondApiResponse.class);
    }



    /** {@inheritDoc} */
    @Override
    public void start(Stage stage) {

        this.stage = stage;

        // demonstrate how to load local asset using "file:resources/"
        Image bannerImage = new Image("file:resources/readme-banner.png");
        ImageView banner = new ImageView(bannerImage);
        banner.setPreserveRatio(true);
        banner.setFitWidth(640);

        // some labels to display information
        Label notice = new Label("Modify the starter code to suit your needs.");

        // setup scene
        root.getChildren().addAll(banner, notice);
        scene = new Scene(root);

        // setup stage
        stage.setTitle("ApiApp!");
        stage.setScene(scene);
        stage.setOnCloseRequest(event -> Platform.exit());
        stage.sizeToScene();
        stage.show();

    } // start

} // ApiApp
