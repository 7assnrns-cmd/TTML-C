package com.example

import com.example.data.local.SampleData
import com.example.domain.parser.AutoParser
import com.example.domain.parser.LrcExporter
import com.example.domain.parser.TTMLExporter
import com.example.domain.parser.TTMLParser
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleUnitTest {

    @Test
    fun testTtmlExportProducesAppleMusicSpec() {
        val sample = SampleData.getSampleProjects().first { it.id == "sample_pump_it" }
        val ttmlXml = TTMLExporter.export(sample)

        assertTrue("Should contain itunes:timing=\"Word\"", ttmlXml.contains("itunes:timing=\"Word\""))
        assertTrue("Should contain ttm namespace", ttmlXml.contains("xmlns:ttm=\"http://www.w3.org/ns/ttml#metadata\""))
        assertTrue("Should contain itunes namespace", ttmlXml.contains("xmlns:itunes=\"http://music.apple.com/lyric-ttml-internal\""))
        assertTrue("Should define agent v1", ttmlXml.contains("xml:id=\"v1\""))
        assertTrue("Should define agent v2", ttmlXml.contains("xml:id=\"v2\""))
        assertTrue("Should define agent v3", ttmlXml.contains("xml:id=\"v3\""))
        assertTrue("Should contain songPart", ttmlXml.contains("itunes:songPart=\"Intro\""))
        assertTrue("Should contain background vocal role", ttmlXml.contains("ttm:role=\"x-bg\""))
        assertTrue("Should contain spans for words", ttmlXml.contains("<span>Pump</span>") || ttmlXml.contains(">Pump</span>"))
    }

    @Test
    fun testTtmlParserReferenceInput() {
        val referenceXml = """
            <tt xmlns="http://www.w3.org/ns/ttml"
                xmlns:itunes="http://music.apple.com/lyric-ttml-internal"
                xmlns:ttm="http://www.w3.org/ns/ttml#metadata"
                itunes:timing="Word" xml:lang="en">
              <head>
                <metadata>
                  <ttm:agent type="person" xml:id="v1">
                    <ttm:name type="full">The Black Eyed Peas</ttm:name>
                  </ttm:agent>
                  <ttm:agent type="person" xml:id="v2"/>
                  <ttm:agent type="group" xml:id="v3"/>
                </metadata>
              </head>
              <body dur="3:33.067" ttm:agent="v3">
                <div begin="5.090" end="15.892" itunes:songPart="Intro">
                  <p begin="9.890" end="10.461" itunes:key="L2" ttm:agent="v3">
                    <span begin="9.890" end="10.188">Pump</span>
                    <span begin="10.188" end="10.461">it</span>
                  </p>
                  <p begin="15.902" end="17.660" itunes:key="L4" ttm:agent="v3">
                    <span begin="15.902" end="16.075">And</span>
                    <span begin="16.075" end="16.372">pump</span>
                    <span>it</span>
                    <span ttm:role="x-bg">
                      <span begin="16.732" end="17.196">(Loud</span>
                      <span begin="17.196" end="17.660">er)</span>
                    </span>
                  </p>
                </div>
              </body>
            </tt>
        """.trimIndent()

        val project = TTMLParser.parse(referenceXml, "Pump It Reference")
        assertEquals("en", project.language)
        assertEquals("Word", project.timingMode)
        assertTrue(project.agents.size >= 3)
        assertEquals("v1", project.agents[0].id)
        assertEquals("The Black Eyed Peas", project.agents[0].name)
        assertTrue(project.lines.isNotEmpty())

        val secondLine = project.lines[1]
        val bgTokens = secondLine.tokens.filter { it.isBackground }
        assertTrue("Should have background tokens", bgTokens.isNotEmpty())
    }

    @Test
    fun testAutoParserDetectsFormats() {
        val lrcContent = """
            [ti:Test Song]
            [ar:Artist]
            [00:05.50]First line of lyrics
            [00:10.00]Second line of lyrics
        """.trimIndent()

        val parsedLrc = AutoParser.parse(lrcContent)
        assertEquals("Test Song", parsedLrc.title)
        assertEquals("Artist", parsedLrc.artist)
        assertEquals(2, parsedLrc.lines.size)
    }
}
