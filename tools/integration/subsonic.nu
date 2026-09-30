# Nextcloud reads repeated (array) params from the raw query string without URL-decoding, so ids
# must keep their unreserved characters.
def url-escape [value: string] {
    $value
        | url encode
        | str replace --all '%2D' '-'
        | str replace --all '%5F' '_'
        | str replace --all '%7E' '~'
}

def sub-url [base: string, user: string, pass: string, method: string, params: list<any>] {
    let all = ([["u" $user] ["p" $pass] ["v" "1.16.1"] ["c" "subtracks-test"] ["f" "json"]] | append $params)
    let query = ($all | each {|p| $"($p.0)=(url-escape ($p.1 | into string))" } | str join "&")
    $"($base)rest/($method).view?($query)"
}

export def sub-get [base: string, user: string, pass: string, method: string, params: list<any> = []] {
    let response = (http get -m 10sec (sub-url $base $user $pass $method $params) | get "subsonic-response")
    if $response.status == "failed" {
        error make { msg: $"($method) failed: ($response.error.message? | default 'unknown error')" }
    }
    $response
}

export def named-id [entities: list<any>, name: string] {
    $entities | where {|e| $e.name == $name } | first | get id
}

export def song-id [base: string, user: string, pass: string, album: string, track: int] {
    sub-get $base $user $pass "getAlbum" [["id" $album]] | get album.song | where {|s| $s.track == $track } | first | get id
}

export def has-tracks [base: string, user: string, pass: string, album: string, tracks: list<int>] {
    let found = sub-get $base $user $pass "getAlbum" [["id" $album]] | get album.song | get track
    $tracks | all {|track| $found | any {|f| $f == $track } }
}
