package fixtures

import util.jsonify

fun youTubeCurrentUserPlaylists(id: String, name: String, next: String? = null) = """{
  "kind": "youtube#playlistListResponse",
  "etag": "6RWuhXexRX_p_SgwX2svaG35X7Y",
  "nextPageToken": ${next.jsonify()},
  "pageInfo": {
    "totalResults": 10,
    "resultsPerPage": 5
  },
  "items": [
    {
      "kind": "youtube#playlist",
      "etag": "biKiru_-t7KsuiDJ4UA3tfz-biI",
      "id": "$id",
      "snippet": {
        "publishedAt": "2025-04-29T10:04:44.04884Z",
        "channelId": "UCjg5Kq3qiHNmsxBeY2z-2gQ",
        "title": "$name",
        "description": "This playlist was created by https://www.tunemymusic.com that lets you transfer your playlist to YouTubeMediaConnect from any music platform such as Spotify, YouTube, Apple Music etc.",
        "thumbnails": {
          "default": {
            "url": "https://i.ytimg.com/vi/4SxMxOOooBI/default.jpg",
            "width": 120,
            "height": 90
          },
          "medium": {
            "url": "https://i.ytimg.com/vi/4SxMxOOooBI/mqdefault.jpg",
            "width": 320,
            "height": 180
          },
          "high": {
            "url": "https://i.ytimg.com/vi/4SxMxOOooBI/hqdefault.jpg",
            "width": 480,
            "height": 360
          },
          "standard": {
            "url": "https://i.ytimg.com/vi/4SxMxOOooBI/sddefault.jpg",
            "width": 640,
            "height": 480
          },
          "maxres": {
            "url": "https://i.ytimg.com/vi/4SxMxOOooBI/maxresdefault.jpg",
            "width": 1280,
            "height": 720
          }
        },
        "channelTitle": "Jacob Girling",
        "localized": {
          "title": "Current Groove",
          "description": "This playlist was created by https://www.tunemymusic.com that lets you transfer your playlist to YouTubeMediaConnect from any music platform such as Spotify, YouTube, Apple Music etc."
        }
      }
    }
  ]
}""".trimIndent()
