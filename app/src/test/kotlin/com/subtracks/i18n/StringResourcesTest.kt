package com.subtracks.i18n

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.w3c.dom.Element
import java.io.File
import javax.xml.parsers.DocumentBuilderFactory

class StringResourcesTest {
    private val resDir = sequenceOf(File("src/main/res"), File("app/src/main/res")).first(File::isDirectory)
    private val defaultFile = File(resDir, "values/strings.xml")

    private data class Resource(
        val kind: String,
        val text: String,
        val quantities: Set<String>,
    )

    private val validQuantities = setOf("zero", "one", "two", "few", "many", "other")

    @Test
    fun everyLocaleResourceExistsInDefaultWithMatchingTypeAndPlaceholders() {
        val default = parse(defaultFile)
        assertTrue("default strings.xml should not be empty", default.isNotEmpty())

        default.forEach { (name, resource) ->
            if (resource.kind == "plurals") {
                assertTrue("$name in values/strings.xml is missing the required 'other' quantity", "other" in resource.quantities)
            }
        }

        localeFiles().forEach { file ->
            val locale = parse(file)
            locale.forEach { (name, resource) ->
                val base = default[name]
                assertTrue("$name in ${file.parentFile.name} is missing from values/strings.xml", base != null)
                assertEquals("$name kind differs in ${file.parentFile.name}", base!!.kind, resource.kind)
                if (resource.kind == "plurals") {
                    assertTrue(
                        "$name has an invalid quantity in ${file.parentFile.name}",
                        resource.quantities.all { it in validQuantities },
                    )
                    assertTrue("$name in ${file.parentFile.name} is missing the required 'other' quantity", "other" in resource.quantities)
                }
                assertEquals(
                    "$name placeholders differ in ${file.parentFile.name}",
                    placeholders(base.text),
                    placeholders(resource.text),
                )
            }
        }
    }

    private fun localeFiles(): List<File> =
        resDir
            .listFiles { file -> file.isDirectory && file.name.startsWith("values-") }
            .orEmpty()
            .map { File(it, "strings.xml") }
            .filter(File::isFile)

    private fun parse(file: File): Map<String, Resource> {
        val document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(file)
        val resources = document.documentElement
        val result = mutableMapOf<String, Resource>()
        val nodes = resources.childNodes
        for (index in 0 until nodes.length) {
            val element = nodes.item(index) as? Element ?: continue
            val name = element.getAttribute("name")
            if (name.isEmpty()) continue
            val text =
                when (element.tagName) {
                    "plurals" -> pluralText(element)
                    else -> element.textContent
                }
            val quantities =
                if (element.tagName == "plurals") {
                    val items = element.getElementsByTagName("item")
                    (0 until items.length).mapTo(mutableSetOf()) { (items.item(it) as Element).getAttribute("quantity") }
                } else {
                    emptySet()
                }
            result[name] = Resource(element.tagName, text, quantities)
        }
        return result
    }

    private fun pluralText(element: Element): String {
        val items = element.getElementsByTagName("item")
        return (0 until items.length).joinToString(" ") { items.item(it).textContent }
    }

    private fun placeholders(text: String): Map<Int, Char> =
        Regex("""%(\d+)\$([a-zA-Z])""").findAll(text).associate { it.groupValues[1].toInt() to it.groupValues[2][0] }
}
