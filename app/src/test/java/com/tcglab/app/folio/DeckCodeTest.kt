package com.tcglab.app.folio

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Must match the TypeScript encodeDeckCode in src/lib/folio/deck-code.ts.
 * Fixture: 2× trainer entity 100, 2× pokemon entity 10, energies [2, 3]
 * trainers sorted, each id + 10_000_000 as u24; pokemon as u24; then energy count + ids.
 */
class DeckCodeTest {
    @Test
    fun encodeDeckCode_matchesOriginalFormat() {
        val cards = listOf(
            EncodedCard(entityId = 100, trainer = true, count = 2),
            EncodedCard(entityId = 10, trainer = false, count = 2),
        )
        val encoded = encodeDeckCode(cards, listOf(2, 3))

        // Manual expected bytes:
        // trainers: 2, then 100000100 twice as u24, pokemon: 2, then 10 twice as u24, energies: 2, 2, 3
        // 10_000_100 = 0x9896E4
        val expectedBytes = byteArrayOf(
            2,
            0x98.toByte(), 0x96.toByte(), 0xE4.toByte(),
            0x98.toByte(), 0x96.toByte(), 0xE4.toByte(),
            2,
            0x00, 0x00, 0x0A,
            0x00, 0x00, 0x0A,
            2,
            2,
            3,
        )
        val expected = java.util.Base64.getEncoder().encodeToString(expectedBytes)
        assertEquals(expected, encoded)
    }

    @Test
    fun encodeDeckCode_sortsByEntityId() {
        val cards = listOf(
            EncodedCard(entityId = 30, trainer = false, count = 1),
            EncodedCard(entityId = 10, trainer = false, count = 1),
        )
        val encoded = encodeDeckCode(cards, listOf(1))
        val expectedBytes = byteArrayOf(
            0,
            2,
            0x00, 0x00, 0x0A,
            0x00, 0x00, 0x1E,
            1,
            1,
        )
        assertEquals(java.util.Base64.getEncoder().encodeToString(expectedBytes), encoded)
    }
}
