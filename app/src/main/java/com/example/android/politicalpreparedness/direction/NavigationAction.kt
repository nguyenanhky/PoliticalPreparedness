package com.example.android.politicalpreparedness.direction

import androidx.navigation.NavDirections

sealed class NavigationAction {
    data class Open(val directions: NavDirections) : NavigationAction()
    object Back : NavigationAction()
    data class BackTo(val destinationId: Int) : NavigationAction()
}