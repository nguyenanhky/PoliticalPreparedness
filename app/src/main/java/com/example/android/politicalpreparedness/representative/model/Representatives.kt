package com.example.android.politicalpreparedness.representative.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import org.jetbrains.annotations.NotNull
import java.io.Serializable

@Entity(tableName = "representative_table")
data class Representatives(
    @PrimaryKey
    val id: Int = 0,
    val representatives: List<Representative>
):Serializable