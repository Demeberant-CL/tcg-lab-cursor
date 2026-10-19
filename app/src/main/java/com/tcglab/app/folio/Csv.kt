package com.tcglab.app.folio

private fun parseTable(text: String): List<List<String>> {
    val rows = mutableListOf<List<String>>()
    var row = mutableListOf<String>()
    var current = StringBuilder()
    var quoted = false
    var i = 0
    while (i < text.length) {
        val char = text[i]
        if (quoted) {
            if (char == '"') {
                if (i + 1 < text.length && text[i + 1] == '"') {
                    current.append('"')
                    i += 1
                } else {
                    quoted = false
                }
            } else {
                current.append(char)
            }
        } else when (char) {
            '"' -> quoted = true
            ',' -> {
                row += current.toString()
                current = StringBuilder()
            }
            '\n' -> {
                row += current.toString()
                rows += row
                row = mutableListOf()
                current = StringBuilder()
            }
            '\r' -> Unit
            else -> current.append(char)
        }
        i += 1
    }
    if (current.isNotEmpty() || row.isNotEmpty()) {
        row += current.toString()
        rows += row
    }
    return rows.filter { cells -> cells.any { it.trim().isNotEmpty() } }
}

private fun canonRarity(value: String): String? {
    val code = value.trim().replace(Regex("^rarity-", RegexOption.IGNORE_CASE), "")
    return code.ifEmpty { null }
}

fun parseCollectionCsv(text: String): List<CsvRow> {
    val table = parseTable(text.removePrefix("\uFEFF"))
    if (table.isEmpty()) return emptyList()
    val header = table[0].map { it.trim().lowercase() }
    val hasHeader = header.contains("set") &&
        (header.contains("id") || header.contains("number") || header.contains("cantidad") ||
            header.contains("número") || header.contains("numero"))
    val body = if (hasHeader) table.drop(1) else table

    fun index(names: List<String>, fallback: Int): Int {
        for (name in names) {
            val found = header.indexOf(name)
            if (found >= 0) return found
        }
        return fallback
    }

    val setIndex = if (hasHeader) index(listOf("set"), 1) else 1
    val idIndex = if (hasHeader) index(listOf("id", "number", "numero", "número"), 2) else 2
    val qtyIndex = if (hasHeader) index(listOf("cantidad", "qty", "quantity", "count"), 5) else 5
    val nameIndex = if (hasHeader) index(listOf("nombre", "name"), 3) else 3
    val rarityIndex = if (hasHeader) index(listOf("rareza", "rarity"), 4) else 4

    val rows = mutableListOf<CsvRow>()
    for (cells in body) {
        val set = Catalog.canonSet(cells.getOrNull(setIndex).orEmpty())
        val number = cells.getOrNull(idIndex)?.toIntOrNull() ?: continue
        val qty = cells.getOrNull(qtyIndex)?.trim()?.toIntOrNull() ?: 0
        if (set.isEmpty() || number < 1 || qty < 0) continue
        val name = cells.getOrNull(nameIndex)?.trim().orEmpty()
        val rarity = canonRarity(cells.getOrNull(rarityIndex).orEmpty())
        rows += CsvRow(
            set = set,
            number = number,
            qty = qty.coerceAtMost(99),
            name = name.ifEmpty { null },
            rarity = rarity,
        )
    }
    return rows
}

private fun csvCell(value: Any): String {
    val text = value.toString()
    return if (Regex("[\",\\n]").containsMatchIn(text)) {
        "\"${text.replace("\"", "\"\"")}\""
    } else {
        "\"$text\""
    }
}

fun holdingsToCsv(holdings: List<Holding>): String {
    val lines = mutableListOf("Seccion,Set,ID,Nombre,Rareza,Cantidad,Registrada")
    val sorted = holdings.sortedWith(compareBy({ it.set }, { it.number }))
    for (holding in sorted) {
        val card = Catalog.findCard(holding.set, holding.number)
        val name = card?.name ?: holding.name.orEmpty()
        val rarity = if (card != null) rarityMark(card.rarity) else holding.rarity.orEmpty()
        lines += listOf(
            csvCell("Cards"),
            csvCell(holding.set),
            csvCell(holding.number),
            csvCell(name),
            csvCell(rarity),
            csvCell(holding.qty),
            csvCell(if (holding.qty > 0) "sí" else "no"),
        ).joinToString(",")
    }
    return lines.joinToString("\n", postfix = "\n")
}
