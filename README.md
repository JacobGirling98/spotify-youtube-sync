# spotify-youtube-sync

## To Do
1. Pull in playlists from Spotify
2. Idea of an adapter for a certain service - e.g. for a song, the adapter contains information on finding the song (ids) 
3. Create song dictionary with song and the adapters for each service
4. Populate songs + Spotify adapters
5. Pull in playlists from YouTube
6. Update song dictionary based on YouTube adapters

### Research
- The Spotify and YouTube APIs, how to get playlist, song information etc, to get an idea of data structures


### Spotify Flow
1. /me/playlists - get a list of my playlists, need id and name
2. /playlists/{id}/tracks - get a list of tracks in each playlist
3. Populate song dictionary with songs and their adapters - the adapter would just be a Map<Service (e.g. spotify), Id>
4. Return list of playlists (pure domain)

### YouTube Flow
1. /v3/playlists?part=snippet&mine=true - get a list of my playlists, need id and name
2. /v3/playlistsItems - get a list of tracks in each playlist
3. Populate song dictionary with songs and their adapters
4. Return list of playlists (pure domain)