package com.example.android.politicalpreparedness.election

import android.app.Application
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.android.politicalpreparedness.R
import com.example.android.politicalpreparedness.databinding.FragmentElectionBinding
import com.example.android.politicalpreparedness.direction.NavigationAction
import com.example.android.politicalpreparedness.election.adapter.ElectionListAdapter
import com.example.android.politicalpreparedness.repository.ElectionRepository
import com.example.android.politicalpreparedness.utlis.Logger

class ElectionsFragment : Fragment() {

    // TODO: Declare ViewModel

    private lateinit var binding: FragmentElectionBinding


    private val viewModel: ElectionsViewModel by lazy {
        val activity = requireNotNull(this.activity) {
            "The viewModel is not available until onViewCreated() is called"
        }
        ViewModelProvider(this, ElectionsViewModel.ElectionViewModelFactory(activity.application))[ElectionsViewModel::class.java]
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_election, container, false)
        binding.electionListViewModel = viewModel
        binding.lifecycleOwner = this
        return binding.root

    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()
    }
    private fun init() {
        upComingElections()
        saveElections()
    }

    private fun upComingElections() {
        val electionListAdapter = ElectionListAdapter(ElectionListAdapter.ElectionListener {
            viewModel.onElectionItemClick(it)
        })

        binding.rcvUpcomingElection.adapter = electionListAdapter
        viewModel.elections.observe(viewLifecycleOwner, Observer {elections->
            electionListAdapter.submitList(elections)
        })

        viewModel.navigationAction.observe(viewLifecycleOwner,Observer{
            when (it) {
                is NavigationAction.Open-> findNavController().navigate(it.directions)
                is NavigationAction.Back -> findNavController().popBackStack()
                is NavigationAction.BackTo -> findNavController().popBackStack(
                    it.destinationId,
                    false
                )
            }
        })
    }

    private fun saveElections() {
        val savedElectionListAdapter = ElectionListAdapter(ElectionListAdapter.ElectionListener {
            viewModel.onElectionItemClick(it)
        })

        binding.rcvSavedElection.adapter = savedElectionListAdapter
        viewModel.savedElections.observe(viewLifecycleOwner, Observer { elections ->
            savedElectionListAdapter.submitList(elections)
        })
    }


}