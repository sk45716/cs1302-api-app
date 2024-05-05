package cs1302.api;

/**
 * Represents a response from the HTTP Dog API.
 */
public class HttpDogResponse {

    private String url;

    /**
     * Gets the URL of the dog image.
     *
     * @return the URL of the dog image
     */
    public String getUrl() {
        return url;
    }

    /**
     * Sets the URL of the dog image.
     *
     * @param url the URL to set
     */
    public void setUrl(String url) {
        this.url = url;
    }
}
