package com.example.android.politicalpreparedness.election

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.android.politicalpreparedness.R
import com.example.android.politicalpreparedness.data.VoterData
import com.example.android.politicalpreparedness.data.dto.DataResult
import com.example.android.politicalpreparedness.database.ElectionDao
import com.example.android.politicalpreparedness.database.ElectionDatabase
import com.example.android.politicalpreparedness.network.CivicsApi
import com.example.android.politicalpreparedness.network.CivicsApiService
import com.example.android.politicalpreparedness.network.models.Election
import com.example.android.politicalpreparedness.repository.ElectionRepository
import com.example.android.politicalpreparedness.utlis.Logger
import com.example.android.politicalpreparedness.utlis.SingleDataEvent
import kotlinx.coroutines.launch

class VoterInfoViewModel(val election: Election, application: Application) :
    AndroidViewModel(application) {

    //TODO: Add live data to hold voter info

    //TODO: Add var and methods to populate voter info

    //TODO: Add var and methods to support loading URLs

    //TODO: Add var and methods to save and remove elections to local database
    //TODO: cont'd -- Populate initial state of save button to reflect proper action based on election saved status

    /**
     * Hint: The saved state can be accomplished in multiple ways. It is directly related to how elections are saved/removed from the database.
     */

    private val database = ElectionDatabase.getInstance(application)
    private val electionRepository: ElectionRepository = ElectionRepository(database)

    private var _voterData = MutableLiveData<VoterData>()
    val voterData:LiveData<VoterData>
        get() = _voterData

    val url: SingleDataEvent<String> = SingleDataEvent()

    private var _isFollow = MutableLiveData<Boolean>()
    val isFollow: LiveData<Boolean>
        get() = _isFollow



    init {
        checkIsFollowing()
        fetchVoterInfo()
    }

    private fun checkIsFollowing() {
        viewModelScope.launch {
            _isFollow.value = electionRepository.getElectionById(election.id) != null
        }
    }
    private fun fetchVoterInfo() {
        viewModelScope.launch {
            if (election.division.state.isNotEmpty()) {
                val address = "${election.division.country},${election.division.state}"
                val result = electionRepository.getVoterInfo(address, election.id)
                when (result) {
                    is DataResult.Success -> {
                        _voterData.value = result.data
                    }

                    is DataResult.Error -> {
                        _voterData.value = VoterData()
                        //showToast.value = app.getString(R.string.error_voter_information)
                    }
                }
            }
        }
    }

    fun toggleElection(election: Election) {
        viewModelScope.launch {
            if (isFollow.value == true) {
                electionRepository.deleteElection(election)
            } else {
                electionRepository.insertElection(election)
            }
            checkIsFollowing()
        }
    }
    fun openURL(url:String){
       this.url.value = url
    }

    /**
     * Factory dependency injection
     */
    class VoteInfoViewModelFactory(val election: Election, val application: Application):ViewModelProvider.Factory{
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(VoterInfoViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return VoterInfoViewModel(election,application) as T
            }
            throw IllegalArgumentException("ViewModel construction failed")
        }
    }

}