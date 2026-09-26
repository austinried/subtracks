package com.subtracks.playback

import android.net.Uri
import androidx.media3.common.C
import androidx.media3.datasource.DataSource
import androidx.media3.datasource.DataSpec
import androidx.media3.datasource.TransferListener
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.subtracks.data.source.streamLengthSuffix
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class KnownLengthDataSourceTest {
    private class StubDataSource(
        private val length: Long,
    ) : DataSource {
        override fun open(dataSpec: DataSpec): Long = length

        override fun read(
            buffer: ByteArray,
            offset: Int,
            length: Int,
        ): Int = C.RESULT_END_OF_INPUT

        override fun getUri(): Uri? = null

        override fun close() = Unit

        override fun addTransferListener(transferListener: TransferListener) = Unit
    }

    private fun transcodedUrl(length: Long) = Uri.parse("http://host/stream" + streamLengthSuffix(length))

    @Test
    fun declaresTheFragmentLengthWhenTheServerDoesNot() {
        val dataSource = KnownLengthDataSource(StubDataSource(C.LENGTH_UNSET.toLong()))

        assertEquals(1_000_000L, dataSource.open(DataSpec(transcodedUrl(1_000_000))))
    }

    @Test
    fun subtractsThePositionFromTheDeclaredLength() {
        val dataSource = KnownLengthDataSource(StubDataSource(C.LENGTH_UNSET.toLong()))

        assertEquals(600_000L, dataSource.open(DataSpec(transcodedUrl(1_000_000), 400_000L, C.LENGTH_UNSET.toLong())))
    }

    @Test
    fun keepsTheServerLengthWhenItHasOne() {
        val dataSource = KnownLengthDataSource(StubDataSource(500_000))

        assertEquals(500_000L, dataSource.open(DataSpec(transcodedUrl(1_000_000))))
    }

    @Test
    fun passesUnknownLengthThroughWithoutAFragment() {
        val dataSource = KnownLengthDataSource(StubDataSource(C.LENGTH_UNSET.toLong()))

        assertEquals(C.LENGTH_UNSET.toLong(), dataSource.open(DataSpec(Uri.parse("http://host/stream"))))
    }
}
