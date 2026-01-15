package com.asistente.core.data.local.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.asistente.core.domain.models.Recordatory
import kotlinx.coroutines.flow.Flow

@Dao
interface RecordatoryDao {

        @Query("SELECT * FROM recordatories where owners LIKE '%' || :id || '%'")
        fun getAllRecordatorysByUserId(id: String): Flow<List<Recordatory>>

        @Query("SELECT * FROM recordatories where owners LIKE '%' || :id || '%'")
        fun getAllRecordatoryListByUserId(id: String): List<Recordatory>

        @Query("SELECT * FROM recordatories where parentCalendarId LIKE :id")
        fun getAllRecordatorysByCalendarId(id: String): Flow<List<Recordatory>>

        @Query("SELECT * FROM recordatories where parentCalendarId LIKE :id")
        fun getAllRecordatoryList(id: String): List<Recordatory>

        @Query("SELECT * FROM recordatories where id = :id")
        fun getRecordatoryById(id: String): Recordatory?

        @Insert(onConflict = OnConflictStrategy.REPLACE)
        suspend fun insertRecordatory(recordatory: Recordatory)

        @Query("DELETE FROM recordatories WHERE id = :id")
        suspend fun deleteRecordatoryById(id: String)

}