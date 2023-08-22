package tv.banko.songrequest.spotify;

import org.jetbrains.annotations.NotNull;
import tv.banko.songrequest.SongRequest;

import java.util.concurrent.CompletableFuture;

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
    public CompletableFuture<Boolean> addSongToQueue(@NotNull String spotifyTrackId) {
        return this.api.addSongToQueue(this.request.getConfig().getSpotifyAccessToken(), spotifyTrackId);
    }

    /**
     * Skip the playing song.
     *
     * @return A completable future which contains a true boolean when the execution was successful.
     */
    public CompletableFuture<Boolean> skipSong() {
        return this.api.skipSong(this.request.getConfig().getSpotifyAccessToken());
    }

    /**
     * Get the track id.
     * When the query starts with "spotify:track:", the track id is used.
     * When the query contains open.spotify.com/track/, the track id within the url is used.
     *
     * @param query The query entered by the user.
     * @return A completable future which contains the track id (spotify:track:<ID>) string when the execution was successful.
     */
    public CompletableFuture<String> getTrackId(@NotNull String query) {
        if (query.startsWith("spotify:track:")) {
            return CompletableFuture.completedFuture(query.split(" ")[0]);
        }

        if (query.contains("://open.spotify.com/")) {
            String trackId = query.split("/track/")[1].split("\\?")[0];
            return CompletableFuture.completedFuture("spotify:track:" + trackId);
        }

        return this.api.searchTrack(this.request.getConfig().getSpotifyAccessToken(), query);
    }

    public SongRequest getRequest() {
        return request;
    }

    public SpotifyAPI getAPI() {
        return api;
    }
}
