package com.tcglab.app.folio

import java.text.Normalizer

data class EnergyDef(val id: Int, val key: String, val es: String)

val ENERGIES = listOf(
    EnergyDef(1, "grass", "Planta"),
    EnergyDef(2, "fire", "Fuego"),
    EnergyDef(3, "water", "Agua"),
    EnergyDef(4, "lightning", "Rayo"),
    EnergyDef(5, "psychic", "Psíquica"),
    EnergyDef(6, "fighting", "Lucha"),
    EnergyDef(7, "darkness", "Oscura"),
    EnergyDef(8, "metal", "Metálica"),
)

private val ENERGY_ALIASES = mapOf(
    "grass" to 1, "planta" to 1, "hoja" to 1,
    "fire" to 2, "fuego" to 2,
    "water" to 3, "agua" to 3,
    "lightning" to 4, "electric" to 4, "electrico" to 4, "rayo" to 4,
    "psychic" to 5, "psiquica" to 5, "psiquico" to 5,
    "fighting" to 6, "fight" to 6, "lucha" to 6,
    "darkness" to 7, "dark" to 7, "oscura" to 7, "oscuro" to 7,
    "metal" to 8, "steel" to 8, "metalica" to 8, "acero" to 8,
)

data class RarityInfo(val es: String, val group: String, val count: Int)

val RARITIES = mapOf(
    "C" to RarityInfo("Común", "diamond", 1),
    "U" to RarityInfo("Poco común", "diamond", 2),
    "R" to RarityInfo("Rara", "diamond", 3),
    "RR" to RarityInfo("Doble rara", "diamond", 4),
    "AR" to RarityInfo("Arte rara", "star", 1),
    "SR" to RarityInfo("Súper rara", "star", 2),
    "SAR" to RarityInfo("Arte especial", "star", 2),
    "IM" to RarityInfo("Inmersiva", "star", 3),
    "UR" to RarityInfo("Corona", "crown", 1),
    "S" to RarityInfo("Shiny", "star", 1),
    "SSR" to RarityInfo("Shiny súper rara", "star", 2),
)

private val MARKS = mapOf(
    "C" to "♦",
    "U" to "♦♦",
    "R" to "♦♦♦",
    "RR" to "♦♦♦♦",
    "AR" to "★",
    "SR" to "★★",
    "SAR" to "★★",
    "IM" to "★★★",
    "UR" to "♛",
    "S" to "★",
    "SSR" to "★★",
)

fun fold(value: String): String {
    val normalized = Normalizer.normalize(value, Normalizer.Form.NFD)
    return normalized
        .replace("\\p{InCombiningDiacriticalMarks}+".toRegex(), "")
        .replace(Regex("[\'’‘ʼ]"), "")
        .lowercase()
        .trim()
}

fun energyById(id: Int): EnergyDef? = ENERGIES.find { it.id == id }

fun parseEnergy(value: Any?): Int? {
    when (value) {
        is Number -> {
            val id = value.toInt()
            return if (ENERGIES.any { it.id == id }) id else null
        }
        is String -> {
            val key = fold(value).replace(Regex("[^a-z]"), "")
            return ENERGY_ALIASES[key]
        }
        else -> return null
    }
}

fun energyLabel(ids: List<Int>): String =
    ids.mapNotNull { energyById(it)?.es }.joinToString(" · ")

fun rarityMark(code: String): String = MARKS[code] ?: code

fun padNumber(number: Int): String = number.toString().padStart(3, '0')

fun isExCard(name: String, rarity: String): Boolean =
    rarity == "RR" || name.trim().endsWith(" ex", ignoreCase = true)
