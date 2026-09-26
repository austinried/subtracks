package com.subtracks.ui.settings

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AddSourceValidationTest {
    @Test
    fun flagsOnlyTheMissingFields() {
        val missingName = AddSourceState(name = "", address = "https://music.example.com").validated()
        assertTrue("a blank name should be flagged", missingName.nameError)
        assertFalse("a present address should not be flagged", missingName.addressError)

        val missingAddress = AddSourceState(name = "Home", address = "").validated()
        assertFalse("a present name should not be flagged", missingAddress.nameError)
        assertTrue("a blank address should be flagged", missingAddress.addressError)

        val complete = AddSourceState(name = "Home", address = "https://music.example.com").validated()
        assertFalse(complete.nameError)
        assertFalse(complete.addressError)
    }
}
