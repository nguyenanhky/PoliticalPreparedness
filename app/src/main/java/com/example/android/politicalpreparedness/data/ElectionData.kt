package com.example.android.politicalpreparedness.data

import androidx.lifecycle.LiveData
import com.example.android.politicalpreparedness.data.dto.DataResult
import com.example.android.politicalpreparedness.network.models.Election
import com.example.android.politicalpreparedness.network.models.ElectionResponse

interface ElectionData {

    suspend fun getElections(): DataResult<ElectionResponse>
    suspend fun getVoterInfo(address: String, electionId: Long): DataResult<VoterData>
    suspend fun insertElection(election: Election)
    fun getAllElections(): LiveData<List<Election>>
    suspend fun getElectionById(id: Long): Election?
    suspend fun deleteElection(election: Election)
}