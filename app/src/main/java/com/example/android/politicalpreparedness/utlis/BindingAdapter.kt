package com.example.android.politicalpreparedness.utlis

import android.widget.TextView
import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.android.politicalpreparedness.election.adapter.ElectionListAdapter
import com.example.android.politicalpreparedness.network.models.Election
import com.example.android.politicalpreparedness.representative.model.Representative
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


@BindingAdapter("date")
fun TextView.bindElectionDateText(date: Date?) {
    fun TextView.bindElectionDateText(date: Date?) {
        text = date?.let {
            val format = SimpleDateFormat("EEEE, MMM. dd, yyyy • HH:mm z", Locale.US)
            format.format(it)
        } ?: ""
    }
}