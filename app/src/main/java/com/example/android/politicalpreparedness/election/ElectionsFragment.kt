package com.example.android.politicalpreparedness.election

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.example.android.politicalpreparedness.R
import com.example.android.politicalpreparedness.databinding.FragmentElectionBinding

class ElectionsFragment: Fragment() {

    // TODO: Declare ViewModel

    private lateinit var binding: FragmentElectionBinding
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?)
    : View? {
        /**
         * TODO: Add ViewModel values and create ViewModel
         * TODO: Add binding values
         * TODO: Link elections to voter info
         * TODO: Initiate recycler adapters
         * TODO: Populate recycler adapters
         */
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_election, container, false)


        return null
    }

    // TODO: Refresh adapters when fragment loads
}