#!/usr/bin/env nu

const NAV_PORT = 4533
const GONIC_PORT = 4747
const LIB = ".integration/music"
const MUSIC = [
    { id: 197, name: "Ugress - Retroconnaissance EP", songs: 4 }
    { id: 199, name: "Ugress - Kosmonaut", songs: 4 }
    { id: 321, name: "Brad Sucks - I Don't Know What I'm Doing", songs: 12 }
]

def song-count [dir: string] {
    if not ($dir | path exists) { return 0 }
    ls $dir | where type == file | where {|f| $f.name =~ '(?i)\.mp3$' } | length
}

def ensure-music [] {
    if ($MUSIC | all {|m| (song-count $"($LIB)/($m.id)") == $m.songs }) {
        print $"using test music in ($LIB)"
        return
    }
    print "downloading test music from demo.subsonic.org (one-time)"
    mkdir $LIB
    let tmp = (^mktemp -d | str trim)
    for m in $MUSIC {
        print $"($m.name)..."
        ^curl -fsS -o $"($tmp)/($m.id).zip" $"http://demo.subsonic.org/rest/download.view?u=guest1&p=guest&v=1.13.0&c=subtracks-test&id=($m.id)"
        ^unzip -q -o $"($tmp)/($m.id).zip" -d $"($LIB)/($m.id)"
    }
    rm -rf $tmp
}

def sub-url [base: string, user: string, pass: string, method: string, params: list<any>] {
    let all = ([["u" $user] ["p" $pass] ["v" "1.13.0"] ["c" "subtracks-test"] ["f" "json"]] | append $params)
    let query = ($all | each {|p| $"($p.0)=($p.1 | url encode)" } | str join "&")
    $"($base)rest/($method).view?($query)"
}

def sub-get [base: string, user: string, pass: string, method: string, params: list<any> = []] {
    let response = (http get -m 10sec (sub-url $base $user $pass $method $params) | get "subsonic-response")
    if $response.status == "failed" {
        error make { msg: $"($method) failed: ($response.error.message? | default 'unknown error')" }
    }
    $response
}

def poll [label: string, check: closure] {
    for attempt in 0..<120 {
        let value = (try { do $check } catch { null })
        if $value != null { return $value }
        if $attempt > 0 and ($attempt mod 5) == 0 { print $"waiting for ($label)..." }
        sleep 1sec
    }
    error make { msg: $"timed out waiting for ($label)" }
}

def port-in-use [port: int] {
    (^curl -s -o /dev/null --max-time 1 $"http://127.0.0.1:($port)/" | complete).exit_code != 7
}

def album-id [albums: list<any>, name: string] {
    $albums | where {|a| $a.name == $name } | first | get id
}

def song-id [base: string, user: string, pass: string, album: string, track: int] {
    sub-get $base $user $pass "getAlbum" [["id" $album]] | get album.song | where {|s| $s.track == $track } | first | get id
}

def has-tracks [base: string, user: string, pass: string, album: string, tracks: list<int>] {
    let found = sub-get $base $user $pass "getAlbum" [["id" $album]] | get album.song | get track
    $tracks | all {|track| $found | any {|f| $f == $track } }
}

def setup-data [base: string, user: string, pass: string] {
    let artists = (poll "artists" {||
        let items = (sub-get $base $user $pass "getArtists" | get artists.index | each {|i| $i.artist } | flatten)
        if (["Ugress" "Brad Sucks"] | all {|name| $items | any {|i| $i.name == $name } }) { $items } else { null }
    })

    let albums = (poll "albums" {||
        let items = (sub-get $base $user $pass "getAlbumList2" [["type" "newest"]] | get albumList2.album)
        if (["Kosmonaut" "Retroconnaissance EP" "I Don't Know What I'm Doing"] | all {|name| $items | any {|i| $i.name == $name } }) { $items } else { null }
    })

    let ugress = (album-id $artists "Ugress")
    let kosmo = (album-id $albums "Kosmonaut")
    let retro = (album-id $albums "Retroconnaissance EP")
    let dunno = (album-id $albums "I Don't Know What I'm Doing")

    poll "album songs" {||
        if (has-tracks $base $user $pass $retro [1 2]) and (has-tracks $base $user $pass $kosmo [2 4]) and (has-tracks $base $user $pass $dunno [9 10 11]) {
            $albums
        } else {
            null
        }
    } | ignore

    sub-get $base $user $pass "star" [["albumId" $kosmo]] | ignore
    sub-get $base $user $pass "star" [["artistId" $ugress]] | ignore

    let song_ids = [
        (song-id $base $user $pass $retro 2)
        (song-id $base $user $pass $retro 1)
        (song-id $base $user $pass $kosmo 2)
        (song-id $base $user $pass $kosmo 4)
        (song-id $base $user $pass $dunno 9)
        (song-id $base $user $pass $dunno 10)
        (song-id $base $user $pass $dunno 11)
    ]
    let playlist = ([["name" "Playlist 1"]] | append ($song_ids | each {|id| ["songId" $id] }))
    sub-get $base $user $pass "createPlaylist" $playlist | ignore

    sub-get $base $user $pass "scrobble" [["id" (song-id $base $user $pass $retro 1)] ["submission" "true"]] | ignore
    sleep 1sec
    sub-get $base $user $pass "scrobble" [["id" (song-id $base $user $pass $retro 2)] ["submission" "true"]] | ignore
    sleep 1sec
    sub-get $base $user $pass "scrobble" [["id" (song-id $base $user $pass $kosmo 1)] ["submission" "true"]] | ignore

    poll "scrobbled album" {||
        let recent = (sub-get $base $user $pass "getAlbumList2" [["type" "recent"]] | get albumList2.album)
        let frequent = (sub-get $base $user $pass "getAlbumList2" [["type" "frequent"]] | get albumList2.album)
        if (($recent | any {|a| $a.id == $kosmo }) or ($frequent | any {|a| $a.id == $kosmo })) { $recent } else { null }
    } | ignore
}

def main [] {
    ensure-music

    if (port-in-use $NAV_PORT) { error make { msg: $"port ($NAV_PORT) is already in use (leftover navidrome?); stop it and retry" } }
    if (port-in-use $GONIC_PORT) { error make { msg: $"port ($GONIC_PORT) is already in use (leftover gonic?); stop it and retry" } }

    let work = (^mktemp -d | str trim)
    for dir in ["navidrome" "gonic-cache" "gonic-playlists" "gonic-podcasts"] {
        mkdir $"($work)/($dir)"
    }

    print "starting navidrome"
    let nav = (job spawn { ^navidrome --address 127.0.0.1 --port $NAV_PORT --musicfolder $LIB --datafolder $"($work)/navidrome" --loglevel warn })
    print "starting gonic"
    let gonic = (job spawn { ^gonic -music-path $LIB -listen-addr $"127.0.0.1:($GONIC_PORT)" -db-path $"($work)/gonic.db" -cache-path $"($work)/gonic-cache" -playlists-path $"($work)/gonic-playlists" -podcast-path $"($work)/gonic-podcasts" -scan-at-start-enabled -http-log=false })

    let outcome = (try {
        poll "navidrome" {||
            http get -m 5sec $"http://127.0.0.1:($NAV_PORT)/rest/ping.view?u=x&p=x&v=1.13.0&c=test" | ignore
            true
        }
        poll "gonic" {||
            http get -m 5sec $"http://127.0.0.1:($GONIC_PORT)/rest/ping.view?u=x&p=x&v=1.13.0&c=test" | ignore
            true
        }

        print "creating navidrome admin"
        http post -m 10sec -t application/json $"http://127.0.0.1:($NAV_PORT)/auth/createAdmin" { username: "admin", password: "password" } | ignore

        print "setting up test data"
        setup-data $"http://127.0.0.1:($NAV_PORT)/" "admin" "password"
        setup-data $"http://127.0.0.1:($GONIC_PORT)/" "admin" "admin"

        print "running integration tests"
        ^gradle :app:integrationTest --rerun --no-configuration-cache --console=plain
        $env.LAST_EXIT_CODE
    } catch {|e|
        print $"error: ($e.msg)"
        ($env.LAST_EXIT_CODE? | default 1)
    })

    do -i { job kill $nav }
    do -i { job kill $gonic }
    rm -rf $work

    exit $outcome
}
