package com.cortisense.app

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ChatMessageDao {
    @Query("SELECT * FROM chat_messages WHERE userEmail = :email ORDER BY timestamp ASC")
    fun getMessagesForUser(email: String): Flow<List<ChatMessageEntity>>

    @Insert
    suspend fun insertChatMessage(message: ChatMessageEntity): Long

    @Query("DELETE FROM chat_messages WHERE userEmail = :email")
    suspend fun deleteUserChatMessages(email: String): Int
}
