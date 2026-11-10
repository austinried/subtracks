package com.subtracks.data.net

import okhttp3.HttpUrl.Companion.toHttpUrlOrNull

internal fun normalizeServerAddress(address: String): String {
    val trimmed = address.trim()
    val withScheme =
        if (trimmed.startsWith("http://") || trimmed.startsWith("https://")) {
            trimmed
        } else {
            "http://$trimmed"
        }
    return withScheme.trimEnd('/') + "/"
}

internal fun serverHost(address: String): String? = normalizeServerAddress(address).toHttpUrlOrNull()?.host
