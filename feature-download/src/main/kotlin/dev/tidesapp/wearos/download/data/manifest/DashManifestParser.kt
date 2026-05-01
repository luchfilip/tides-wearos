package dev.tidesapp.wearos.download.data.manifest

import java.io.ByteArrayInputStream
import java.util.Base64
import javax.inject.Inject
import javax.xml.parsers.DocumentBuilderFactory
import org.w3c.dom.Element
import org.w3c.dom.NodeList

/**
 * Pure-Kotlin DASH MPD manifest parser.
 *
 * Extracts segment download URLs from Tidal's DASH MPD XML so the
 * download worker can fetch every segment and concatenate them into
 * a single playable file.
 *
 * No Android framework dependencies -- runs in plain JVM unit tests.
 */
class DashManifestParser @Inject constructor() {

    /**
     * Decode a base64-encoded MPD manifest and parse it.
     */
    fun parse(base64Manifest: String): SegmentInfo {
        val xml = String(Base64.getDecoder().decode(base64Manifest))
        return parseXml(xml)
    }

    /**
     * Parse raw MPD XML into a [SegmentInfo].
     */
    fun parseXml(xml: String): SegmentInfo {
        val factory = DocumentBuilderFactory.newInstance().apply {
            isNamespaceAware = true
        }
        val doc = factory.newDocumentBuilder()
            .parse(ByteArrayInputStream(xml.toByteArray()))

        val mpd = doc.documentElement
        val duration = parseDuration(
            mpd.getAttribute("mediaPresentationDuration"),
        )

        val representation = firstElement(mpd.getElementsByTagNameNS("*", "Representation"))
        val codec = representation.getAttribute("codecs")
        val bandwidth = representation.getAttribute("bandwidth").toLong()
        val sampleRate = representation.getAttribute("audioSamplingRate").toInt()

        val segmentTemplate = firstElement(
            representation.getElementsByTagNameNS("*", "SegmentTemplate"),
        )
        val initUrl = segmentTemplate.getAttribute("initialization")
        val mediaTemplate = segmentTemplate.getAttribute("media")
        val startNumber = segmentTemplate.getAttribute("startNumber")
            .ifEmpty { "1" }
            .toInt()

        val segmentTimeline = firstElement(
            segmentTemplate.getElementsByTagNameNS("*", "SegmentTimeline"),
        )
        val sElements = segmentTimeline.getElementsByTagNameNS("*", "S")

        // Count total segments from <S d="..." r="..."/> entries.
        // Each S element produces (r + 1) segments; r defaults to 0.
        var totalSegments = 0
        for (i in 0 until sElements.length) {
            val s = sElements.item(i) as Element
            val r = s.getAttribute("r").ifEmpty { "0" }.toInt()
            totalSegments += r + 1
        }

        // Build media URLs by substituting $Number$ with sequential numbers.
        val mediaUrls = (startNumber until startNumber + totalSegments).map { number ->
            mediaTemplate.replace("\$Number\$", number.toString())
        }

        return SegmentInfo(
            initUrl = initUrl,
            mediaUrls = mediaUrls,
            codec = codec,
            bandwidth = bandwidth,
            sampleRate = sampleRate,
            durationSeconds = duration,
        )
    }

    /**
     * Parse an ISO 8601 duration string like `PT4M56.978S` to seconds.
     *
     * Supports hours (H), minutes (M), and fractional seconds (S).
     */
    internal fun parseDuration(duration: String): Double {
        val regex = Regex("""PT(?:(\d+)H)?(?:(\d+)M)?(?:([\d.]+)S)?""")
        val match = regex.matchEntire(duration)
            ?: throw IllegalArgumentException("Invalid ISO 8601 duration: $duration")

        val hours = match.groupValues[1].toDoubleOrNull() ?: 0.0
        val minutes = match.groupValues[2].toDoubleOrNull() ?: 0.0
        val seconds = match.groupValues[3].toDoubleOrNull() ?: 0.0

        return hours * 3600.0 + minutes * 60.0 + seconds
    }

    private fun firstElement(nodes: NodeList): Element {
        require(nodes.length > 0) { "Expected at least one element, found none" }
        return nodes.item(0) as Element
    }
}
