package com.tcglab.app.folio

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RulesTest {
    @Test
    fun deckProblems_rejects_more_than_two_copies_of_same_name() {
        val cards = listOf(
            ResolvedDeckCard("A", 1, 2, "Pikachu", 101, false, "", "Lightning"),
            ResolvedDeckCard("B", 2, 1, "Pikachu", 202, false, "", "Lightning"),
        )

        val problems = deckProblems(cards, listOf(1))

        assertTrue(problems.any { it.contains("Pikachu") && it.contains("máximo es 2") })
    }

    @Test
    fun deckProblems_allows_two_copies_split_across_variants_of_same_name() {
        val cards = listOf(
            ResolvedDeckCard("A", 1, 1, "Pikachu", 101, false, "", "Lightning"),
            ResolvedDeckCard("B", 2, 1, "Pikachu", 202, false, "", "Lightning"),
        )

        val problems = deckProblems(cards, listOf(1))

        assertEquals(0, problems.size)
    }

    @Test
    fun copyLimitReached_counts_existing_copies_by_name() {
        val cards = listOf(
            DeckCard("A", 1, 1),
            DeckCard("B", 2, 1),
        )

        assertTrue(copyLimitReached(cards, "A", 1))
        assertTrue(copyLimitReached(cards, "B", 2))
    }
}
