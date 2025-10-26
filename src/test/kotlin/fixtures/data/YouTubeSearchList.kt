package fixtures.data

import util.jsonify

fun youTubeSearchListWithNoResults() = """{
    "kind": "youtube#playlistItemListResponse",
    "etag": "6RWuhXexRX_p_SgwX2svaG35X7Y",
    "nextPageToken": null,
    "pageInfo": {
    "totalResults": 10,
    "resultsPerPage": 5
    },
    "items": []
}""".trimMargin()

fun youTubeSearchList(id: String, next: String? = null) = """{
  "kind": "youtube#playlistItemListResponse",
  "etag": "6RWuhXexRX_p_SgwX2svaG35X7Y",
  "nextPageToken": ${next.jsonify()},
  "pageInfo": {
    "totalResults": 10,
    "resultsPerPage": 5
  },
  "items": [
    {
      "kind": "youtube#playlistItem",
      "etag": "biKiru_-t7KsuiDJ4UA3tfz-biI",
      "id": {
        "kind": "youtube#video",
        "videoId": "$id"
      },
      "snippet": {
        "publishedAt": "2025-04-29T10:04:44.04884Z",
        "channelId": "UCjg5Kq3qiHNmsxBeY2z-2gQ",
        "title": "Against The Current - burn it down",
        "description": "Song title",
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
        "channelTitle": "Against The Current"
      }
    }
  ]
}""".trimIndent()