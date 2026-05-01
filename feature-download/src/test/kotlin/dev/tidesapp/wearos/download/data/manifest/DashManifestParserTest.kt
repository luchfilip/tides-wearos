package dev.tidesapp.wearos.download.data.manifest

import java.util.Base64
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DashManifestParserTest {

    private val parser = DashManifestParser()

    // Real MPD from Charles capture (URLs sanitized but structure authentic)
    private val realMpd = """
        <?xml version='1.0' encoding='UTF-8'?>
        <MPD xmlns="urn:mpeg:dash:schema:mpd:2011"
             xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
             xmlns:xlink="http://www.w3.org/1999/xlink"
             xmlns:cenc="urn:mpeg:cenc:2013"
             xsi:schemaLocation="urn:mpeg:dash:schema:mpd:2011 DASH-MPD.xsd"
             profiles="urn:mpeg:dash:profile:isoff-main:2011"
             type="static"
             minBufferTime="PT3.993S"
             mediaPresentationDuration="PT4M56.978S">
          <Period id="0">
            <AdaptationSet id="0"
                           contentType="audio"
                           mimeType="audio/mp4"
                           lang="und"
                           group="main"
                           segmentAlignment="true">
              <audioChannelConfiguration
                  schemeIdUri="urn:mpeg:dash:23003:3:audio_channel_configuration:2011"
                  value="2"/>
              <Role schemeIdUri="urn:mpeg:dash:role:2011" value="main"/>
              <Representation id="FLAC,44100,16"
                              codecs="flac"
                              bandwidth="1001821"
                              audioSamplingRate="44100">
                <SegmentTemplate
                    timescale="44100"
                    initialization="https://sp-ad-cf.audio.tidal.com/mediatracks/abc123/0.mp4?token=xyz"
                    media="https://sp-ad-cf.audio.tidal.com/mediatracks/abc123/${'$'}Number${'$'}.mp4?token=xyz"
                    startNumber="1">
                  <SegmentTimeline>
                    <S d="176128" r="73"/>
                    <S d="63292"/>
                  </SegmentTimeline>
                </SegmentTemplate>
              </Representation>
            </AdaptationSet>
          </Period>
        </MPD>
    """.trimIndent()

    // --- Real manifest tests ---

    @Test
    fun `parse real manifest returns correct segment count`() {
        val result = parser.parseXml(realMpd)
        // r=73 means 74 segments + 1 final segment = 75
        assertEquals(75, result.mediaUrls.size)
    }

    @Test
    fun `parse real manifest returns correct init URL`() {
        val result = parser.parseXml(realMpd)
        assertEquals(
            "https://sp-ad-cf.audio.tidal.com/mediatracks/abc123/0.mp4?token=xyz",
            result.initUrl,
        )
    }

    @Test
    fun `parse real manifest returns correct media URLs`() {
        val result = parser.parseXml(realMpd)
        // First URL should have Number=1
        assertEquals(
            "https://sp-ad-cf.audio.tidal.com/mediatracks/abc123/1.mp4?token=xyz",
            result.mediaUrls.first(),
        )
        // Last URL should have Number=75
        assertEquals(
            "https://sp-ad-cf.audio.tidal.com/mediatracks/abc123/75.mp4?token=xyz",
            result.mediaUrls.last(),
        )
        // Spot check middle
        assertEquals(
            "https://sp-ad-cf.audio.tidal.com/mediatracks/abc123/40.mp4?token=xyz",
            result.mediaUrls[39],
        )
    }

    @Test
    fun `parse real manifest returns correct codec`() {
        val result = parser.parseXml(realMpd)
        assertEquals("flac", result.codec)
    }

    @Test
    fun `parse real manifest returns correct bandwidth`() {
        val result = parser.parseXml(realMpd)
        assertEquals(1001821L, result.bandwidth)
    }

    @Test
    fun `parse real manifest returns correct sample rate`() {
        val result = parser.parseXml(realMpd)
        assertEquals(44100, result.sampleRate)
    }

    @Test
    fun `parse real manifest returns correct duration`() {
        val result = parser.parseXml(realMpd)
        assertEquals(296.978, result.durationSeconds, 0.001)
    }

    // --- parseDuration tests ---

    @Test
    fun `parseDuration handles minutes and seconds`() {
        assertEquals(296.978, parser.parseDuration("PT4M56.978S"), 0.001)
    }

    @Test
    fun `parseDuration handles hours`() {
        assertEquals(3723.0, parser.parseDuration("PT1H2M3S"), 0.001)
    }

    @Test
    fun `parseDuration handles seconds only`() {
        assertEquals(30.0, parser.parseDuration("PT30S"), 0.001)
    }

    @Test
    fun `parseDuration handles minutes only`() {
        assertEquals(300.0, parser.parseDuration("PT5M"), 0.001)
    }

    // --- Edge case manifests ---

    @Test
    fun `parse manifest with single segment`() {
        val xml = buildMpd(
            segmentTimeline = """<S d="44100"/>""",
        )
        val result = parser.parseXml(xml)
        assertEquals(1, result.mediaUrls.size)
        assertEquals(
            "https://example.com/media/1.mp4",
            result.mediaUrls.first(),
        )
    }

    @Test
    fun `parse manifest with multiple S entries`() {
        val xml = buildMpd(
            segmentTimeline = """<S d="176128" r="4"/><S d="88064" r="1"/><S d="44032"/>""",
        )
        val result = parser.parseXml(xml)
        // 5 + 2 + 1 = 8
        assertEquals(8, result.mediaUrls.size)
        // Numbers should be sequential 1..8
        for (i in 1..8) {
            assertEquals(
                "https://example.com/media/$i.mp4",
                result.mediaUrls[i - 1],
            )
        }
    }

    // --- Base64 round-trip ---

    @Test
    fun `parse base64 encoded manifest`() {
        val encoded = Base64.getEncoder().encodeToString(realMpd.toByteArray())
        val fromBase64 = parser.parse(encoded)
        val fromXml = parser.parseXml(realMpd)
        assertEquals(fromXml, fromBase64)
    }

    // --- Error handling ---

    @Test(expected = Exception::class)
    fun `parse malformed XML throws`() {
        parser.parseXml("this is not xml at all <><><<<")
    }

    // --- Helper to build minimal MPD XML ---

    private fun buildMpd(
        duration: String = "PT30S",
        codec: String = "flac",
        bandwidth: Long = 1001821,
        sampleRate: Int = 44100,
        initUrl: String = "https://example.com/media/0.mp4",
        mediaTemplate: String = "https://example.com/media/\$Number\$.mp4",
        segmentTimeline: String = """<S d="44100"/>""",
    ): String = """
        <?xml version='1.0' encoding='UTF-8'?>
        <MPD xmlns="urn:mpeg:dash:schema:mpd:2011"
             profiles="urn:mpeg:dash:profile:isoff-main:2011"
             type="static"
             mediaPresentationDuration="$duration">
          <Period id="0">
            <AdaptationSet id="0" contentType="audio" mimeType="audio/mp4">
              <Representation id="test"
                              codecs="$codec"
                              bandwidth="$bandwidth"
                              audioSamplingRate="$sampleRate">
                <SegmentTemplate
                    timescale="$sampleRate"
                    initialization="$initUrl"
                    media="$mediaTemplate"
                    startNumber="1">
                  <SegmentTimeline>
                    $segmentTimeline
                  </SegmentTimeline>
                </SegmentTemplate>
              </Representation>
            </AdaptationSet>
          </Period>
        </MPD>
    """.trimIndent()
}
