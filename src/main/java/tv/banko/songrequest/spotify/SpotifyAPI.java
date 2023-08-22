package tv.banko.songrequest.spotify;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;
import tv.banko.songrequest.config.Config;
import tv.banko.songrequest.spotify.api.SpotifyAccessToken;
import tv.banko.songrequest.spotify.api.SpotifyPlayerAction;
import tv.banko.songrequest.spotify.api.SpotifySearchTrack;
import tv.banko.songrequest.util.HTTPMethod;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.util.Base64;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;

public class SpotifyAPI {

    private final Spotify spotify;
    private final OkHttpClient client;

    private final SpotifyPlayerAction playerAction;
    private final SpotifySearchTrack searchTrack;
    private final SpotifyAccessToken accessToken;

    public SpotifyAPI(@NotNull Spotify spotify) {
        this.spotify = spotify;
        this.client = new OkHttpClient();
        this.playerAction = new SpotifyPlayerAction();
        this.searchTrack = new SpotifySearchTrack();
        this.accessToken = new SpotifyAccessToken(this);
    }

    public Spotify getSpotify() {
        return spotify;
    }

    /**
     * Add a song to the queue.
     *
     * @param spotifyTrackId The id of the track (spotify:track:<ID>).
     * @return A completable future which contains a true boolean when the execution was successful.
     */
    public CompletableFuture<Boolean> addSongToQueue(@NotNull String accessToken, @NotNull String spotifyTrackId) {
        return this.playerAction.addSongToQueue(this.client, accessToken, spotifyTrackId);
    }

    /**
     * Skip the playing song.
     *
     * @return A completable future which contains a true boolean when the execution was successful.
     */
    public CompletableFuture<Boolean> skipSong(@NotNull String accessToken) {
        return this.playerAction.skipSong(this.client, accessToken);
    }

    /**
     * Search for a specific track.
     *
     * @param query The query for the song.
     * @return A completable future which contains the track id (spotify:track:<ID>) string when the execution was successful.
     */
    public CompletableFuture<String> searchTrack(@NotNull String accessToken, @NotNull String query) {
        return this.searchTrack.execute(this.client, accessToken, query);
    }

    /**
     * Set the authorization information from an authorization code.
     *
     * @param code The authorization code from the api request executed within the user's browser.
     * @return A completable future which contains a true boolean when the execution was successful.
     */
    public CompletableFuture<SpotifyAccessToken.Response> getAuthorizationFromCode(@NotNull String code) {
        return this.accessToken.getAccessToken(this.client, code);
    }

    /**
     * Regenerate the authorization token using the refresh token from the config.
     *
     * @return A completable future which contains the access token when the execution was successful.
     */
    public CompletableFuture<SpotifyAccessToken.Response> regenerateAccessToken(@NotNull String refreshToken) {
        return this.accessToken.regenerateToken(this.client, refreshToken);
    }
}
