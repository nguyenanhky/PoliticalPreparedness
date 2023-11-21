package com.example.android.politicalpreparedness.election

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.android.politicalpreparedness.R
import com.example.android.politicalpreparedness.data.dto.DataResult
import com.example.android.politicalpreparedness.database.ElectionDatabase.Companion.getInstance
import com.example.android.politicalpreparedness.direction.NavigationAction
import com.example.android.politicalpreparedness.network.models.Election
import com.example.android.politicalpreparedness.repository.ElectionRepository
import com.example.android.politicalpreparedness.utlis.SingleDataEvent
import kotlinx.coroutines.launch

//TODO: Construct ViewModel and provide election datasource
class ElectionsViewModel(private val application: Application): AndroidViewModel(application) {

    private val electionDatabase = getInstance(application)
    private val electionRepository: ElectionRepository = ElectionRepository(electionDatabase)


    val navigationAction: SingleDataEvent<NavigationAction> = SingleDataEvent()

    val isLoading: MutableLiveData<Boolean> = MutableLiveData()
    val showToast: SingleDataEvent<String> = SingleDataEvent()


    private val _elections = MutableLiveData<List<Election>>()
    val elections: LiveData<List<Election>>
        get() = _elections



    val savedElections = electionRepository.getAllElections()

    init {
        fetchElections()
    }

    private fun fetchElections() = viewModelScope.launch {
        isLoading.value = true
        val result = electionRepository.getElections()
        isLoading.value = false
        when (result) {
            is DataResult.Success -> _elections.value = result.data.elections
            is DataResult.Error -> {
                _elections.value = emptyList()
                showToast.value = application.getString(R.string.error_election)
            }
        }
    }

    fun onElectionItemClick(election: Election) {
        navigationAction.value = NavigationAction.Open(
            ElectionsFragmentDirections.actionElectionsFragmentToVoterInfoFragment(
                election
            )
        )
    }

    /**
     * Factor(dependency injection)
     */
    class ElectionViewModelFactory(val application: Application):ViewModelProvider.Factory{
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(ElectionsViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return ElectionsViewModel(application) as T
            }
            throw IllegalArgumentException("ViewModel construction failed")
        }
    }

}