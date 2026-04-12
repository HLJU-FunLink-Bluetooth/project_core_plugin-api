package com.hlju.funlinkbluetooth.core.plugin.api.support

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import com.hlju.funlinkbluetooth.core.designsystem.token.Corners
import com.hlju.funlinkbluetooth.core.designsystem.token.Spacing
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.HorizontalDivider
import top.yukonga.miuix.kmp.basic.SmallTitle
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.shapes.SmoothRoundedCornerShape
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Composable
fun PluginPageLayout(
    title: String,
    eventLogs: SnapshotStateList<String>,
    emptyMessage: String,
    modifier: Modifier = Modifier,
    headerExtra: (@Composable () -> Unit)? = null,
    logContent: @Composable (msg: String) -> Unit = { msg -> DefaultLogLine(msg) },
    actions: @Composable () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(
                horizontal = Spacing.PageBase10,
                vertical = Spacing.Medium
            ),
        verticalArrangement = Arrangement.spacedBy(Spacing.Medium)
    ) {
        Card(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .animateContentSize()
                .clip(SmoothRoundedCornerShape(Corners.Outer)),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(
                        horizontal = Spacing.PageBase10,
                        vertical = Spacing.Medium
                    ),
                verticalArrangement = Arrangement.spacedBy(Spacing.Medium)
            ) {
                if (headerExtra != null) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        SmallTitle(text = title)
                        Spacer(modifier = Modifier.weight(1f))
                        headerExtra()
                    }
                } else {
                    SmallTitle(text = title)
                }
                HorizontalDivider(modifier = Modifier.fillMaxWidth())

                if (eventLogs.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = emptyMessage,
                            style = MiuixTheme.textStyles.body2,
                            color = MiuixTheme.colorScheme.onBackgroundVariant
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(Spacing.ExtraSmall),
                        contentPadding = PaddingValues(vertical = Spacing.ExtraSmall)
                    ) {
                        items(eventLogs) { msg ->
                            logContent(msg)
                        }
                    }
                }
            }
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .animateContentSize()
                .clip(SmoothRoundedCornerShape(Corners.Outer)),
        ) {
            Column(
                modifier = Modifier.padding(
                    horizontal = Spacing.PageBase10,
                    vertical = Spacing.Medium
                ),
                verticalArrangement = Arrangement.spacedBy(Spacing.Medium)
            ) {
                actions()
            }
        }
    }
}

@Composable
fun DefaultLogLine(msg: String) {
    val eventLog = LogHelper.parseEventLog(msg)
    Text(
        text = eventLog.content,
        style = MiuixTheme.textStyles.body2,
        color = when (eventLog.type) {
            EventLogType.SYSTEM -> MiuixTheme.colorScheme.onBackgroundVariant
            else -> MiuixTheme.colorScheme.onSurface
        }
    )
}
