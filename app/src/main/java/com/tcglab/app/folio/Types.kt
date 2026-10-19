package com.tcglab.app.folio

data class CardRow(
    val set: String,
    val number: Int,
    val name: String,
    val rarity: String,
    val entityId: Int,
    val trainer: Boolean,
    val element: String,
)

data class SetInfo(
    val code: String,
    val es: String,
    val en: String,
    val count: Int,
)

data class Holding(
    val set: String,
    val number: Int,
    val qty: Int,
    val name: String? = null,
    val rarity: String? = null,
)

data class DeckCard(
    val set: String,
    val number: Int,
    val count: Int,
)

data class Deck(
    val id: String,
    val name: String,
    val energies: List<Int>,
    val cards: List<DeckCard>,
    val updatedAt: Long,
)

data class CollectionChange(
    val set: String,
    val number: Int,
    val name: String,
    val from: Int,
    val to: Int,
)

data class EncodedCard(
    val entityId: Int,
    val trainer: Boolean,
    val count: Int,
)

data class ResolvedDeckCard(
    val set: String,
    val number: Int,
    val count: Int,
    val name: String,
    val entityId: Int,
    val trainer: Boolean,
    val rarity: String,
    val element: String,
)

data class CsvRow(
    val set: String,
    val number: Int,
    val qty: Int,
    val name: String? = null,
    val rarity: String? = null,
)
