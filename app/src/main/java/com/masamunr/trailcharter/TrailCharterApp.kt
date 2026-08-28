package com.masamunr.trailcharter

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.weight
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.masamunr.trailcharter.privacy.NetworkState
import com.masamunr.trailcharter.privacy.PrivacyStatus

@Composable
fun TrailCharterApp(
    privacyStatus: PrivacyStatus = PrivacyStatus.FoundationDefault,
) {
    Surface(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .padding(horizontal = 24.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Text(
                text = stringResource(R.string.app_name),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = stringResource(R.string.foundation_build),
                style = MaterialTheme.typography.titleMedium,
            )
            Text(
                text = stringResource(R.string.foundation_ui_note),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            HorizontalDivider()

            Text(
                text = stringResource(R.string.privacy_status),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            StatusRow(
                label = stringResource(R.string.location_tracking),
                value = if (privacyStatus.locationTrackingEnabled) "On" else stringResource(R.string.status_off),
            )
            StatusRow(
                label = stringResource(R.string.internet),
                value = when (privacyStatus.networkState) {
                    NetworkState.Disabled -> stringResource(R.string.internet_disabled_foundation)
                    NetworkState.Idle -> "Idle"
                    NetworkState.Active -> "Active"
                },
            )
            StatusRow(
                label = stringResource(R.string.cloud_backup),
                value = if (privacyStatus.cloudBackupEnabled) "On" else stringResource(R.string.status_off),
            )

            Spacer(modifier = Modifier.weight(1f))

            Text(
                text = stringResource(R.string.foundation_footer),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun StatusRow(
    label: String,
    value: String,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyLarge)
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium,
        )
    }
}
