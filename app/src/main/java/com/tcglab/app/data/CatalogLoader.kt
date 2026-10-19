package com.tcglab.app.data

import android.content.Context
import com.tcglab.app.folio.CardRow
import com.tcglab.app.folio.Catalog
import com.tcglab.app.folio.Holding
import com.tcglab.app.folio.SetInfo
import org.json.JSONArray

object CatalogLoader {
    fun ensureLoaded(context: Context) {
        if (Catalog.isLoaded()) return
        val setsJson = context.assets.open("sets.json").bufferedReader().use { it.readText() }
        val cardsJson = context.assets.open("cards.json").bufferedReader().use { it.readText() }
        Catalog.load(parseSets(setsJson), parseCards(cardsJson))
    }

    fun loadSeed(context: Context): List<Holding> {
        val text = context.assets.open("seed.json").bufferedReader().use { it.readText() }
        val array = JSONArray(text)
        return buildList {
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                add(
                    Holding(
                        set = obj.getString("set"),
                        number = obj.getInt("number"),
                        qty = obj.getInt("qty"),
                    ),
                )
            }
        }
    }

    private fun parseSets(text: String): List<SetInfo> {
        val array = JSONArray(text)
        return buildList {
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                add(
                    SetInfo(
                        code = obj.getString("code"),
                        es = obj.getString("es"),
                        en = obj.getString("en"),
                        count = obj.getInt("count"),
                    ),
                )
            }
        }
    }

    private fun parseCards(text: String): List<CardRow> {
        val array = JSONArray(text)
        return buildList {
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                add(
                    CardRow(
                        set = obj.getString("set"),
                        number = obj.getInt("number"),
                        name = obj.getString("name"),
                        rarity = obj.getString("rarity"),
                        entityId = obj.getInt("entityId"),
                        trainer = obj.getBoolean("trainer"),
                        element = obj.getString("element"),
                    ),
                )
            }
        }
    }
}
