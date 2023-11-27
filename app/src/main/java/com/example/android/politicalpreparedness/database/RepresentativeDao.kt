package com.example.android.politicalpreparedness.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.android.politicalpreparedness.network.models.Election
import com.example.android.politicalpreparedness.representative.model.Representatives

@Dao
interface RepresentativeDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRepresentative(representatives: Representatives)

    @Query("SELECT * FROM representative_table")
    fun getAllElections() : LiveData<Representatives>


}