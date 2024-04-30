package cs1302.api;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.function.Consumer;
import java.util.function.Function;
import com.google.gson.Gson;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.util.List;

/**
 * REPLACE WITH NON-SHOUTING DESCRIPTION OF YOUR APP.
 */
public class ApiApp extends Application {
    Stage stage;
    Scene scene;
    VBox root;
    private HttpClient client = HttpClient.newHttpClient();
    private Gson gson = new Gson();

     public static class AnimeSearchResponse {
        private List<AnimeResult> results;

        public List<AnimeResult> getResults() {
            return results;
        }

        public void setResults(List<AnimeResult> results) {
            this.results = results;
        }
    }

    // Nested static class for Anime result
    public static class AnimeResult {
        private String title;
        private String imageUrl;

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getImageUrl() {
            return imageUrl;
        }

        public void setImageUrl(String imageUrl) {
            this.imageUrl = imageUrl;
        }
    }

    // Nested static class for AniDB search response
    public static class AniDBSearchResponse {
        private List<AnimeDetail> animeDetails;

        public List<AnimeDetail> getAnimeDetails() {
            return animeDetails;
        }

        public void setAnimeDetails(List<AnimeDetail> animeDetails) {
            this.animeDetails = animeDetails;
        }
    }

    // Nested static class for Anime detail
    public static class AnimeDetail {
        private String title;
        private String description;

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }
    }
    /**
     * Constructs an {@code ApiApp} object. This default (i.e., no argument)
     * constructor is executed in Step 2 of the JavaFX Application Life-Cycle.
     */
    public ApiApp() {
        root = new VBox();
    } // ApiApp

   private void performApiRequests(String searchTerm) {
       searchAnime(searchTerm);
   }


   private void searchAnime(String searchTerm) {
         String jikanSearchUri = buildJikanApiUri(searchTerm);
    HttpRequest jikanRequest = HttpRequest.newBuilder()
        .uri(URI.create(jikanSearchUri))
        .build();

    client.sendAsync(jikanRequest, HttpResponse.BodyHandlers.ofString())
        .thenApply(response -> response.body())
        .thenApply(jsonResponse -> {
            System.out.println("API Response: " + jsonResponse); // Log the raw JSON response
            return gson.fromJson(jsonResponse, AnimeSearchResponse.class);
        })
        .thenAccept(animeResponse -> {
            if (animeResponse != null && animeResponse.getResults() != null && !animeResponse.getResults().isEmpty()) {
                displayAnimeResults(animeResponse);
            } else {
                displayErrorMessage("No anime results found or data is incomplete.");
            }
        })
        .exceptionally(e -> {
            e.printStackTrace(); // Print the stack trace for the exception
            displayErrorMessage("Error processing anime search: " + e.getMessage());
            return null;
        });
   }


    private String buildJikanApiUri(String searchTerm) {
         return "https://api.jikan.moe/v4/anime?q=" + URLEncoder.encode(searchTerm, StandardCharsets.UTF_8);
    }

    private String buildAniDBApiUri(String relatedGenres) {
    String endpoint = "http://api.anidb.net:9001/httpapi";
    String client = "your-client-name";
    String clientver = "1";
    String protover = "1";
    String apiKey = "your-anidb-api-key-here";  // Your API key for AniDB

    String queryParams = String.format("client=%s&clientver=%s&protover=%s&apikey=%s&request=anime&title=%s",
                                       client, clientver, protover, apiKey,
                                       URLEncoder.encode(relatedGenres, StandardCharsets.UTF_8));

    return endpoint + "?" + queryParams;
}

private void displayAnimeResults(AnimeSearchResponse animeResponse) {
   Platform.runLater(new Runnable() {
        @Override
        public void run() {
            root.getChildren().clear();
            Label resultsLabel = new Label("Anime Search Results:");
            root.getChildren().add(resultsLabel);
            for (AnimeResult result : animeResponse.getResults()) {
                Label titleLabel = new Label(result.getTitle());
                Image image = new Image(result.getImageUrl(), true);
                ImageView imageView = new ImageView(image);
                imageView.setFitHeight(100);
                imageView.setPreserveRatio(true);
                VBox vbox = new VBox(5, titleLabel, imageView);
                root.getChildren().add(vbox);
            }
        }
    });
}

    private void searchAniDB(String relatedGenres) {
         String aniDbSearchUri = buildAniDBApiUri(relatedGenres);
    HttpClient client = HttpClient.newHttpClient();
    HttpRequest aniDbRequest = HttpRequest.newBuilder()
                                          .uri(URI.create(aniDbSearchUri))
                                          .build();

    client.sendAsync(aniDbRequest, HttpResponse.BodyHandlers.ofString())
          .thenApply(response -> response.body())
          .thenApply(jsonResponse -> {
              try {
                  return new Gson().fromJson(jsonResponse, AniDBSearchResponse.class);
              } catch (Exception e) {
                  displayErrorMessage("Failed to parse AniDB results");
                  return null; // Returning null to handle this in subsequent steps.
              }
          })
          .thenAccept(aniDbResponse -> {
              if (aniDbResponse != null) {
                  displayAniDBResults(aniDbResponse);
              } else {
                  // Handle null case or update UI to show an error message
                  displayErrorMessage("AniDB data could not be loaded.");
              }
          });
}

private void displayAniDBResults(AniDBSearchResponse response) {
   Platform.runLater(() -> {
            for (AnimeDetail detail : response.getAnimeDetails()) {
                Label titleLabel = new Label("Title: " + detail.getTitle());
                Label descriptionLabel = new Label("Description: " + detail.getDescription());
                VBox vbox = new VBox(5, titleLabel, descriptionLabel);
                root.getChildren().add(vbox);
            }
        });
}


// This method would be used to display an error message in the UI
    private void displayErrorMessage(String message) {
       Platform.runLater(new Runnable() {
        @Override
        public void run() {
            root.getChildren().clear();
            Label errorLabel = new Label(message);
            root.getChildren().add(errorLabel);
        }
    });
    }



    /** {@inheritDoc} */
    @Override
    public void start(Stage stage) {

        this.stage = stage;

        TextField searchField = new TextField();
    searchField.setPromptText("Type in an anime name");

    Button searchButton = new Button("Search");
    searchButton.setOnAction(event -> performApiRequests(searchField.getText()));

    HBox searchBox = new HBox(searchField, searchButton);
    searchBox.setSpacing(10);
    searchField.setPrefWidth(300); // set the preferred width of the search bar
    searchButton.setPrefSize(100, 20);

    Label welcomeText = new Label("Welcome to the anime channel search area. Type in an anime name in the search bar.");


        root.getChildren().addAll(welcomeText, searchBox);
        scene = new Scene(root,800, 600);

        // setup stage
        stage.setTitle("Anime Search App");
        stage.setScene(scene);
        stage.setOnCloseRequest(event -> Platform.exit());
        stage.sizeToScene();
        stage.show();

    } // start

} // ApiApp
