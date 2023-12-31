package com.example.android.politicalpreparedness.data

import androidx.lifecycle.LiveData
import com.example.android.politicalpreparedness.data.dto.DataResult
import com.example.android.politicalpreparedness.network.models.Election
import com.example.android.politicalpreparedness.network.models.ElectionResponse
import com.example.android.politicalpreparedness.network.models.RepresentativeResponse
import com.example.android.politicalpreparedness.representative.model.Representative
import com.example.android.politicalpreparedness.representative.model.Representatives

interface ElectionData {

    suspend fun getElections(): DataResult<ElectionResponse>
    suspend fun getVoterInfo(address: String, electionId: Long): DataResult<VoterData>
    suspend fun insertElection(election: Election)
    fun getAllElections(): LiveData<List<Election>>
    suspend fun getElectionById(id: Long): Election?
    suspend fun deleteElection(election: Election)

    suspend fun getRepresentatives(address: String): DataResult<RepresentativeResponse>
    //fun getAllRepresentatives():LiveData<List<RepresentativeResponse>>

    suspend fun insertRepresentatives(representatives: Representatives)

    fun getAllRepresentatives():LiveData<Representatives>
}