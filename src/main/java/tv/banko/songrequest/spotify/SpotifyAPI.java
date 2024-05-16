package tv.banko.songrequest.spotify;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;
import tv.banko.songrequest.config.Config;
import tv.banko.songrequest.util.HTTPMethod;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.BiConsumer;

public class SpotifyAPI {

    private final Spotify spotify;
    private final OkHttpClient client;

    public SpotifyAPI(@NotNull Spotify spotify) {
        this.spotify = spotify;
        this.client = new OkHttpClient();
    }

    /**
     * Add a song to the queue.
     *
     * @param spotifyTrackId The id of the track (spotify:track:<ID>).
     * @return A completable future which contains a true boolean when the execution was successful.
     */
    public CompletableFuture<Object> addSongToQueue(@NotNull String spotifyTrackId) {
        String url = MessageFormat.format("https://api.spotify.com/v1/me/player/queue?uri={0}",
                spotifyTrackId);
        return this.sendNoResponseBodyRequest(url);
    }

    /**
     * Skip the playing song.
     *
     * @return A completable future which contains a true boolean when the execution was successful.
     */
    public CompletableFuture<Object> skipSong() {
        String url = "https://api.spotify.com/v1/me/player/next";
        return this.sendNoResponseBodyRequest(url);
    }

    /**
     * Skip to the previously playing song.
     *
     * @return A completable future which contains a true boolean when the execution was successful.
     */
    public CompletableFuture<Object> playLastSong() {
        String url = "https://api.spotify.com/v1/me/player/previous";
        return this.sendNoResponseBodyRequest(url);
    }

    /**
     * Start the playback.
     *
     * @return A completable future which contains a true boolean when the execution was successful.
     */
    public CompletableFuture<Object> startPlayback() {
        String url = "https://api.spotify.com/v1/me/player/play";
        return this.sendNoResponseBodyRequest(url, HTTPMethod.PUT);
    }

    /**
     * Pause the playback.
     *
     * @return A completable future which contains a true boolean when the execution was successful.
     */
    public CompletableFuture<Object> pausePlayback() {
        String url = "https://api.spotify.com/v1/me/player/pause";
        return this.sendNoResponseBodyRequest(url, HTTPMethod.PUT);
    }

    /**
     * Offset the playback progress.
     *
     * @return A completable future which contains a true boolean when the execution was successful.
     */
    public CompletableFuture<Boolean> offsetPlaybackProgress(int offset) {
        CompletableFuture<Boolean> future = new CompletableFuture<>();
        String currentURL = "https://api.spotify.com/v1/me/player/currently-playing";
        String offsetURL = "https://api.spotify.com/v1/me/player/seek?position_ms={0}";

        this.sendRequest(currentURL, HTTPMethod.GET, (response, currentFuture) -> {
            if (currentFuture.isCompletedExceptionally()) {
                try {
                    currentFuture.get();
                } catch (Exception e) {
                    future.completeExceptionally(e);
                }
                return;
            }

            if (response.body() == null) {
                future.completeExceptionally(new RuntimeException("Error code " + response.code() + ": null"));
                return;
            }

            if (response.code() != 200) {
                try {
                    future.completeExceptionally(new RuntimeException("Error code " + response.code() + ": " + response.body().string()));
                } catch (IOException e) {
                    future.completeExceptionally(e);
                }
                return;
            }

            try {
                JsonObject object = JsonParser.parseString(response.body().string()).getAsJsonObject();
                int progress = Math.max(object.get("progress_ms").getAsInt() + offset, 0);

                this.sendNoResponseBodyRequest(MessageFormat.format(offsetURL, "" + progress), HTTPMethod.PUT)
                        .whenCompleteAsync((o, throwable) -> {
                            if (throwable != null) {
                                future.completeExceptionally(throwable);
                                return;
                            }
                            future.complete((Boolean) o);
                        });
            } catch (IOException e) {
                future.completeExceptionally(e);
            }
        });

        return future;
    }

    /**
     * Add a song to the playlist.
     *
     * @param spotifyTrackId The id of the track (spotify:track:<ID>).
     */
    public void addToPlaylist(String spotifyTrackId) {
        String url = MessageFormat.format("https://api.spotify.com/v1/playlists/{0}/tracks?uris={1}",
                this.spotify.getRequest().getConfig().getSpotifyPlaylistID(), spotifyTrackId);
        this.sendNoResponseBodyRequest(url, HTTPMethod.POST).whenCompleteAsync((o, throwable) -> {
            if (throwable != null) {
                System.out.println("Error while adding song to playlist: " + throwable.getMessage());
                return;
            }
            System.out.println(o);
        });
    }

    /**
     * Gets the next 5 songs from the queue.
     *
     * @return A completable future which contains a List of Strings with the song's name and the artist's name.
     */
    public CompletableFuture<List<String>> getQueue() {
        CompletableFuture<List<String>> future = new CompletableFuture<>();

        this.sendRequest("https://api.spotify.com/v1/me/player/queue", HTTPMethod.GET, (response, currentFuture) -> {
            if (currentFuture.isCompletedExceptionally()) {
                try {
                    currentFuture.get();
                } catch (Exception e) {
                    future.completeExceptionally(e);
                }
                return;
            }

            if (response.body() == null) {
                future.completeExceptionally(new RuntimeException("Error code " + response.code() + ": null"));
                return;
            }

            if (response.code() != 200) {
                try {
                    future.completeExceptionally(new RuntimeException("Error code " + response.code() + ": " + response.body().string()));
                } catch (IOException e) {
                    future.completeExceptionally(e);
                }
                return;
            }

            try {
                JsonObject object = JsonParser.parseString(response.body().string()).getAsJsonObject();

                if (!object.has("queue")) {
                    future.complete(Collections.emptyList());
                    return;
                }

                List<String> list = new ArrayList<>();

                JsonArray queue = object.getAsJsonArray("queue");

                for (int queueId = 0; queueId < Math.min(queue.size(), 5); queueId++) {
                    StringBuilder builder = new StringBuilder();
                    JsonObject queueObject = queue.get(queueId).getAsJsonObject();

                    builder.append(queueObject.get("name")).append(" - ");
                    JsonArray artists = queueObject.getAsJsonArray("artists");

                    for (int artistId = 0; artistId < artists.size(); artistId++) {
                        if (artistId != 0) {
                            builder.append(", ");
                        }

                        JsonObject artistObject = artists.get(artistId).getAsJsonObject();
                        builder.append(artistObject.get("name").getAsString());
                    }

                    list.add(builder.toString());
                }

                future.complete(list);
            } catch (IOException e) {
                future.completeExceptionally(e);
            }
        });

        return future;
    }

    /**
     * Search for a specific track.
     *
     * @param query The query for the song.
     * @return A completable future which contains the track id (spotify:track:<ID>) string when the execution was successful.
     */
    public CompletableFuture<Object> searchTrack(@NotNull String query) {
        String url = MessageFormat.format("https://api.spotify.com/v1/search?q={0}&type={1}",
                query, "track");

        return this.sendRequest(url, HTTPMethod.GET, (response, future) -> {
            if (future.isCompletedExceptionally()) {
                try {
                    future.get();
                } catch (Exception e) {
                    future.completeExceptionally(e);
                }
                return;
            }

            if (response.body() == null) {
                future.completeExceptionally(new RuntimeException("Error code " + response.code() + ": null"));
                return;
            }

            if (response.code() != 200) {
                try {
                    future.completeExceptionally(new RuntimeException("Error code " + response.code() + ": " + response.body().string()));
                } catch (IOException e) {
                    future.completeExceptionally(e);
                }
                return;
            }

            try {
                JsonObject object = JsonParser.parseString(response.body().string()).getAsJsonObject();
                JsonObject tracks = object.getAsJsonObject("tracks");
                JsonArray items = tracks.getAsJsonArray("items");

                if (items.size() == 0) {
                    future.completeExceptionally(new NullPointerException("No tracks found"));
                    return;
                }

                JsonObject item = items.get(0).getAsJsonObject();
                String id = item.get("id").getAsString();
                future.complete("spotify:track:" + id);
            } catch (IOException e) {
                future.completeExceptionally(e);
            }
        });
    }

    /**
     * Set the authorization information from an authorization code.
     *
     * @param code The authorization code from the api request executed within the user's browser.
     * @return A completable future which contains a true boolean when the execution was successful.
     */
    public CompletableFuture<Object> setAuthorizationFromCode(@NotNull String code) {
        CompletableFuture<Object> future = new CompletableFuture<>();

        Config config = this.spotify.getRequest().getConfig();
        String url = MessageFormat.format("https://accounts.spotify.com/api/token" +
                        "?code={0}&redirect_uri={1}&grant_type={2}",
                code, config.getSpotifyRedirectURI(), "authorization_code");
        String basicAuth = Base64.getEncoder().encodeToString((config.getSpotifyClientID() + ":"
                + config.getSpotifyClientSecret()).getBytes(StandardCharsets.UTF_8));

        Request.Builder builder = new Request.Builder()
                .url(url)
                .header("Authorization", "Basic " + basicAuth)
                .header("Content-Type", "application/x-www-form-urlencoded")
                .post(RequestBody.create(new byte[0]));

        this.client.newCall(builder.build()).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                future.completeExceptionally(e);
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                if (response.body() == null) {
                    future.completeExceptionally(new RuntimeException("Error code " + response.code() + ": null"));
                    return;
                }

                if (response.code() != 200) {
                    future.completeExceptionally(new RuntimeException("Error code " + response.code() + ": " + response.body().string()));
                    return;
                }

                try {
                    JsonObject object = JsonParser.parseString(response.body().string()).getAsJsonObject();

                    config.setValue("spotify.token", "Bearer " + object.get("access_token").getAsString());
                    config.setValue("spotify.expiresAt", System.currentTimeMillis()
                            + (object.get("expires_in").getAsInt() * 1000L));
                    config.setValue("spotify.refreshToken", object.get("refresh_token").getAsString());

                    future.complete(true);
                } catch (IOException e) {
                    future.completeExceptionally(e);
                }
            }
        });
        return future;
    }

    /**
     * Regenerate the authorization token using the refresh token from the config.
     *
     * @return A completable future which contains the access token when the execution was successful.
     */
    private CompletableFuture<Object> regenerateAccessToken() {
        CompletableFuture<Object> future = new CompletableFuture<>();

        Config config = this.spotify.getRequest().getConfig();
        String url = MessageFormat.format("https://accounts.spotify.com/api/token" +
                        "?refresh_token={0}&grant_type={1}",
                config.getSpotifyRefreshToken(), "refresh_token");
        String basicAuth = Base64.getEncoder().encodeToString((config.getSpotifyClientID() + ":"
                + config.getSpotifyClientSecret()).getBytes(StandardCharsets.UTF_8));

        Request.Builder builder = new Request.Builder()
                .url(url)
                .header("Authorization", "Basic " + basicAuth)
                .header("Content-Type", "application/x-www-form-urlencoded")
                .post(RequestBody.create(new byte[0]));

        this.client.newCall(builder.build()).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                future.completeExceptionally(e);
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                if (response.body() == null) {
                    future.completeExceptionally(new RuntimeException("Error code " + response.code() + ": null"));
                    return;
                }

                if (response.code() != 200) {
                    future.completeExceptionally(new RuntimeException("Error code " + response.code() + ": " + response.body().string()));
                    return;
                }

                try {
                    JsonObject object = JsonParser.parseString(response.body().string()).getAsJsonObject();

                    config.setValue("spotify.token", "Bearer " + object.get("access_token").getAsString());
                    config.setValue("spotify.expiresAt", System.currentTimeMillis()
                            + (object.get("expires_in").getAsInt() * 1000L));

                    future.complete(config.getSpotifyAccessToken());
                } catch (IOException e) {
                    future.completeExceptionally(e);
                }
            }
        });

        return future;
    }

    /**
     * Send a request with the post method and no response body when the execution was successful.
     *
     * @param url The url of the api endpoint.
     * @return A completable future which contains a true boolean when the execution was successful.
     */
    private CompletableFuture<Object> sendNoResponseBodyRequest(@NotNull String url, @NotNull HTTPMethod method) {
        return this.sendRequest(url, method, (response, future) -> {
            if (future.isDone()) {
                return;
            }

            if (response.code() < 200 || response.code() > 299) {
                try {
                    assert response.body() != null;
                    future.completeExceptionally(new RuntimeException("Error code " + response.code() + ": " + response.body().string()));
                } catch (IOException e) {
                    future.completeExceptionally(e);
                }
                return;
            }

            future.complete(true);
        });
    }

    /**
     * Send a request with the post method and no response body when the execution was successful.
     *
     * @param url The url of the api endpoint.
     * @return A completable future which contains a true boolean when the execution was successful.
     */
    private CompletableFuture<Object> sendNoResponseBodyRequest(@NotNull String url) {
        return this.sendNoResponseBodyRequest(url, HTTPMethod.POST);
    }

    /**
     * Send a request to a specific api endpoint.
     *
     * @param url      The url of the api endpoint.
     * @param method   The method used to access the endpoint.
     * @param consumer The response handling.
     * @return A completable future which may be already completed with an exception; otherwise not completed yet.
     */
    private CompletableFuture<Object> sendRequest(@NotNull String url, @NotNull HTTPMethod method,
                                                  @NotNull BiConsumer<Response, CompletableFuture<Object>> consumer) {
        CompletableFuture<Object> future = new CompletableFuture<>();
        this.getAccessToken().whenCompleteAsync((accessToken, throwable) -> {
            if (throwable != null) {
                future.completeExceptionally(throwable);
                return;
            }

            Request.Builder builder = new Request.Builder()
                    .url(url)
                    .header("Authorization", this.spotify.getRequest().getConfig().getSpotifyAccessToken());

            switch (method) {
                case POST -> builder.post(RequestBody.create(new byte[0]))
                        .header("Content-Type", "application/x-www-form-urlencoded");
                case PUT -> builder.put(RequestBody.create(new byte[0]))
                        .header("Content-Type", "application/x-www-form-urlencoded");
                case GET -> builder.get();
            }

            this.client.newCall(builder.build()).enqueue(new Callback() {
                @Override
                public void onFailure(@NotNull Call call, @NotNull IOException e) {
                    future.completeExceptionally(e);
                }

                @Override
                public void onResponse(@NotNull Call call, @NotNull Response response) {
                    consumer.accept(response, future);
                }
            });
        });
        return future;
    }

    /**
     * Get the access token of the user.
     * May be regenerated when the access token expires in less than 5 seconds or is already expired.
     *
     * @return A completable future which contains the access token when the execution was successful.
     */
    private CompletableFuture<String> getAccessToken() {
        Config config = this.spotify.getRequest().getConfig();

        if ((config.getSpotifyExpiresAt() - 5000) > System.currentTimeMillis()) {
            return CompletableFuture.completedFuture(config.getSpotifyAccessToken());
        }

        CompletableFuture<String> future = new CompletableFuture<>();

        this.regenerateAccessToken().whenCompleteAsync((o, throwable) -> {
            if (throwable != null) {
                future.completeExceptionally(throwable);
                return;
            }
            future.complete(config.getSpotifyAccessToken());
        });

        return future;
    }

}
