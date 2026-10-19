package com.tcglab.app.folio

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CsvImportTest {
    @Test
    fun blankCsvHasNoRows() {
        assertTrue(parseCollectionCsv(" \n\r\n").isEmpty())
    }

    @Test
    fun headerOnlyCsvHasNoRows() {
        val csv = "Seccion,Set,ID,Nombre,Rareza,Cantidad,Registrada\n"
        assertTrue(parseCollectionCsv(csv).isEmpty())
    }

    @Test(expected = IllegalArgumentException::class)
    fun unmatchedQuoteRejectsWholeImport() {
        parseCollectionCsv("\"Seccion,Set,ID,Nombre,Rareza,Cantidad,Registrada")
    }

    @Test(expected = IllegalArgumentException::class)
    fun invalidCardNumberRejectsWholeImport() {
        val csv = "Seccion,Set,ID,Nombre,Rareza,Cantidad,Registrada\nCards,A1,xyz,Pikachu,rarity-1,1,sí"
        parseCollectionCsv(csv)
    }

    @Test(expected = IllegalArgumentException::class)
    fun invalidQuantityRejectsWholeImport() {
        val csv = "Seccion,Set,ID,Nombre,Rareza,Cantidad,Registrada\nCards,A1,1,Pikachu,rarity-1,999,sí"
        parseCollectionCsv(csv)
    }

    @Test
    fun validRowParsesWithoutClamping() {
        Catalog.load(emptyList(), emptyList())
        val csv = "Seccion,Set,ID,Nombre,Rareza,Cantidad,Registrada\nCards,A1,1,Pikachu,rarity-1,2,sí"

        val rows = parseCollectionCsv(csv)

        assertEquals(1, rows.size)
        assertEquals("A1", rows.single().set)
        assertEquals(1, rows.single().number)
        assertEquals(2, rows.single().qty)
    }
}
