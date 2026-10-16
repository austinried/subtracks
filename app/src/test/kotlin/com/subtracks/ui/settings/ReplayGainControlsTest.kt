package com.subtracks.ui.settings

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.subtracks.data.prefs.MAX_REPLAY_GAIN_PREAMP_DB
import com.subtracks.data.prefs.MIN_REPLAY_GAIN_PREAMP_DB
import com.subtracks.data.prefs.ReplayGainMode
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ReplayGainControlsTest {
    private val resources = ApplicationProvider.getApplicationContext<Context>().resources

    @Test
    fun modeLabelsNameTheGainEachOneUses() {
        assertEquals("Off", replayGainModeLabel(resources, ReplayGainMode.Off))
        assertEquals("Track gain", replayGainModeLabel(resources, ReplayGainMode.Track))
        assertEquals("Album gain", replayGainModeLabel(resources, ReplayGainMode.Album))
    }

    @Test
    fun preampLabelsCarryTheSignAndTheUnit() {
        assertEquals("+3.0 dB", replayGainPreampLabel(resources, 3f))
        assertEquals("-2.5 dB", replayGainPreampLabel(resources, -2.5f))
        assertEquals("0.0 dB", replayGainPreampLabel(resources, 0f))
    }

    @Test
    fun theSliderHasOneStepPerHalfDecibel() {
        assertEquals(60, ((MAX_REPLAY_GAIN_PREAMP_DB - MIN_REPLAY_GAIN_PREAMP_DB) / REPLAY_GAIN_PREAMP_STEP_DB).toInt())
        assertEquals(59, REPLAY_GAIN_PREAMP_STEPS)
    }

    @Test
    fun thePreampStepsAlongTheGridAndStopsAtTheEnds() {
        assertEquals(-1.5f, stepReplayGainPreampDb(-1f, -1), 0f)
        assertEquals(-0.5f, stepReplayGainPreampDb(-1f, 1), 0f)
        assertEquals(MIN_REPLAY_GAIN_PREAMP_DB, stepReplayGainPreampDb(MIN_REPLAY_GAIN_PREAMP_DB, -1), 0f)
        assertEquals(MAX_REPLAY_GAIN_PREAMP_DB, stepReplayGainPreampDb(MAX_REPLAY_GAIN_PREAMP_DB, 1), 0f)
    }

    @Test
    fun thePreampSnapsBackOntoTheHalfDecibelGrid() {
        assertEquals(0f, snapReplayGainPreampDb(-0f), 0f)
        assertEquals(-1f, snapReplayGainPreampDb(-0.99999905f), 0f)
        assertEquals(-3f, snapReplayGainPreampDb(-3.000001f), 0f)
        assertEquals(14.5f, snapReplayGainPreampDb(14.6f), 0f)
        assertEquals(MAX_REPLAY_GAIN_PREAMP_DB, snapReplayGainPreampDb(14.9f), 0f)
        assertEquals(MIN_REPLAY_GAIN_PREAMP_DB, snapReplayGainPreampDb(-14.9f), 0f)
    }
}
