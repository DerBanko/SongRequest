# SongRequest

A song request system which helps you 

## How to use

1. Start the application; stop it afterward.
2. Open the newly created config.json file.
3. Copy the following link and replace all placeholders with the data from your application: 
https://id.twitch.tv/oauth2/authorize?scope=channel:read:redemptions+channel:manage:redemptions+chat:edit+chat:read&redirect_uri=[REDIRECT_URI]&client_id=[CLIENT_ID]&response_type=code
4. Authorize and copy the code from your browser's url.
5. Execute the following cURL command: 