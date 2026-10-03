#!/usr/bin/env nu

# Submit a build recipe to fdroiddata via the GitLab API, without cloning it.
#
#   nix develop --command nu tools/fdroid-submit.nu
#
# Reads a recipe from tools/fdroid/<appid>.yml, pushes it to a branch on the
# fork and opens or updates the merge request against fdroid/fdroiddata. Needs
# a GitLab personal access token with the 'api' scope in GL_TOKEN.
#
# The branch is created from the fork's master, which drifts from upstream. If
# upstream has since edited the same recipe the MR conflicts and the fork must
# be synced (GitLab's "Update fork") before running this again.

const FORK = "austinried/fdroiddata"
const UPSTREAM = "fdroid/fdroiddata"
const BASE_BRANCH = "master"

def pct-encode [s: string] { $s | str replace -a "/" "%2F" }

def main [
    recipe: path = "tools/fdroid/com.subtracks.yml"
    --api: string = "https://gitlab.com/api/v4"
    --dry-run
] {
    if not ($recipe | path exists) {
        error make { msg: $"recipe not found: ($recipe)" }
    }

    let appid = ($recipe | path basename | str replace -r '\.ya?ml$' '')
    let branch = $"($appid)-recipe"
    let title = $"($appid): update build recipe"
    let file_path = (pct-encode $"metadata/($appid).yml")
    let fork = (pct-encode $FORK)
    let upstream = (pct-encode $UPSTREAM)

    if $dry_run {
        print $"would push ($recipe) to ($FORK)@($branch) and open an MR against ($UPSTREAM)@($BASE_BRANCH)"
        return
    }

    let token = ($env.GL_TOKEN? | default "")
    if ($token | is-empty) {
        error make { msg: "GL_TOKEN is unset; it needs a GitLab token with the 'api' scope" }
    }
    let headers = { "PRIVATE-TOKEN": $token }
    let upstream_id = (http get $"($api)/projects/($upstream)" --headers $headers | get id)

    let branch_exists = (try { http get $"($api)/projects/($fork)/repository/branches/($branch)" --headers $headers } catch { null }) | is-not-empty
    if not $branch_exists {
        http post $"($api)/projects/($fork)/repository/branches" ({ branch: $branch, ref: $BASE_BRANCH } | to json) --headers $headers --content-type "application/json" | ignore
    }

    http put $"($api)/projects/($fork)/repository/files/($file_path)" ({ branch: $branch, content: (open --raw $recipe), commit_message: $title } | to json) --headers $headers --content-type "application/json" | ignore

    let open = (http get $"($api)/projects/($fork)/merge_requests?state=opened&source_branch=($branch)&target_branch=($BASE_BRANCH)" --headers $headers)
    if ($open | is-not-empty) {
        print $"updated MR !($open.0.iid): ($open.0.web_url)"
        return
    }

    let mr = (http post $"($api)/projects/($fork)/merge_requests" ({ source_branch: $branch, target_branch: $BASE_BRANCH, target_project_id: $upstream_id, title: $title, remove_source_branch: true } | to json) --headers $headers --content-type "application/json")
    print $"opened MR !($mr.iid): ($mr.web_url)"
}
