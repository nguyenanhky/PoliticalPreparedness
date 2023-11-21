package com.example.android.politicalpreparedness.election

import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.android.politicalpreparedness.R
import com.example.android.politicalpreparedness.databinding.FragmentVoterInfoBinding

class VoterInfoFragment : Fragment() {

    private lateinit var binding: FragmentVoterInfoBinding
    private lateinit var voterInfoViewModel: VoterInfoViewModel
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    )
            : View? {

        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_voter_info, container, false)
        val application = requireNotNull(activity).application
        val selectedElection = VoterInfoFragmentArgs.fromBundle(requireArguments()).selectedElection
        val viewModelFactory =
            VoterInfoViewModel.VoteInfoViewModelFactory(selectedElection, application)
        voterInfoViewModel =
            ViewModelProvider(this, viewModelFactory)[VoterInfoViewModel::class.java]
        binding.viewModel = voterInfoViewModel
        binding.lifecycleOwner = this
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initializeUI()
        observerUrl()
    }

    private fun initializeUI() {
        voterInfoViewModel.voterData.observe(viewLifecycleOwner, Observer { voteData ->
            enableLinkInMessage(
                binding.txtVotingLocations,
                getString(R.string.voting_locations),
                voteData.votingLocationFinderUrl
            )
            enableLinkInMessage(
                binding.txtBallotInformation,
                getString(R.string.ballot_information),
                voteData.ballotInfoUrl
            )
        })
    }

    private fun observerUrl() {
       voterInfoViewModel.url.observe(viewLifecycleOwner,Observer{
           val intent = Intent(Intent.ACTION_VIEW)
           intent.data = Uri.parse(it)
           startActivity(intent)
       })
    }

    private fun enableLinkInMessage(textView: TextView, pattern: String, url: String) {
        val clickableSpan = createClickableSpan(url)

        val startIndex = pattern.indexOf("{url}")
        val endIndex = startIndex + url.length

        val content = pattern.replace("{url}", url)
        val spannableString = createSpannableString(content, clickableSpan, startIndex, endIndex)

        setSpannableText(textView, spannableString)
    }

    private fun createClickableSpan(url: String): ClickableSpan {
        return object : ClickableSpan() {
            override fun onClick(textView: View) {
                val intent = Intent(Intent.ACTION_VIEW)
                intent.data = Uri.parse(url)
                startActivity(intent)
            }
        }
    }

    private fun createSpannableString(
        content: String,
        clickableSpan: ClickableSpan,
        startIndex: Int,
        endIndex: Int,
    ): SpannableString {
        val spannableString = SpannableString(content)
        spannableString.setSpan(
            clickableSpan,
            startIndex,
            endIndex,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        return spannableString
    }

    private fun setSpannableText(textView: TextView, spannableString: SpannableString) {
        textView.text = spannableString
        textView.movementMethod = LinkMovementMethod.getInstance()
        textView.highlightColor = Color.TRANSPARENT
    }


    // TODO: Create method to load URL intents
}