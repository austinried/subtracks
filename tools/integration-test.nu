#!/usr/bin/env nu

# Starts the test servers, prepares the fixture data the Kotlin integration tests expect, runs
# them, then tears everything down. Enter the integration shell first:
#
#   nix develop .#integration --command nu tools/integration-test.nu

use ./integration/util.nu [port-in-use]
use ./integration/servers.nu *

def main [] {
    if (($env.NEXTCLOUD_DIR? | default "") | is-empty) {
        error make { msg: "integration servers are not on PATH; enter the integration shell with 'nix develop .#integration'" }
    }

    ensure-music

    for port in $ports {
        if (port-in-use $port) { error make { msg: $"port ($port) is already in use; stop the leftover server and retry" } }
    }

    let work = (^mktemp -d | str trim)

    print "starting navidrome"
    let nav = (start-navidrome $work)
    print "starting gonic"
    let gonic = (start-gonic $work)

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

        print "preparing fixtures"
        for server in (test-servers) { prepare-fixtures $server }

        print "running kotlin integration tests"
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
