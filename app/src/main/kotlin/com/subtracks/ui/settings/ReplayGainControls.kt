package com.subtracks.ui.settings

import android.content.res.Resources
import com.subtracks.R
import com.subtracks.data.prefs.MAX_REPLAY_GAIN_PREAMP_DB
import com.subtracks.data.prefs.MIN_REPLAY_GAIN_PREAMP_DB
import com.subtracks.data.prefs.ReplayGainMode
import kotlin.math.roundToInt

internal const val REPLAY_GAIN_PREAMP_STEP_DB = 0.5f

internal val REPLAY_GAIN_PREAMP_STEPS =
    ((MAX_REPLAY_GAIN_PREAMP_DB - MIN_REPLAY_GAIN_PREAMP_DB) / REPLAY_GAIN_PREAMP_STEP_DB).toInt() - 1

// Compose Material 3's Slider snaps in pixel space and converts back, so the value it reports is
// near, but not on, the grid it was configured with (for example -0.99999905 for -1.0). Snap before
// showing or storing it.
internal fun snapReplayGainPreampDb(value: Float): Float = (value / REPLAY_GAIN_PREAMP_STEP_DB).roundToInt() * REPLAY_GAIN_PREAMP_STEP_DB

internal fun stepReplayGainPreampDb(
    value: Float,
    steps: Int,
): Float =
    snapReplayGainPreampDb(value + steps * REPLAY_GAIN_PREAMP_STEP_DB)
        .coerceIn(MIN_REPLAY_GAIN_PREAMP_DB, MAX_REPLAY_GAIN_PREAMP_DB)

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
    val magnitude = String.format(resources.configuration.locales[0], "%.1f", decibels)
    return resources.getString(R.string.settings_replay_gain_preamp_value, "$sign$magnitude")
}
