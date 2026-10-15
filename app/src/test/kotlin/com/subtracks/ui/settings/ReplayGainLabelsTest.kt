package com.subtracks.ui.settings

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.subtracks.data.prefs.ReplayGainMode
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ReplayGainLabelsTest {
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
}
