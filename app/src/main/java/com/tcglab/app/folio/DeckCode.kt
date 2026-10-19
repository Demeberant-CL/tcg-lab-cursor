package com.tcglab.app.folio

import java.util.Base64

private const val TRAINER_OFFSET = 10_000_000
private const val MAX_U24 = 0xffffff

private fun pushU24(bytes: MutableList<Int>, value: Int) {
    require(value in 0..MAX_U24) { "El código de la carta no entra en el formato del juego." }
    bytes += (value ushr 16) and 0xff
    bytes += (value ushr 8) and 0xff
    bytes += value and 0xff
}

/** Base64 payload scanned by the game (trainer ids, pokémon ids, energies). */
fun encodeDeckCode(cards: List<EncodedCard>, energies: List<Int>): String {
    val trainers = mutableListOf<Int>()
    val pokemon = mutableListOf<Int>()
    for (card in cards) {
        require(card.entityId > 0) { "Hay una carta sin código de juego." }
        if (card.count < 1) continue
        val list = if (card.trainer) trainers else pokemon
        repeat(card.count) { list += card.entityId }
    }
    trainers.sort()
    pokemon.sort()
    require(trainers.size <= 255 && pokemon.size <= 255) { "El mazo tiene demasiadas cartas." }

    val bytes = mutableListOf<Int>()
    bytes += trainers.size
    for (id in trainers) pushU24(bytes, id + TRAINER_OFFSET)
    bytes += pokemon.size
    for (id in pokemon) pushU24(bytes, id)
    require(energies.size <= 3) { "Máximo 3 energías." }
    bytes += energies.size
    for (energy in energies) {
        require(energy in 1..8) { "Energía inválida." }
        bytes += energy
    }
    val array = ByteArray(bytes.size) { bytes[it].toByte() }
    return Base64.getEncoder().encodeToString(array)
}
