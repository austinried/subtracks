package com.subtracks.playback

import android.net.Uri
import androidx.media3.common.C
import androidx.media3.datasource.DataSourceException
import androidx.media3.datasource.DataSpec
import androidx.media3.exoplayer.source.LoadEventInfo
import androidx.media3.exoplayer.source.MediaLoadData
import androidx.media3.exoplayer.upstream.DefaultLoadErrorHandlingPolicy
import androidx.media3.exoplayer.upstream.LoadErrorHandlingPolicy
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

@RunWith(AndroidJUnit4::class)
class RetryOutOfRangePolicyTest {
    private val policy = RetryOutOfRangePolicy(delayMs = 123L)

    @Test
    fun anOutOfRangeReadIsRetriedOnce() {
        assertEquals(123L, policy.getRetryDelayMsFor(info(outOfRange(), errorCount = 1)))
    }

    @Test
    fun anOutOfRangeReadIsNotRetriedForever() {
        assertEquals(C.TIME_UNSET.toLong(), policy.getRetryDelayMsFor(info(outOfRange(), errorCount = 2)))
    }

    @Test
    fun aWrappedOutOfRangeIsStillSeen() {
        val wrapped = IOException("load failed", outOfRange())

        assertEquals(123L, policy.getRetryDelayMsFor(info(wrapped, errorCount = 1)))
    }

    @Test
    fun otherErrorsGoToTheDefaultPolicy() {
        val plain = IOException("network")
        val expected = DefaultLoadErrorHandlingPolicy().getRetryDelayMsFor(info(plain, errorCount = 1))

        assertEquals(expected, policy.getRetryDelayMsFor(info(plain, errorCount = 1)))
    }

    private fun outOfRange() = DataSourceException(DataSourceException.POSITION_OUT_OF_RANGE)

    private fun info(
        error: IOException,
        errorCount: Int,
    ) = LoadErrorHandlingPolicy.LoadErrorInfo(
        LoadEventInfo(0, DataSpec(Uri.EMPTY), 0),
        MediaLoadData(C.DATA_TYPE_MEDIA),
        error,
        errorCount,
    )
}
