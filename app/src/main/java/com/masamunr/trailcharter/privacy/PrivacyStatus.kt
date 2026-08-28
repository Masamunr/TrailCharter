package com.masamunr.trailcharter.privacy

data class PrivacyStatus(
    val locationTrackingEnabled: Boolean,
    val networkState: NetworkState,
    val cloudBackupEnabled: Boolean,
) {
    companion object {
        val FoundationDefault = PrivacyStatus(
            locationTrackingEnabled = false,
            networkState = NetworkState.Disabled,
            cloudBackupEnabled = false,
        )
    }
}

enum class NetworkState {
    Disabled,
    Idle,
    Active,
}
