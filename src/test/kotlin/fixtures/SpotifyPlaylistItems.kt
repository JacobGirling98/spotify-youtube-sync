package fixtures

import util.jsonify

fun spotifyPlaylistItems(songName: String, artist: String, id: String, next: String? = null) = """{
  "href": "https://api.spotify.com/v1/playlists/3cEYpjA9oz9GiPac4AsH4n/tracks?offset=0&limit=100&locale=en-GB,en-US;q%3D0.9,en;q%3D0.8",
  "limit": 100,
  "next": ${next.jsonify()},
  "offset": 0,
  "previous": null,
  "total": 1,
  "items": [
    {
      "added_at": "2015-01-15T12:39:22Z",
      "added_by": {
        "external_urls": {
          "spotify": "https://open.spotify.com/user/jmperezperez"
        },
        "href": "https://api.spotify.com/v1/users/jmperezperez",
        "id": "jmperezperez",
        "type": "user",
        "uri": "spotify:user:jmperezperez"
      },
      "is_local": false,
      "track": {
        "album": {
          "album_type": "compilation",
          "total_tracks": 20,
          "available_markets": ["AR", "AU", "AT", "BE", "BO", "BR", "BG", "CA", "CL", "CO", "CR", "CY", "CZ", "DK", "DO", "DE", "EC", "EE", "SV", "FI", "FR", "GR", "GT", "HN", "HK", "HU", "IS", "IE", "IT", "LV", "LT", "LU", "MY", "MT", "MX", "NL", "NZ", "NI", "NO", "PA", "PY", "PE", "PH", "PL", "PT", "SG", "SK", "ES", "SE", "CH", "TW", "TR", "UY", "US", "GB", "AD", "LI", "MC", "ID", "JP", "TH", "VN", "RO", "IL", "ZA", "SA", "AE", "BH", "QA", "OM", "KW", "EG", "MA", "DZ", "TN", "LB", "JO", "PS", "IN", "BY", "KZ", "MD", "UA", "AL", "BA", "HR", "ME", "MK", "RS", "SI", "KR", "BD", "PK", "LK", "GH", "KE", "NG", "TZ", "UG", "AG", "AM", "BS", "BB", "BZ", "BT", "BW", "BF", "CV", "CW", "DM", "FJ", "GM", "GE", "GD", "GW", "GY", "HT", "JM", "KI", "LS", "LR", "MW", "MV", "ML", "MH", "FM", "NA", "NR", "NE", "PW", "PG", "PR", "WS", "SM", "ST", "SN", "SC", "SL", "SB", "KN", "LC", "VC", "SR", "TL", "TO", "TT", "TV", "VU", "AZ", "BN", "BI", "KH", "CM", "TD", "KM", "GQ", "SZ", "GA", "GN", "KG", "LA", "MO", "MR", "MN", "NP", "RW", "TG", "UZ", "ZW", "BJ", "MG", "MU", "MZ", "AO", "CI", "DJ", "ZM", "CD", "CG", "IQ", "LY", "TJ", "VE", "ET", "XK"],
          "external_urls": {
            "spotify": "https://open.spotify.com/album/2pANdqPvxInB0YvcDiw4ko"
          },
          "href": "https://api.spotify.com/v1/albums/2pANdqPvxInB0YvcDiw4ko",
          "id": "2pANdqPvxInB0YvcDiw4ko",
          "images": [
            {
              "url": "https://i.scdn.co/image/ab67616d0000b273ce6d0eef0c1ce77e5f95bbbc",
              "height": 640,
              "width": 640
            },
            {
              "url": "https://i.scdn.co/image/ab67616d00001e02ce6d0eef0c1ce77e5f95bbbc",
              "height": 300,
              "width": 300
            },
            {
              "url": "https://i.scdn.co/image/ab67616d00004851ce6d0eef0c1ce77e5f95bbbc",
              "height": 64,
              "width": 64
            }
          ],
          "name": "Progressive Psy Trance Picks Vol.8",
          "release_date": "2012-04-02",
          "release_date_precision": "day",
          "type": "album",
          "uri": "spotify:album:2pANdqPvxInB0YvcDiw4ko",
          "artists": [
            {
              "external_urls": {
                "spotify": "https://open.spotify.com/artist/0LyfQWJT6nXafLPZqxe9Of"
              },
              "href": "https://api.spotify.com/v1/artists/0LyfQWJT6nXafLPZqxe9Of",
              "id": "0LyfQWJT6nXafLPZqxe9Of",
              "name": "$artist",
              "type": "artist",
              "uri": "spotify:artist:0LyfQWJT6nXafLPZqxe9Of"
            }
          ]
        },
        "artists": [
          {
            "external_urls": {
              "spotify": "https://open.spotify.com/artist/6eSdhw46riw2OUHgMwR8B5"
            },
            "href": "https://api.spotify.com/v1/artists/6eSdhw46riw2OUHgMwR8B5",
            "id": "6eSdhw46riw2OUHgMwR8B5",
            "name": "$artist",
            "type": "artist",
            "uri": "spotify:artist:6eSdhw46riw2OUHgMwR8B5"
          }
        ],
        "available_markets": ["AR", "AU", "AT", "BE", "BO", "BR", "BG", "CA", "CL", "CO", "CR", "CY", "CZ", "DK", "DO", "DE", "EC", "EE", "SV", "FI", "FR", "GR", "GT", "HN", "HK", "HU", "IS", "IE", "IT", "LV", "LT", "LU", "MY", "MT", "MX", "NL", "NZ", "NI", "NO", "PA", "PY", "PE", "PH", "PL", "PT", "SG", "SK", "ES", "SE", "CH", "TW", "TR", "UY", "US", "GB", "AD", "LI", "MC", "ID", "JP", "TH", "VN", "RO", "IL", "ZA", "SA", "AE", "BH", "QA", "OM", "KW", "EG", "MA", "DZ", "TN", "LB", "JO", "PS", "IN", "BY", "KZ", "MD", "UA", "AL", "BA", "HR", "ME", "MK", "RS", "SI", "KR", "BD", "PK", "LK", "GH", "KE", "NG", "TZ", "UG", "AG", "AM", "BS", "BB", "BZ", "BT", "BW", "BF", "CV", "CW", "DM", "FJ", "GM", "GE", "GD", "GW", "GY", "HT", "JM", "KI", "LS", "LR", "MW", "MV", "ML", "MH", "FM", "NA", "NR", "NE", "PW", "PG", "PR", "WS", "SM", "ST", "SN", "SC", "SL", "SB", "KN", "LC", "VC", "SR", "TL", "TO", "TT", "TV", "VU", "AZ", "BN", "BI", "KH", "CM", "TD", "KM", "GQ", "SZ", "GA", "GN", "KG", "LA", "MO", "MR", "MN", "NP", "RW", "TG", "UZ", "ZW", "BJ", "MG", "MU", "MZ", "AO", "CI", "DJ", "ZM", "CD", "CG", "IQ", "LY", "TJ", "VE", "ET", "XK"],
        "disc_number": 1,
        "duration_ms": 376000,
        "explicit": false,
        "external_ids": {
          "isrc": "DEKC41200989"
        },
        "external_urls": {
          "spotify": "https://open.spotify.com/track/4rzfv0JLZfVhOhbSQ8o5jZ"
        },
        "href": "https://api.spotify.com/v1/tracks/4rzfv0JLZfVhOhbSQ8o5jZ",
        "id": "$id",
        "name": "$songName",
        "popularity": 2,
        "preview_url": null,
        "track_number": 10,
        "type": "track",
        "uri": "spotify:track:4rzfv0JLZfVhOhbSQ8o5jZ",
        "is_local": false,
        "episode": false,
        "track": true
      },
      "primary_color": null,
      "video_thumbnail": {
        "url": null
      }
    }
  ]
}
""".trimIndent()

fun spotifyPlaylistItemsWithoutTrack() = """{
  "href": "https://api.spotify.com/v1/playlists/3cEYpjA9oz9GiPac4AsH4n/tracks?offset=0&limit=100&locale=en-GB,en-US;q%3D0.9,en;q%3D0.8",
  "limit": 100,
  "next": null,
  "offset": 0,
  "previous": null,
  "total": 1,
  "items": [
    {
      "added_at": "2015-01-15T12:39:22Z",
      "added_by": {
        "external_urls": {
          "spotify": "https://open.spotify.com/user/jmperezperez"
        },
        "href": "https://api.spotify.com/v1/users/jmperezperez",
        "id": "jmperezperez",
        "type": "user",
        "uri": "spotify:user:jmperezperez"
      },
      "is_local": false,
      "track": null,
      "primary_color": null,
      "video_thumbnail": {
        "url": null
      }
    }
  ]
}
    
""".trimIndent()