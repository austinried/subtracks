#!/usr/bin/env nu

# Submit a build recipe to fdroiddata via the GitLab API, without cloning it.
#
#   nix develop --command nu tools/fdroid-submit.nu
#
# Reads a recipe from tools/fdroid/<appid>.yml, pushes it to a branch on the
# fork and opens a merge request against fdroid/fdroiddata. Needs a GitLab
# personal access token with the 'api' scope in GITLAB_TOKEN.

const FORK = "austinried/fdroiddata"
const UPSTREAM = "fdroid/fdroiddata"
const BASE_BRANCH = "master"

def pct-encode [s: string] { $s | str replace -a "/" "%2F" }

def main [
    recipe: path = "tools/fdroid/com.subtracks.yml"
    --api: string = "https://gitlab.com/api/v4"
    --branch: string = ""
    --title: string = ""
    --dry-run
] {
    if not ($recipe | path exists) {
        error make { msg: $"recipe not found: ($recipe)" }
    }

    let appid = ($recipe | path basename | str replace -r '\.ya?ml$' '')
    let branch = (if ($branch | is-empty) { $"($appid)-recipe" } else { $branch })
    let title = (if ($title | is-empty) { $"($appid): update build recipe" } else { $title })
    let file_path = (pct-encode $"metadata/($appid).yml")
    let fork = (pct-encode $FORK)
    let upstream = (pct-encode $UPSTREAM)

    if $dry_run {
        print $"would push ($recipe) to ($FORK)@($branch) and open an MR against ($UPSTREAM)@($BASE_BRANCH)"
        return
    }

    let token = ($env.GITLAB_TOKEN? | default "")
    if ($token | is-empty) {
        error make { msg: "GITLAB_TOKEN is unset; it needs a GitLab token with the 'api' scope" }
    }
    let headers = { "PRIVATE-TOKEN": $token }
    let fork_id = (http get $"($api)/projects/($fork)" --headers $headers | get id)

    try {
        http delete $"($api)/projects/($fork)/repository/branches/($branch)" --headers $headers | ignore
    } catch { }

    http post $"($api)/projects/($fork)/repository/branches" ({ branch: $branch, ref: $BASE_BRANCH } | to json) --headers $headers --content-type "application/json" | ignore

    http post $"($api)/projects/($fork)/repository/files/($file_path)" ({ branch: $branch, content: (open --raw $recipe), commit_message: $title } | to json) --headers $headers --content-type "application/json" | ignore

    let mr = (http post $"($api)/projects/($upstream)/merge_requests" ({ source_project_id: $fork_id, source_branch: $branch, target_branch: $BASE_BRANCH, title: $title, remove_source_branch: true } | to json) --headers $headers --content-type "application/json")
    print $"opened MR !($mr.iid): ($mr.web_url)"
}
