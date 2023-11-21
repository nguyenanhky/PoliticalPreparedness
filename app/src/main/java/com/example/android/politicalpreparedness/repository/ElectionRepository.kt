package com.example.android.politicalpreparedness.repository

import androidx.lifecycle.LiveData
import com.example.android.politicalpreparedness.data.ElectionData
import com.example.android.politicalpreparedness.data.VoterData
import com.example.android.politicalpreparedness.data.dto.DataResult
import com.example.android.politicalpreparedness.database.ElectionDatabase
import com.example.android.politicalpreparedness.network.CivicsApi
import com.example.android.politicalpreparedness.network.CivicsApiService
import com.example.android.politicalpreparedness.network.models.Election
import com.example.android.politicalpreparedness.network.models.ElectionResponse
import com.example.android.politicalpreparedness.network.models.RepresentativeResponse
import com.example.android.politicalpreparedness.network.models.asVoterInformation
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext


class ElectionRepository(
    private val electionDatabase: ElectionDatabase,
): ElectionData {
    val service: CivicsApiService = CivicsApi.retrofitService

    override suspend fun getElections(): DataResult<ElectionResponse> = withContext(Dispatchers.IO){
        return@withContext try {
            DataResult.Success(service.getElections())
        } catch (ex: Exception) {
            DataResult.Error(ex.localizedMessage)
        }
    }


    override suspend fun getVoterInfo(address: String, electionId: Long): DataResult<VoterData> {

        return try {
            DataResult.Success(service.getVoterInfo(address, electionId).asVoterInformation())
        } catch (ex: Exception) {
            DataResult.Error(ex.localizedMessage)
        }
    }

    override suspend fun insertElection(election: Election) {
        withContext(Dispatchers.IO) {
            electionDatabase.electionDao.insertElection(election)
        }
    }

    override fun getAllElections(): LiveData<List<Election>>  = electionDatabase.electionDao.getAllElections()

    override suspend fun getElectionById(id: Long): Election? = withContext(Dispatchers.IO) {
        electionDatabase.electionDao.getElectionById(id)
    }



    override suspend fun deleteElection(election: Election) {
        withContext(Dispatchers.IO) {
            electionDatabase.electionDao.deleteElection(election)
        }
    }

    override suspend fun getRepresentatives(address: String): DataResult<RepresentativeResponse> = withContext(Dispatchers.IO) {
        return@withContext try {
            DataResult.Success(service.getRepresentativesByAddress(address))
        } catch (ex: Exception) {
            DataResult.Error(ex.localizedMessage)
        }
    }


}