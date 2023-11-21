package com.example.android.politicalpreparedness.representative

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.android.politicalpreparedness.data.dto.DataResult
import com.example.android.politicalpreparedness.database.ElectionDatabase
import com.example.android.politicalpreparedness.network.models.Address
import com.example.android.politicalpreparedness.repository.ElectionRepository
import com.example.android.politicalpreparedness.representative.model.Representative
import kotlinx.coroutines.launch

class RepresentativeViewModel(application: Application) : AndroidViewModel(application) {

    private val electionDatabase = ElectionDatabase.getInstance(application)
    private val electionRepository: ElectionRepository = ElectionRepository(electionDatabase)

    val addressLine1 = MutableLiveData<String>()
    val addressLine2 = MutableLiveData<String>()
    val city = MutableLiveData<String>()
    val state = MutableLiveData<String>()
    val zip = MutableLiveData<String>()

    private val _representatives = MutableLiveData<List<Representative>>()
    val representatives: LiveData<List<Representative>>
        get() = _representatives

    val isLoading: MutableLiveData<Boolean> = MutableLiveData()


    fun searchRepresentatives() = viewModelScope.launch {
        isLoading.value = true
        val result = electionRepository.getRepresentatives(locateAddress().toFormattedString())
        isLoading.value = false
        when (result) {
            is DataResult.Success -> {
                _representatives.value = result.data.offices.flatMap { office ->
                    office.getRepresentatives(result.data.officials)
                }
            }

            is DataResult.Error -> {
                _representatives.value = emptyList()
            }
        }
    }

    private fun locateAddress() = Address(
        addressLine1.value.toString(),
        addressLine2.value.toString(),
        city.value.toString(),
        state.value.toString(),
        zip.value.toString()
    )

    fun updateStateValue(newState: String) {
        state.value = newState
    }

    fun updateAddress(address: Address) {
        addressLine1.value = address.line1
        addressLine2.value = address.line2 ?: ""
        city.value = address.city
        state.value = address.state
        zip.value = address.zip
    }


    /**
     * Factory
     */
    class RepresentativeViewModelFactory(val application: Application) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(RepresentativeViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return RepresentativeViewModel(application) as T
            }
            throw IllegalArgumentException("ViewModel construction failed")
        }
    }
}
