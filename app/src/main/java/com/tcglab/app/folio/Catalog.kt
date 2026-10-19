package com.tcglab.app.folio

object Catalog {
    lateinit var sets: List<SetInfo>
        private set
    lateinit var cards: List<CardRow>
        private set

    private var byKey: Map<String, CardRow> = emptyMap()
    private var setIndex: Map<String, Int> = emptyMap()

    private val imageSet = mapOf(
        "PROMO-A" to "P-A",
        "PROMO-B" to "P-B",
    )

    private val aliases = mapOf(
        "p-a" to "PROMO-A",
        "pa" to "PROMO-A",
        "promo a" to "PROMO-A",
        "promo-a" to "PROMO-A",
        "p-b" to "PROMO-B",
        "pb" to "PROMO-B",
        "promo b" to "PROMO-B",
        "promo-b" to "PROMO-B",
    )

    fun load(sets: List<SetInfo>, cards: List<CardRow>) {
        this.sets = sets
        this.cards = cards
        byKey = cards.associateBy { "${it.set}#${it.number}" }
        setIndex = sets.mapIndexed { index, set -> set.code to index }.toMap()
    }

    fun isLoaded(): Boolean = ::sets.isInitialized && ::cards.isInitialized

    fun canonSet(input: String): String {
        val compact = fold(input).replace(Regex("[\\s_]+"), "")
        val spaced = fold(input).replace(Regex("\\s+"), " ")
        aliases[spaced]?.let { return it }
        aliases[compact]?.let { return it }
        return sets.find { it.code.lowercase() == compact }?.code ?: input.trim()
    }

    fun findCard(set: String, number: Int): CardRow? =
        byKey["${canonSet(set)}#$number"]

    fun cardKey(set: String, number: Int): String = "$set#$number"

    fun cardImageUrl(set: String, number: Int): String = CardImages.primaryUrl(set, number)

    fun cardImageFallbacks(set: String, number: Int): List<String> = CardImages.fallbackUrls(set, number)

    fun setInfo(code: String): SetInfo? = sets.find { it.code == code }

    fun compareCards(a: CardRow, b: CardRow): Int {
        val ia = setIndex[a.set] ?: 999
        val ib = setIndex[b.set] ?: 999
        if (ia != ib) return ia - ib
        return a.number - b.number
    }

    fun displayCard(holding: Holding): CardRow =
        findCard(holding.set, holding.number) ?: CardRow(
            set = holding.set,
            number = holding.number,
            name = holding.name?.trim()?.takeIf { it.isNotEmpty() } ?: "${holding.set} ${holding.number}",
            rarity = holding.rarity.orEmpty(),
            entityId = 0,
            trainer = false,
            element = "",
        )

    fun searchCards(query: String, set: String? = null, limit: Int = 40): List<CardRow> {
        val capped = limit.coerceIn(1, 80)
        val setFilter = set?.let { canonSet(it) }
        val raw = query.trim()
        val q = fold(raw)
        if (q.isEmpty()) return emptyList()

        val idMatch = Regex("^([a-z]+-?[a-z0-9]+)\\s*[#-]\\s*(\\d{1,4})$").find(q)
            ?: Regex("^([a-z]\\d[a-z]?|promo-[ab])\\s+(\\d{1,4})$").find(q)

        val hits = mutableListOf<Pair<CardRow, Int>>()
        for (card in cards) {
            if (setFilter != null && card.set != setFilter) continue
            var score = 0
            val name = fold(card.name)
            if (idMatch != null) {
                val wantedSet = canonSet(idMatch.groupValues[1])
                val wantedNumber = idMatch.groupValues[2].toInt()
                if (card.set == wantedSet && card.number == wantedNumber) score = 200
            }
            when {
                name == q -> score = maxOf(score, 100)
                name.startsWith(q) -> score = maxOf(score, 80)
                name.contains(q) -> score = maxOf(score, 50)
                q.length >= 2 && "${card.set} ${card.number}".lowercase().contains(q) ->
                    score = maxOf(score, 40)
            }
            if (score > 0) hits += card to score
        }
        return hits
            .sortedWith(
                compareByDescending<Pair<CardRow, Int>> { it.second }
                    .thenComparator { a, b -> compareCards(a.first, b.first) },
            )
            .take(capped)
            .map { it.first }
    }
}
