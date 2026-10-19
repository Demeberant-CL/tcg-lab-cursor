package com.tcglab.app.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "holdings")
data class HoldingEntity(
    @PrimaryKey val key: String,
    val setCode: String,
    val number: Int,
    val qty: Int,
)

@Entity(tableName = "decks")
data class DeckEntity(
    @PrimaryKey val id: String,
    val name: String,
    val energiesJson: String,
    val cardsJson: String,
    val updatedAt: Long,
)

@Entity(tableName = "meta")
data class MetaEntity(
    @PrimaryKey val key: String,
    val value: String,
)

@Entity(tableName = "chat_messages")
data class ChatMessageEntity(
    @PrimaryKey val id: String,
    val role: String,
    val content: String,
    val changesJson: String? = null,
    val deckName: String? = null,
    val deckEvent: String? = null,
    val createdAt: Long,
)
