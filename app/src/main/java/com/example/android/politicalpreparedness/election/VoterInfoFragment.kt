package com.example.android.politicalpreparedness.election

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.example.android.politicalpreparedness.R
import com.example.android.politicalpreparedness.databinding.FragmentVoterInfoBinding

class VoterInfoFragment : Fragment() {

    private lateinit var binding: FragmentVoterInfoBinding
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?)
    : View? {
        /**
         *   TODO: Add ViewModel values and create ViewModel
         *   TODO: Add binding values
         *   TODO: Populate voter info -- hide views without provided data.
         */

        /**
        Hint: You will need to ensure proper data is provided from previous fragment.
        */

        /**
         * TODO: Handle loading of URLs
         * TODO: Handle save button UI state
         * TODO: cont'd Handle save button clicks
         */

        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_voter_info, container, false)
        return null
    }

    // TODO: Create method to load URL intents
}