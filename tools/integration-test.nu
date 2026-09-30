#!/usr/bin/env nu

# Run inside the integration shell:
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

    mut nav = -1
    mut gonic = -1
    mut lms = -1
    mut nc = -1
    mut prune = -1
    let outcome = (try {
        print "starting navidrome"
        $nav = (start-navidrome $work)
        print "starting gonic"
        $gonic = (start-gonic $work)
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

    for id in [$nav $gonic $lms $nc $prune] {
        if $id >= 0 { do -i { job kill $id } }
    }
    rm -rf $work

    exit $outcome
}
