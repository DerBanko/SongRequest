package tv.banko.songrequest.spotify;

import org.jetbrains.annotations.NotNull;
import tv.banko.songrequest.SongRequest;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class Spotify {

    private final SongRequest request;
    private final SpotifyAPI api;

    public Spotify(@NotNull SongRequest request) {
        this.request = request;
        this.api = new SpotifyAPI(this);
    }

    /**
     * Add a song to the queue.
     *
     * @param spotifyTrackId The id of the track (spotify:track:<ID>).
     * @return A completable future which contains a true boolean when the execution was successful.
     */
    public CompletableFuture<Object> addSongToQueue(@NotNull String spotifyTrackId) {
        return this.api.addSongToQueue(spotifyTrackId);
    }

    /**
     * Skip the playing song.
     *
     * @return A completable future which contains a true boolean when the execution was successful.
     */
    public CompletableFuture<Object> skipSong() {
        return this.api.skipSong();
    }

    /**
     * Start the playback.
     *
     * @return A completable future which contains a true boolean when the execution was successful.
     */
    public CompletableFuture<Object> startPlayback() {
        return this.api.startPlayback();
    }

    /**
     * Pause the playback.
     *
     * @return A completable future which contains a true boolean when the execution was successful.
     */
    public CompletableFuture<Object> pausePlayback() {
        return this.api.pausePlayback();
    }

    /**
     * Skip to the previously playing song.
     *
     * @return A completable future which contains a true boolean when the execution was successful.
     */
    public CompletableFuture<Object> playLastSong() {
        return this.api.playLastSong();
    }

    /**
     * Skip ten seconds.
     *
     * @return A completable future which contains a true boolean when the execution was successful.
     */
    public CompletableFuture<Object> skipTenSeconds() {
        return this.api.offsetPlaybackProgress((int) TimeUnit.SECONDS.toMillis(10));
    }

    /**
     * Play the last ten seconds.
     *
     * @return A completable future which contains a true boolean when the execution was successful.
     */
    public CompletableFuture<Object> playLastTenSeconds() {
        return this.api.offsetPlaybackProgress((-1) * (int) TimeUnit.SECONDS.toMillis(10));
    }

    /**
     * Get the track id.
     * When the query starts with "spotify:track:", the track id is used.
     * When the query contains open.spotify.com/track/, the track id within the url is used.
     *
     * @param query The query entered by the user.
     * @return A completable future which contains the track id (spotify:track:<ID>) string when the execution was successful.
     */
    public CompletableFuture<Object> getTrackId(@NotNull String query) {
        if (query.startsWith("spotify:track:")) {
            return CompletableFuture.completedFuture(query.split(" ")[0]);
        }

        if (query.contains("://open.spotify.com/")) {
            String trackId = query.split("/track/")[1].split("\\?")[0];
            return CompletableFuture.completedFuture("spotify:track:" + trackId);
        }

        return this.api.searchTrack(query);
    }

    public SongRequest getRequest() {
        return request;
    }

    public SpotifyAPI getAPI() {
        return api;
    }
}
