package com.subtracks.data.download

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

@RunWith(AndroidJUnit4::class)
class ArtworkStoreTest {
    private val context = ApplicationProvider.getApplicationContext<Context>()
    private val root = File(context.cacheDir, "art-${System.nanoTime()}")
    private val store = ArtworkStore(root)

    @Test
    fun storedBytesComeBackThroughTheFile() {
        store.write(1, "1:art-1:false", byteArrayOf(1, 2, 3))

        assertArrayEquals(byteArrayOf(1, 2, 3), store.file(1, "1:art-1:false").readBytes())
        assertTrue(store.uri(1, "1:art-1:false")!!.startsWith("file:"))
    }

    @Test
    fun anUnstoredKeyHasNoUri() {
        assertNull(store.uri(1, "1:missing:false"))
    }

    @Test
    fun aServerSuppliedCoverArtIdCannotEscapeTheArtDirectory() {
        val hostile = "1:x/../../../../escape:false"

        store.write(1, hostile, byteArrayOf(1))

        assertTrue(store.file(1, hostile).canonicalPath.startsWith(root.canonicalPath + File.separator))
    }

    @Test
    fun sweepKeepsWhatIsStillNeededAndDropsTheRest() {
        store.write(1, "1:keep:false", byteArrayOf(1))
        store.write(1, "1:drop:false", byteArrayOf(1))
        store.write(2, "2:other:false", byteArrayOf(1))

        store.sweep(1, setOf("1:keep:false"))

        assertTrue(store.file(1, "1:keep:false").exists())
        assertFalse(store.file(1, "1:drop:false").exists())
        assertTrue(store.file(2, "2:other:false").exists())
    }

    @Test
    fun anUnwritableArtDirectoryIsReportedRatherThanThrown() {
        File(root, "1/art").apply {
            parentFile?.mkdirs()
            writeText("not a directory")
        }

        assertFalse(store.write(1, "1:art-1:false", byteArrayOf(1)))
        assertFalse(store.file(1, "1:art-1:false").exists())
    }
}
