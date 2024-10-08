package tv.banko.songrequest;

import tv.banko.songrequest.config.Config;
import tv.banko.songrequest.spotify.Spotify;
import tv.banko.songrequest.twitch.Twitch;

public class SongRequest {

    private final Config config;
    private final Spotify spotify;
    private final Twitch twitch;

    public SongRequest() {
        this.config = new Config();
        this.spotify = new Spotify(this);
        this.twitch = new Twitch(this);
    }

    public Config getConfig() {
        return config;
    }

    public Spotify getSpotify() {
        return spotify;
    }

    public Twitch getTwitch() {
        return twitch;
    }
}
