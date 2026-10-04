package com.subtracks.ui.components

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withTimeoutOrNull
import org.junit.Assert.assertEquals
import org.junit.Test

class ContextMenuHostTest {
    @Test
    fun aDismissalBeforeCollectionIsStillDelivered() =
        runTest {
            val host = ContextMenuHost()

            host.dismissTransients()

            assertEquals(Unit, withTimeoutOrNull(1_000) { host.dismissals.first() })
        }
}
