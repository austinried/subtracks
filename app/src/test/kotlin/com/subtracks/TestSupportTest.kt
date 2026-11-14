package com.subtracks

import org.junit.Test

class TestSupportTest {
    @Test
    fun returnsWhenThePredicateIsBrieflyTrue() {
        var calls = 0
        awaitUntil("the predicate is briefly true") { ++calls == 1 }
    }
}
