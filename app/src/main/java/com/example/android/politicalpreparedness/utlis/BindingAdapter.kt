package com.example.android.politicalpreparedness.utlis

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.databinding.BindingAdapter
import com.example.android.politicalpreparedness.R
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


@BindingAdapter("date")
fun TextView.bindElectionDateText(date: Date?) {
    text = date?.let {
        val format = SimpleDateFormat("EEEE, MMM. dd, yyyy • HH:mm z", Locale.US)
        format.format(it)
    } ?: ""
}


@BindingAdapter("isVisible")
fun View.bindIsVisible(visible: Boolean? = true) {
    if (tag == null) {
        tag = true
        visibility = if (visible == true) View.VISIBLE else View.GONE
    } else {
        handleVisibilityAnimation(visible)
    }
}

fun View.handleVisibilityAnimation(visible: Boolean?) {
    animate().cancel()
    when {
        visible == true && visibility == View.GONE -> fadeInAndVisible()
        visible == false && visibility == View.VISIBLE -> fadeOutAndGone()
    }

}

private fun View.fadeInAndVisible() {
    this.visibility = View.VISIBLE
    this.alpha = 0f
    this.animate().alpha(1f).setListener(object : AnimatorListenerAdapter() {
        override fun onAnimationEnd(animation: Animator) {
            this@fadeInAndVisible.alpha = 1f
        }
    })
}

private fun View.fadeOutAndGone() {
    this.animate().alpha(0f).setListener(object : AnimatorListenerAdapter() {
        override fun onAnimationEnd(animation: Animator) {
            this@fadeOutAndGone.alpha = 1f
            this@fadeOutAndGone.visibility = View.GONE
        }
    })
}

@BindingAdapter("isFollowing")
fun Button.bindIsFollowing(isFollow: Boolean) {
    text = if (isFollow) {
        context.getString(R.string.unfollow)
    } else {
        context.getString(R.string.follow)
    }
}

@BindingAdapter("contentVisibility")
fun View.bindContentVisibility(content: String?) {
    visibility = if (content.isNullOrEmpty()) {
        View.GONE
    } else {
        View.VISIBLE
    }
}


