# Shared helpers for the integration harness: waiting and port checks.

export def poll [label: string, check: closure, --attempts (-n): int = 120] {
    for attempt in 0..<$attempts {
        let value = (try { do $check } catch { null })
        if $value != null { return $value }
        if $attempt > 0 and ($attempt mod 5) == 0 { print $"waiting for ($label)..." }
        sleep 1sec
    }
    error make { msg: $"timed out waiting for ($label)" }
}

export def port-in-use [port: int] {
    (^curl -s -o /dev/null --max-time 1 $"http://127.0.0.1:($port)/" | complete).exit_code != 7
}
