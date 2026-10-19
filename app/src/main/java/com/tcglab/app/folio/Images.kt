package com.tcglab.app.folio

/**
 * Fuentes de imagen (Pocket):
 * 1. CDN Limitless (principal, verificado)
 * 2. Variante SM de Limitless
 * 3. Espejo jsDelivr del dataset flibustier (si está publicado)
 */
object CardImages {
    fun primaryUrl(set: String, number: Int): String {
        val code = imageSetCode(set)
        val num = number.toString().padStart(3, '0')
        return "https://limitlesstcg.nyc3.cdn.digitaloceanspaces.com/pocket/$code/${code}_${num}_EN.webp"
    }

    fun fallbackUrls(set: String, number: Int): List<String> {
        val code = imageSetCode(set)
        val num = number.toString().padStart(3, '0')
        val bare = number.toString()
        return listOf(
            primaryUrl(set, number),
            "https://limitlesstcg.nyc3.cdn.digitaloceanspaces.com/pocket/$code/${code}_${num}_EN_SM.webp",
            "https://cdn.jsdelivr.net/gh/flibustier/pokemon-tcg-pocket-database@main/dist/cards-by-set/$set/$bare.webp",
            "https://cdn.jsdelivr.net/npm/pokemon-tcg-pocket-database/dist/cards-by-set/$set/$bare.webp",
        ).distinct()
    }

    private fun imageSetCode(set: String): String = when (set) {
        "PROMO-A" -> "P-A"
        "PROMO-B" -> "P-B"
        else -> set
    }
}
