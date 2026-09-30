#!/usr/bin/env nu

const NAV_PORT = 4533
const GONIC_PORT = 4747
const LMS_PORT = 5082
const NC_PORT = 8090
const PRUNE_PORT = 4534
const LMS_KEY = "subtracks-lms"
const NC_KEY = "subtracks-nextcloud"
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

# Nextcloud's Music app reads repeated (array) parameters straight from the raw query string
# without URL-decoding them, so ids must be sent with their unreserved characters intact.
def url-escape [value: string] {
    $value
        | url encode
        | str replace --all '%2D' '-'
        | str replace --all '%2d' '-'
        | str replace --all '%2E' '.'
        | str replace --all '%2e' '.'
        | str replace --all '%5F' '_'
        | str replace --all '%5f' '_'
        | str replace --all '%7E' '~'
        | str replace --all '%7e' '~'
}

def sub-url [base: string, user: string, pass: string, method: string, params: list<any>] {
    let all = ([["u" $user] ["p" $pass] ["v" "1.16.1"] ["c" "subtracks-test"] ["f" "json"]] | append $params)
    let query = ($all | each {|p| $"($p.0)=(url-escape ($p.1 | into string))" } | str join "&")
    $"($base)rest/($method).view?($query)"
}

def sub-get [base: string, user: string, pass: string, method: string, params: list<any> = []] {
    let response = (http get -m 10sec (sub-url $base $user $pass $method $params) | get "subsonic-response")
    if $response.status == "failed" {
        error make { msg: $"($method) failed: ($response.error.message? | default 'unknown error')" }
    }
    $response
}

def poll [label: string, check: closure, --attempts (-n): int = 120] {
    for attempt in 0..<$attempts {
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

    # Not every server maintains "recent"/"frequent" album lists; when they are missing the
    # scrobble still succeeded, so this is only a best-effort check.
    let observed = (try {
        poll -n 15 "scrobbled album" {||
            let recent = (sub-get $base $user $pass "getAlbumList2" [["type" "recent"]] | get albumList2.album)
            let frequent = (sub-get $base $user $pass "getAlbumList2" [["type" "frequent"]] | get albumList2.album)
            if (($recent | any {|a| $a.id == $kosmo }) or ($frequent | any {|a| $a.id == $kosmo })) { $recent } else { null }
        }
        true
    } catch { false })
    if not $observed { print "  (no recent/frequent album list on this server; skipping scrobble check)" }
}

def start-lms [work: string] {
    let lms_bin = (which lms | get 0.path)
    let lms_home = ($lms_bin | path dirname | path dirname)
    let dir = ($work | path join "lms")
    mkdir ($dir | path join "work")
    let conf = ($dir | path join "lms.conf")
    (open ($lms_home | path join "share/lms/lms.conf"))
        | str replace --all 'working-dir = "/var/lms"' $"working-dir = \"($dir)/work\""
        | str replace --all 'listen-addr = "0.0.0.0"' 'listen-addr = "127.0.0.1"'
        | save -f $conf

    # The first start creates and migrates the database; stop it before seeding, then run for real.
    let bootstrap = (job spawn { ^$lms_bin $conf out+err> ($dir | path join "bootstrap.log") })
    let ready = (try {
        poll -n 60 "lms database" {||
            try { http get -m 2sec $"http://127.0.0.1:($LMS_PORT)/rest/ping.view?u=x&p=x&v=1.16.0&c=subtracks-test" | ignore; true } catch { null }
        }
        true
    } catch { false })
    job kill $bootstrap
    if not $ready { error make { msg: "lms failed to initialise its database" } }

    let db = ($dir | path join "work/lms.db")
    let user_sql = (r#'INSERT INTO user (version,type,login_name,bcrypt_round_count,password_salt,password_hash,subsonic_enable_transcoding_by_default,subsonic_default_transcode_format,subsonic_default_transcode_bitrate,subsonic_artist_list_mode,ui_theme,ui_artist_release_sort_method,ui_enable_inline_artist_relationships,ui_inline_artist_relationships,feedback_backend,scrobbling_backend,listenbrainz_token,lastfm_api_key,lastfm_api_secret,lastfm_session_key) VALUES (1,1,'admin',7,'salt','$2y$07$R6aUyqKU1HM4uX6dtpnh0OIsgqa5JID8qaH4syPlFokL3WoATtYim',0,2,128000,0,1,7,0,0,0,0,'','','','');'#)
    ^sqlite3 $db $user_sql
    let lib_sql = (r#'INSERT INTO media_library (version,path,name) VALUES (0,'MUSIC','Main');'# | str replace 'MUSIC' ((pwd) | path join $LIB))
    ^sqlite3 $db $lib_sql
    let token_sql = (r#'INSERT INTO auth_token (version,domain,value,expiry,use_count,last_used,max_use_count,user_id) VALUES (1,'subsonic','KEY',NULL,0,NULL,NULL,(SELECT id FROM user WHERE login_name='admin'));'# | str replace 'KEY' $LMS_KEY)
    ^sqlite3 $db $token_sql

    let daemon = (job spawn { ^$lms_bin $conf out+err> ($dir | path join "lms.log") })
    let started = (try {
        poll "lms" {||
            let response = (try { sub-get $"http://127.0.0.1:($LMS_PORT)/" "admin" $LMS_KEY "ping" } catch { null })
            if $response == null { null } else { true }
        }
        sub-get $"http://127.0.0.1:($LMS_PORT)/" "admin" $LMS_KEY "startScan" | ignore
        poll "lms scan" {||
            let scanning = (sub-get $"http://127.0.0.1:($LMS_PORT)/" "admin" $LMS_KEY "getScanStatus" | get scanStatus.scanning)
            if (($scanning | into string | str lowercase) == "false") { true } else { null }
        }
        true
    } catch { false })
    if not $started { do -i { job kill $daemon }; error make { msg: "lms did not become ready" } }
    $daemon
}

def start-nextcloud [work: string] {
    let dir = ($work | path join "nextcloud")
    mkdir $dir
    ^cp -r $"($env.NEXTCLOUD_DIR)/." $dir
    ^chmod -R u+w $dir
    mkdir ($dir | path join "custom_apps")
    ^cp -r $env.NEXTCLOUD_MUSIC_APP ($dir | path join "custom_apps/music")
    ^chmod -R u+w ($dir | path join "custom_apps/music")

    let occ = ($dir | path join "occ")
    ^php -d "memory_limit=512M" $occ "maintenance:install" "--database" "sqlite" "--admin-user" "admin" "--admin-pass" "password" "--data-dir" ($dir | path join "data") out+err> ($dir | path join "install.log")
    ^php -d "memory_limit=512M" $occ "app:enable" "music" out+err> ($dir | path join "enable.log")
    mkdir ($dir | path join "data/admin/files/Music")
    ^cp -r $"((pwd) | path join $LIB)/." ($dir | path join "data/admin/files/Music")
    ^php -d "memory_limit=512M" $occ "files:scan" "admin" out+err> ($dir | path join "files-scan.log")
    ^php -d "memory_limit=512M" $occ "music:scan" "admin" out+err> ($dir | path join "music-scan.log")

    let hash = ($NC_KEY | hash sha256)
    let key_sql = (r#'INSERT INTO oc_music_ampache_users (user_id, hash, description) VALUES ('admin','HASH','subtracks');'# | str replace 'HASH' $hash)
    ^sqlite3 ($dir | path join "data/owncloud.db") $key_sql

    let daemon = (job spawn { ^php -d "memory_limit=512M" -S $"127.0.0.1:($NC_PORT)" -t $dir out+err> ($dir | path join "php.log") })
    let started = (try {
        poll "nextcloud" {||
            try { http get -m 3sec $"http://127.0.0.1:($NC_PORT)/status.php" | ignore; true } catch { null }
        }
        true
    } catch { false })
    if not $started { do -i { job kill $daemon }; error make { msg: "nextcloud did not become ready" } }
    $daemon
}

# A second, disposable navidrome over a private copy of two albums. The prune test deletes one
# of them and rescans, so this must not share a library with the servers above.
def start-prune [work: string] {
    let dir = ($work | path join "prune")
    let music = ($dir | path join "music")
    mkdir ($dir | path join "data")
    mkdir $music
    ^cp -r $"((pwd) | path join $LIB)/197" $music
    ^cp -r $"((pwd) | path join $LIB)/199" $music

    let daemon = (job spawn { ^navidrome --address 127.0.0.1 --port $PRUNE_PORT --musicfolder $music --datafolder ($dir | path join "data") --loglevel warn out+err> ($dir | path join "navidrome.log") })
    let ready = (try {
        poll "prune navidrome" {||
            try { http get -m 2sec $"http://127.0.0.1:($PRUNE_PORT)/rest/ping.view?u=x&p=x&v=1.16.1&c=test" | ignore; true } catch { null }
        }
        http post -m 10sec -t application/json $"http://127.0.0.1:($PRUNE_PORT)/auth/createAdmin" { username: "admin", password: "password" } | ignore
        poll "prune library" {||
            let albums = (try {
                http get -m 5sec $"http://127.0.0.1:($PRUNE_PORT)/rest/getAlbumList2.view?u=admin&p=password&v=1.16.1&c=test&f=json&type=newest&size=500"
                    | get subsonic-response.albumList2.album
            } catch { [] })
            if (($albums | length) == 2) { true } else { null }
        }
        true
    } catch { false })
    if not $ready { do -i { job kill $daemon }; error make { msg: "prune navidrome did not become ready" } }
    $daemon
}

def main [] {
    if (($env.NEXTCLOUD_DIR? | default "") | is-empty) {
        error make { msg: "integration servers are not on PATH; enter the integration shell with 'nix develop .#integration'" }
    }

    ensure-music

    for port in [$NAV_PORT $GONIC_PORT $LMS_PORT $NC_PORT $PRUNE_PORT] {
        if (port-in-use $port) { error make { msg: $"port ($port) is already in use; stop the leftover server and retry" } }
    }

    let work = (^mktemp -d | str trim)
    for dir in ["navidrome" "gonic-cache" "gonic-playlists" "gonic-podcasts"] {
        mkdir $"($work)/($dir)"
    }

    print "starting navidrome"
    let nav = (job spawn { ^navidrome --address 127.0.0.1 --port $NAV_PORT --musicfolder $LIB --datafolder $"($work)/navidrome" --loglevel warn out+err> $"($work)/navidrome.log" })
    print "starting gonic"
    let gonic = (job spawn { ^gonic -music-path $LIB -listen-addr $"127.0.0.1:($GONIC_PORT)" -db-path $"($work)/gonic.db" -cache-path $"($work)/gonic-cache" -playlists-path $"($work)/gonic-playlists" -podcast-path $"($work)/gonic-podcasts" -scan-at-start-enabled -http-log=false out+err> $"($work)/gonic.log" })
    mut lms = -1
    mut nc = -1
    mut prune = -1
    let outcome = (try {
        print "starting lms"
        $lms = (start-lms $work)
        print "starting nextcloud"
        $nc = (start-nextcloud $work)
        print "starting prune navidrome"
        $prune = (start-prune $work)

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
        setup-data $"http://127.0.0.1:($LMS_PORT)/" "admin" $LMS_KEY
        setup-data $"http://127.0.0.1:($NC_PORT)/index.php/apps/music/subsonic/" "admin" $NC_KEY

        print "running integration tests"
        let prune_base = $"http://localhost:($PRUNE_PORT)/"
        let prune_music = ($work | path join "prune/music")
        ^gradle :app:integrationTest --rerun --no-configuration-cache --console=plain $"-PpruneBaseUrl=($prune_base)" $"-PpruneMusicDir=($prune_music)"
        $env.LAST_EXIT_CODE
    } catch {|e|
        print $"error: ($e.msg)"
        ($env.LAST_EXIT_CODE? | default 1)
    })

    let lms_id = $lms
    let nc_id = $nc
    let prune_id = $prune
    do -i { job kill $nav }
    do -i { job kill $gonic }
    if $lms_id >= 0 { do -i { job kill $lms_id } }
    if $nc_id >= 0 { do -i { job kill $nc_id } }
    if $prune_id >= 0 { do -i { job kill $prune_id } }
    rm -rf $work

    exit $outcome
}
