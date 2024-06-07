package com.yangdai.opennote.presentation.glance

import android.content.Context
import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.ImageProvider
import androidx.glance.LocalContext
import androidx.glance.LocalSize
import androidx.glance.action.ActionParameters
import androidx.glance.action.actionParametersOf
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.components.CircleIconButton
import androidx.glance.appwidget.components.Scaffold
import androidx.glance.appwidget.components.SquareIconButton
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.lazy.LazyColumn
import androidx.glance.appwidget.lazy.items
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.text.Text
import androidx.glance.text.TextAlign
import androidx.glance.text.TextStyle
import com.yangdai.opennote.MainActivity
import com.yangdai.opennote.R
import com.yangdai.opennote.data.di.AppModule
import com.yangdai.opennote.data.local.entity.NoteEntity
import com.yangdai.opennote.data.repository.NoteRepositoryImpl
import com.yangdai.opennote.presentation.util.Constants.LINK
import com.yangdai.opennote.presentation.util.sendPendingIntent

class NoteListWidget : GlanceAppWidget() {

    override val sizeMode = SizeMode.Exact

    override suspend fun provideGlance(context: Context, id: GlanceId) {

        val database = AppModule.provideNoteDatabase(context.applicationContext)
        val noteRepository = NoteRepositoryImpl(database.noteDao)

        provideContent {

            val notes by noteRepository.getAllNotes().collectAsState(initial = emptyList())

            GlanceTheme {
                Content(notes.take(25))
            }
        }
    }

    @Composable
    private fun Content(notes: List<NoteEntity> = emptyList()) {

        val size = LocalSize.current

        Scaffold(
            titleBar = {
                Row(
                    modifier = GlanceModifier.fillMaxWidth()
                        .padding(bottom = 8.dp, top = if (size.width > 250.dp) 16.dp else 8.dp)
                        .padding(horizontal = if (size.width > 250.dp) 16.dp else 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    Text(
                        text = "Open Note",
                        style = TextStyle(
                            color = GlanceTheme.colors.onSurface,
                            fontSize = 18.sp
                        )
                    )

                    if (size.width > 250.dp) {
                        CircleIconButton(
                            imageProvider = ImageProvider(R.drawable.sync),
                            contentDescription = "Sync",
                            backgroundColor = GlanceTheme.colors.widgetBackground,
                            onClick = actionRunCallback<RefreshAction>()
                        )
                    }

                    Spacer(modifier = GlanceModifier.defaultWeight())

                    SquareIconButton(
                        modifier = GlanceModifier.size(48.dp),
                        imageProvider = ImageProvider(R.drawable.add),
                        contentDescription = "Add",
                        onClick = actionRunCallback<NoteAction>(
                            parameters = actionParametersOf(destinationKey to "-1")
                        )
                    )
                }
            },
            horizontalPadding = if (size.width > 250.dp) 16.dp else 2.dp
        ) {
            LazyColumn {
                items(
                    items = notes,
                    itemId = { it.id!! }
                ) {
                    Column {
                        Column(
                            modifier = GlanceModifier
                                .fillMaxWidth()
                                .padding(horizontal = 10.dp, vertical = 2.dp)
                                .background(GlanceTheme.colors.secondaryContainer)
                                .appWidgetInnerCornerRadius()
                                .padding(4.dp)
                                .clickable(
                                    actionRunCallback<NoteAction>(
                                        parameters = actionParametersOf(destinationKey to it.id.toString())
                                    )
                                )
                        ) {
                            Text(
                                modifier = GlanceModifier.fillMaxWidth(),
                                text = it.title,
                                style = TextStyle(
                                    color = GlanceTheme.colors.onSurface,
                                    fontSize = 14.sp
                                ),
                                maxLines = 1
                            )
                            Text(
                                modifier = GlanceModifier.fillMaxWidth(),
                                text = it.content,
                                style = TextStyle(
                                    color = GlanceTheme.colors.onSurfaceVariant,
                                    fontSize = 12.sp
                                ),
                                maxLines = 2
                            )
                        }
                        Spacer(modifier = GlanceModifier.height(4.dp))
                    }
                }

                item {
                    Text(
                        modifier = GlanceModifier.fillMaxWidth()
                            .padding(top = 16.dp, bottom = 24.dp)
                            .clickable(actionStartActivity<MainActivity>()),
                        text = LocalContext.current.getString(R.string.view_all_notes),
                        style = TextStyle(
                            color = GlanceTheme.colors.primary,
                            textAlign = TextAlign.Center,
                            fontSize = 14.sp
                        ),
                    )
                }
            }
        }
    }
}


private val destinationKey = ActionParameters.Key<String>("KEY_DESTINATION")

class NoteAction : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        context.sendPendingIntent("$LINK/note/${parameters[destinationKey]}")
    }
}

class RefreshAction : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        NoteListWidget().update(context, glanceId)
    }
}

fun GlanceModifier.appWidgetInnerCornerRadius(): GlanceModifier {
    if (Build.VERSION.SDK_INT >= 31) {
        return cornerRadius(android.R.dimen.system_app_widget_inner_radius)
    }
    return cornerRadius(8.dp)
}
