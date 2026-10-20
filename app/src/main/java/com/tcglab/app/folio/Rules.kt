package com.tcglab.app.folio

fun resolveDeck(cards: List<DeckCard>): List<ResolvedDeckCard> =
    cards.map { card ->
        val known = Catalog.findCard(card.set, card.number)
        ResolvedDeckCard(
            set = known?.set ?: card.set,
            number = card.number,
            count = card.count,
            name = known?.name ?: "${card.set} ${card.number}",
            entityId = known?.entityId ?: 0,
            trainer = known?.trainer ?: false,
            rarity = known?.rarity.orEmpty(),
            element = known?.element.orEmpty(),
        )
    }

fun deckTotal(cards: List<DeckCard>): Int = cards.sumOf { it.count }

fun deckTotalResolved(cards: List<ResolvedDeckCard>): Int = cards.sumOf { it.count }

fun deckProblems(cards: List<ResolvedDeckCard>, energies: List<Int>): List<String> {
    val problems = mutableListOf<String>()
    val total = deckTotalResolved(cards)
    when {
        total < 20 -> problems += "Faltan ${20 - total} cartas para las 20."
        total > 20 -> problems += "Sobran ${total - 20} cartas. El mazo lleva 20."
    }
    if (energies.isEmpty() || energies.size > 3) {
        problems += "Elige entre 1 y 3 energías."
    } else {
        val invalidEnergies = energies.filter { it !in 1..8 }.distinct()
        if (invalidEnergies.isNotEmpty()) {
            problems += "Hay energías inválidas: ${invalidEnergies.joinToString(", ")}."
        }
        if (energies.size != energies.toSet().size) {
            problems += "No puedes repetir el mismo tipo de energía."
        }
    }
    val byName = linkedMapOf<String, Pair<String, Int>>()
    for (card in cards) {
        if (card.count < 1) problems += "${card.name} tiene una cantidad inválida."
        if (card.entityId == 0) problems += "${card.name} todavía no tiene código para el QR."
        val key = fold(card.name)
        val prev = byName[key]
        byName[key] = (prev?.first ?: card.name) to ((prev?.second ?: 0) + card.count)
    }
    for ((_, bucket) in byName) {
        if (bucket.second > 2) {
            problems += "${bucket.first}: hay ${bucket.second} y el máximo es 2."
        }
    }
    val byEntity = linkedMapOf<String, Pair<MutableSet<String>, Int>>()
    for (card in cards) {
        if (card.entityId == 0) continue
        val key = "${if (card.trainer) "t" else "p"}:${card.entityId}"
        val bucket = byEntity.getOrPut(key) { mutableSetOf<String>() to 0 }
        bucket.first += card.name
        byEntity[key] = bucket.first to (bucket.second + card.count)
    }
    for ((_, bucket) in byEntity) {
        if (bucket.second > 2 && bucket.first.size > 1) {
            problems += "${bucket.first.joinToString(" / ")}: juntas suman ${bucket.second} y el máximo es 2."
        }
    }
    return problems
}

fun canEncode(cards: List<ResolvedDeckCard>, energies: List<Int>): Boolean =
    deckProblems(cards, energies).isEmpty()

fun encodeResolved(cards: List<ResolvedDeckCard>, energies: List<Int>): String =
    encodeDeckCode(
        cards.map { EncodedCard(it.entityId, it.trainer, it.count) },
        energies,
    )

fun qtyOf(holdings: List<Holding>, set: String, number: Int): Int =
    holdings.find { it.set == set && it.number == number }?.qty ?: 0

fun deckCoverage(cards: List<DeckCard>, holdings: List<Holding>): Pair<Int, Int> {
    var have = 0
    for (card in cards) {
        have += minOf(card.count, qtyOf(holdings, card.set, card.number))
    }
    return have to deckTotal(cards)
}

fun nameCopies(cards: List<DeckCard>, set: String, number: Int): Int {
    val card = Catalog.findCard(set, number)
    val name = fold(card?.name ?: "$set $number")
    return cards.sumOf { entry ->
        val entryName = fold(Catalog.findCard(entry.set, entry.number)?.name ?: "${entry.set} ${entry.number}")
        if (entryName == name) entry.count else 0
    }
}

fun entityCopies(cards: List<DeckCard>, set: String, number: Int): Int {
    val card = Catalog.findCard(set, number) ?: return 0
    if (card.entityId == 0) return 0
    return cards.sumOf { entry ->
        val known = Catalog.findCard(entry.set, entry.number)
        if (known != null && known.entityId == card.entityId && known.trainer == card.trainer) {
            entry.count
        } else {
            0
        }
    }
}

fun copyLimitReached(cards: List<DeckCard>, set: String, number: Int): Boolean =
    nameCopies(cards, set, number) >= 2 || entityCopies(cards, set, number) >= 2
