package com.subtracks.playback

import android.net.Uri
import androidx.media3.common.C
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DataSource
import androidx.media3.datasource.DataSpec
import androidx.media3.datasource.TransferListener
import com.subtracks.data.source.declaredLengthFromFragment

@UnstableApi
internal class KnownLengthDataSource(
    private val delegate: DataSource,
) : DataSource {
    override fun open(dataSpec: DataSpec): Long {
        val length = delegate.open(dataSpec)
        if (length != C.LENGTH_UNSET.toLong()) return length
        val declared = declaredLengthFromFragment(dataSpec.uri.fragment) ?: return length
        return (declared - dataSpec.position).coerceAtLeast(0)
    }

    override fun read(
        buffer: ByteArray,
        offset: Int,
        length: Int,
    ): Int = delegate.read(buffer, offset, length)

    override fun getUri(): Uri? = delegate.uri

    override fun getResponseHeaders(): Map<String, List<String>> = delegate.responseHeaders

    override fun close() = delegate.close()

    override fun addTransferListener(transferListener: TransferListener) = delegate.addTransferListener(transferListener)
}

@UnstableApi
internal class KnownLengthDataSourceFactory(
    private val upstream: DataSource.Factory,
) : DataSource.Factory {
    override fun createDataSource(): DataSource = KnownLengthDataSource(upstream.createDataSource())
}
