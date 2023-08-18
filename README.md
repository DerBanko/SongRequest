# SongRequest

A song request system which helps you manage your song requests for Spotify via the channel points system.

## Features

- Add songs to your Spotify queue using **Twitch Channel Points rewards**.
- Skip songs using **Twitch Channel Points rewards**.
- Self-hosted (maybe there will be a cloud solution in the future, but not yet)

## Prerequisites

- Create a twitch application [here](https://dev.twitch.tv/console/apps/create).
  - Note: You can use `https://localhost:8080` as the redirect url.
  - Select `Chat Bot` as the category.
- Create a spotify application [here](https://developer.spotify.com/dashboard/create).
  - Note: You can use `https://localhost:8080` as the redirect url.
- Copy all client ids, client secrets (you may have to create one or show it) and Redirect URLs 

## How to use

1. Start the application; stop it afterward.
2. Open the newly created config.json file.
3. Enter all the data you already know from your applications copied above in the right fields. Additionally, add the Twitch channel name and change the reward names.
    - Hint: To completely disable specific rewards, set the name of the reward to `""`.
4. Copy the following link and replace all placeholders with the data from your **Twitch** application: 
`https://id.twitch.tv/oauth2/authorize?scope=channel:read:redemptions+channel:manage:redemptions+chat:edit+chat:read&redirect_uri=[REDIRECT_URI]&client_id=[CLIENT_ID]&response_type=code`
5. Authorize and copy the code from your browser's url.
6. Execute the following cURL command: `curl -XPOST 'https://id.twitch.tv/oauth2/token?client_id=[CLIENT_ID]&client_secret=[CLIENT_SECRET]&code=[CODE]&grant_type=authorization_code&redirect_uri=[REDIRECT_URI]'`
7. Replace the `TOKEN` from `twitch.token` in the config.json file with the value of `access_token` of the response. 
8. Replace the `REFRESH_TOKEN` from `twitch.refreshToken` in the config.json file with the value of `refresh_token` of the response. 
9. Start the application again. It should start without any issues.
10. To specify a spotify account, Copy the following link and replace all placeholders with the data from your **Spotify** application:
`https://accounts.spotify.com/authorize?response_type=code&client_id=[CLIENT_ID]&scope=user-read-playback-state%20user-modify-playback-state&redirect_uri=[REDIRECT_URI]`
11. Authorize and copy the code from your browser's url. (It is longer than the twitch code, watch out)
12. Enter `!sr-spotify CODE` in the your Twitch channel's chat (replace `CODE` with the code from the browser url).
13. You are ready to go. Try using the rewards :)

## Support

If you need help setting up this service, feel free to check out my [Twitch channel](https://twitch.tv/DerBanko).
Otherwise, you can join my [Discord server](https://banko.tv/discord) in order to ask for help. _(I have a german community, but you are welcome anyway)_


## Contribute

Contribution is welcome, but only under the [LICENSE](https://github.com/DerBanko/SongRequest/blob/main/LICENSE).