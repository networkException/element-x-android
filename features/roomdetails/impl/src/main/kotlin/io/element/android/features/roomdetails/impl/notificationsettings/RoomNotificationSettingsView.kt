/*
 * Copyright (c) 2023 New Vector Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.element.android.features.roomdetails.impl.notificationsettings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import io.element.android.features.roomdetails.impl.R
import io.element.android.libraries.architecture.Async
import io.element.android.libraries.core.bool.orTrue
import io.element.android.libraries.designsystem.components.ProgressDialog
import io.element.android.libraries.designsystem.components.button.BackButton
import io.element.android.libraries.designsystem.components.dialogs.ErrorDialog
import io.element.android.libraries.designsystem.components.preferences.PreferenceCategory
import io.element.android.libraries.designsystem.components.preferences.PreferenceSwitch
import io.element.android.libraries.designsystem.components.preferences.PreferenceText
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.theme.aliasScreenTitle
import io.element.android.libraries.designsystem.theme.components.Scaffold
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.libraries.designsystem.theme.components.TopAppBar
import io.element.android.libraries.matrix.api.room.RoomNotificationMode
import io.element.android.libraries.theme.ElementTheme
import io.element.android.libraries.ui.strings.CommonStrings

@Composable
fun RoomNotificationSettingsView(
    state: RoomNotificationSettingsState,
    modifier: Modifier = Modifier,
    onBackPressed: () -> Unit = {},
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            RoomNotificationSettingsTopBar(
                onBackPressed = { onBackPressed() }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(padding)
                .consumeWindowInsets(padding),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            val subtitle = when (state.defaultRoomNotificationMode) {
                RoomNotificationMode.ALL_MESSAGES -> stringResource(id = R.string.screen_room_notification_settings_mode_all_messages)
                RoomNotificationMode.MENTIONS_AND_KEYWORDS_ONLY -> stringResource(id = R.string.screen_room_notification_settings_mode_mentions_and_keywords)
                RoomNotificationMode.MUTE -> stringResource(id = CommonStrings.common_mute)
                null -> ""
            }

            val roomNotificationSettings = state.roomNotificationSettings.dataOrNull()

            PreferenceCategory(title = stringResource(id = R.string.screen_room_notification_settings_custom_settings_title)) {
                PreferenceSwitch(
                    isChecked = state.displayIsDefault.orTrue(),
                    onCheckedChange = {
                        state.eventSink(RoomNotificationSettingsEvents.SetNotificationMode(it))
                    },
                    title = "Match default setting",
                    subtitle = subtitle,
                    enabled = roomNotificationSettings != null
                )

                PreferenceText(
                    title = stringResource(id = R.string.screen_room_notification_settings_allow_custom),
                    subtitle = stringResource(id = R.string.screen_room_notification_settings_allow_custom_footnote),
                    enabled = !state.displayIsDefault.orTrue(),
                )

                if (roomNotificationSettings != null &&  state.displayNotificationMode != null) {
                    RoomNotificationSettingsOptions(
                        selected = state.displayNotificationMode,
                        enabled = !state.displayIsDefault.orTrue(),
                        onOptionSelected = {
                            state.eventSink(RoomNotificationSettingsEvents.RoomNotificationModeChanged(it.mode))
                        },
                    )
                }
            }

            when (state.setNotificationSettingAction) {
                is Async.Loading -> {
                    ProgressDialog()
                }
                is Async.Failure -> {
                    ShowChangeNotificationSettingError(state, RoomNotificationSettingsEvents.ClearSetNotificationError)
                }
                else -> Unit
            }

            when (state.restoreDefaultAction) {
                is Async.Loading -> {
                    ProgressDialog()
                }
                is Async.Failure -> {
                    ShowChangeNotificationSettingError(state, RoomNotificationSettingsEvents.ClearRestoreDefaultError)
                }
                else -> Unit
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RoomNotificationSettingsTopBar(
    modifier: Modifier = Modifier,
    onBackPressed: () -> Unit = {},
) {
    TopAppBar(
        modifier = modifier,
        title = {
            Text(
                text = stringResource(R.string.screen_room_details_notification_title),
                style = ElementTheme.typography.aliasScreenTitle,
            )
        },
        navigationIcon = { BackButton(onClick = onBackPressed) },
    )
}

@Composable
fun RoomNotificationSettingsOptions(
    selected: RoomNotificationMode?,
    enabled: Boolean,
    modifier: Modifier = Modifier,
    onOptionSelected: (RoomNotificationSettingsItem) -> Unit = {},
) {
    val items = roomNotificationSettingsItems()
    Column(modifier = modifier.selectableGroup()) {
        items.forEach { item ->
            RoomNotificationSettingsOption(
                roomNotificationSettingsItem = item,
                isSelected = selected == item.mode,
                onOptionSelected = onOptionSelected,
                enabled = enabled
            )
        }
    }
}

@Composable
fun ShowChangeNotificationSettingError(state: RoomNotificationSettingsState, event: RoomNotificationSettingsEvents) {
    ErrorDialog(
        title = stringResource(CommonStrings.dialog_title_error),
        content = stringResource(CommonStrings.screen_notification_settings_edit_failed_updating_default_mode),
        onDismiss = { state.eventSink(event) },
    )
}

@PreviewsDayNight
@Composable
internal fun RoomNotificationSettingsPreview(
    @PreviewParameter(RoomNotificationSettingsStateProvider::class) state: RoomNotificationSettingsState
) = ElementPreview {
    RoomNotificationSettingsView(state)
}
