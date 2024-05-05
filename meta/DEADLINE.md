# Deadline

Modify this file to satisfy a submission requirement related to the project
deadline. Please keep this file organized using Markdown. If you click on
this file in your GitHub repository website, then you will see that the
Markdown is transformed into nice-looking HTML.

## Part 1.1: App Description

> Please provide a friendly description of your app, including
> the primary functions available to users of the app. Be sure to
> describe exactly what APIs you are using and how they are connected
> in a meaningful way.
> **Also, include the GitHub `https` URL to your repository.**

The "Weather Mood Dog Viewer" creatively combines real-time weather data with dog images to reflect the mood associated with
 the current weather conditions. This unique approach transforms routine weather checks into a more engaging experience.

### Primary Functions and API Integration:

- **City Input Field**: Users input a city to initiate a search.
- **Fetch Button**: Triggers the weather data retrieval from the weather API based on the city provided.
- **Progress Bar**: Indicates the progress of API requests, enhancing user interaction.
- **Dynamic Image Display**: After fetching weather data, the app uses the weather condition to fetch a corresponding dog image
    from the HTTP Dog API that matches the mood of the weather (e.g., a happy dog on sunny days).
- **Alerts and Notifications**: Provides feedback on user actions or API issues, ensuring a smooth user interface.
- **Responsive UI**: Adapts to various devices for a consistent user experience.

The APIs are integrated such that the response from the weather API, specifically the weather condition, influences the next request to the HTTP Dog API, effectively linking these two APIS to provide a connected output.

GitHub Url:    https://github.com/sk45716/cs1302-api-app.git

## Part 1.2: APIs

> For each RESTful JSON API that your app uses (at least two are required),
> include an example URL for a typical request made by your app. If you
> need to include additional notes (e.g., regarding API keys or rate
> limits), then you can do that below the URL/URI. Placeholders for this
> information are provided below. If your app uses more than two RESTful
> JSON APIs, then include them with similar formatting.

### API 1: weather-api

``` https://goweather.herokuapp.com/weather/{city}

``` This API provides live weather forecasts, incuding temperature, wind, and  atmospheric conditions, which are important
    for determining the mood of the dog images displayed in the app. No API key is required, and no rate limits are
    explicitly documented in this API.
### API 2: HTTP Dog API

``` https://http.dog/{status_code}.jpg

``` This API is used to fetch images of dogs that correspond to different weather conditions seperated by particular HTTP status codes,
    creatively linking weather moods with dog expressions. Like the Weather API, it does not require an API key and does not document
     any rate limits.

## Part 2: New

> What is something new and/or exciting that you learned from working
> on this project?

    From working on the project, I learned how to integrate two APIS that aren't typically associated with each other in a creative and
    engaging manner. Handling asynchronous data fetching and dynamically updating the user interface based on live data were important
    learning points.


## Part 3: Retrospect

> If you could start the project over from scratch, what do
> you think might do differently and why?

If I were to restart the project, I would focus more on designing a more complex user interface and possibly adding more interactive
    elements that would allow users to customize the experience. I would also implement better error handling and feedback mechanisms
    to handle API limitations or failures.  This makes sure that there would be a smoother user experience.
