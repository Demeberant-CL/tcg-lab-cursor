package com.tcglab.app.folio

import com.tcglab.app.data.canonicalized
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

        assertTrue(problems.none { it.contains("máximo es 2") })
    }

    @Test
    fun deckProblems_rejects_invalid_or_repeated_energies() {
        val cards = listOf(
            ResolvedDeckCard("A", 1, 20, "Pikachu", 101, false, "", "Lightning"),
        )

        val invalid = deckProblems(cards, listOf(1, 9))
        assertTrue(invalid.any { it.contains("energías inválidas") })

        val repeated = deckProblems(cards, listOf(1, 1))
        assertTrue(repeated.any { it.contains("No puedes repetir") })
    }

    @Test
    fun deckCoverage_does_not_double_count_duplicate_deck_rows() {
        val cards = listOf(
            DeckCard("A1", 1, 1),
            DeckCard("A1", 1, 1),
        )
        val holdings = listOf(
            Holding("A1", 1, 1),
        )

        assertEquals(1 to 2, deckCoverage(cards, holdings))
    }

    @Test
    fun qtyOf_sums_duplicate_holding_rows() {
        val holdings = listOf(
            Holding("A1", 1, 1),
            Holding("A1", 1, 2),
        )

        assertEquals(3, qtyOf(holdings, "A1", 1))
    }

    @Test
    fun canonicalized_merges_duplicate_rows_for_same_card() {
        val deck = Deck(
            id = "deck-1",
            name = "Prueba",
            energies = listOf(1),
            cards = listOf(
                DeckCard("A1", 1, 1),
                DeckCard("A1", 1, 1),
                DeckCard("A2", 2, 1),
            ),
            updatedAt = 1L,
        )

        val canonical = deck.canonicalized()

        assertEquals(
            listOf(
                DeckCard("A1", 1, 2),
                DeckCard("A2", 2, 1),
            ),
            canonical.cards,
        )
    }
}
