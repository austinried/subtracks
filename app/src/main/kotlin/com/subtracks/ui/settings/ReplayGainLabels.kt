package com.subtracks.ui.settings

import android.content.res.Resources
import com.subtracks.R
import com.subtracks.data.prefs.ReplayGainMode

internal fun replayGainModeLabel(
    resources: Resources,
    mode: ReplayGainMode,
): String =
    when (mode) {
        ReplayGainMode.Off -> resources.getString(R.string.settings_replay_gain_mode_off)
        ReplayGainMode.Track -> resources.getString(R.string.settings_replay_gain_mode_track)
        ReplayGainMode.Album -> resources.getString(R.string.settings_replay_gain_mode_album)
    }

internal fun replayGainPreampLabel(
    resources: Resources,
    decibels: Float,
): String {
    val sign = if (decibels > 0f) "+" else ""
    return resources.getString(R.string.settings_replay_gain_preamp_value, "$sign$decibels")
}
