package com.yangdai.opennote.presentation.glance

import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
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
import androidx.glance.color.ColorProvider
import androidx.glance.color.DynamicThemeColorProviders
import androidx.glance.color.colorProviders
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
import com.yangdai.opennote.presentation.util.Constants
import com.yangdai.opennote.presentation.util.Constants.LINK
import com.yangdai.opennote.presentation.util.extension.properties.Properties.splitPropertiesAndContent

private val transparentColorProviders = colorProviders(
    primary = ColorProvider(Color.Transparent, Color.Transparent),
    onPrimary = DynamicThemeColorProviders.onPrimary,
    primaryContainer = ColorProvider(Color.Transparent, Color.Transparent),
    onPrimaryContainer = DynamicThemeColorProviders.onPrimaryContainer,
    secondary = ColorProvider(Color.Transparent, Color.Transparent),
    onSecondary = DynamicThemeColorProviders.onSecondary,
    secondaryContainer = ColorProvider(Color.Transparent, Color.Transparent),
    onSecondaryContainer = DynamicThemeColorProviders.onSecondaryContainer,
    tertiary = ColorProvider(Color.Transparent, Color.Transparent),
    onTertiary = DynamicThemeColorProviders.onTertiary,
    tertiaryContainer = ColorProvider(Color.Transparent, Color.Transparent),
    onTertiaryContainer = DynamicThemeColorProviders.onTertiaryContainer,
    error = ColorProvider(Color.Transparent, Color.Transparent),
    errorContainer = ColorProvider(Color.Transparent, Color.Transparent),
    onError = DynamicThemeColorProviders.onError,
    onErrorContainer = DynamicThemeColorProviders.onErrorContainer,
    background = ColorProvider(Color.Transparent, Color.Transparent),
    onBackground = DynamicThemeColorProviders.onBackground,
    surface = ColorProvider(Color.Transparent, Color.Transparent),
    onSurface = DynamicThemeColorProviders.onSurface,
    surfaceVariant = ColorProvider(Color.Transparent, Color.Transparent),
    onSurfaceVariant = DynamicThemeColorProviders.onSurfaceVariant,
    outline = ColorProvider(Color.Transparent, Color.Transparent),
    inverseOnSurface = ColorProvider(Color.Transparent, Color.Transparent),
    inverseSurface = ColorProvider(Color.Transparent, Color.Transparent),
    inversePrimary = ColorProvider(Color.Transparent, Color.Transparent),
    widgetBackground = ColorProvider(Color.Transparent, Color.Transparent)
)

class NoteListWidget : GlanceAppWidget() {

    override val sizeMode = SizeMode.Exact

    override suspend fun provideGlance(context: Context, id: GlanceId) {

        val database = AppModule.provideNoteDatabase(context.applicationContext)
        val noteRepository = AppModule.provideNoteRepository(database)
        val widgetPreferences =
            AppModule.provideWidgetDataStoreRepository(context.applicationContext)

        provideContent {

            val notes by noteRepository.getAllNotes().collectAsState(initial = emptyList())
            val textLines by widgetPreferences.intFlow(Constants.Widget.WIDGET_TEXT_LINES)
                .collectAsState(initial = 1)
            val textSize by widgetPreferences.intFlow(Constants.Widget.WIDGET_TEXT_SIZE)
                .collectAsState(initial = 1)
            val backgroundColor by widgetPreferences.intFlow(Constants.Widget.WIDGET_BACKGROUND_COLOR)
                .collectAsState(initial = 1)

            GlanceTheme(
                colors = if (backgroundColor == 1) GlanceTheme.colors else transparentColorProviders
            ) {
                Content(
                    textLines = textLines,
                    textSize = textSize,
                    backgroundColor = backgroundColor,
                    notes = notes.take(25)
                )
            }
        }
    }

    @Composable
    private fun Content(
        textLines: Int = 1,
        textSize: Int = 1,
        backgroundColor: Int = 1,
        notes: List<NoteEntity> = emptyList()
    ) {

        val titleSize by remember(textSize) {
            mutableStateOf(
                when (textSize) {
                    0 -> 14.sp
                    1 -> 16.sp
                    2 -> 18.sp
                    else -> 16.sp
                }
            )
        }

        val contentSize by remember(textSize) {
            mutableStateOf(
                when (textSize) {
                    0 -> 12.sp
                    1 -> 14.sp
                    2 -> 16.sp
                    else -> 14.sp
                }
            )
        }

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
                            backgroundColor = if (backgroundColor == 1) GlanceTheme.colors.widgetBackground
                            else null,
                            onClick = actionRunCallback<RefreshAction>()
                        )
                    }

                    Spacer(modifier = GlanceModifier.defaultWeight())

                    if (backgroundColor == 0)
                        CircleIconButton(
                            modifier = GlanceModifier.size(48.dp),
                            imageProvider = ImageProvider(R.drawable.add),
                            contentDescription = "Add",
                            backgroundColor = null,
                            contentColor = GlanceTheme.colors.onSurface,
                            onClick = actionRunCallback<NoteAction>(
                                parameters = actionParametersOf(destinationKey to -1L)
                            )
                        )
                    else
                        SquareIconButton(
                            modifier = GlanceModifier.size(48.dp),
                            imageProvider = ImageProvider(R.drawable.add),
                            contentDescription = "Add",
                            onClick = actionRunCallback<NoteAction>(
                                parameters = actionParametersOf(destinationKey to -1L)
                            )
                        )
                }
            },
            horizontalPadding = if (size.width > 250.dp) 16.dp else 4.dp
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
                                        parameters = actionParametersOf(destinationKey to it.id!!)
                                    )
                                )
                        ) {
                            if (it.title.isNotEmpty())
                                Text(
                                    modifier = GlanceModifier.fillMaxWidth(),
                                    text = it.title,
                                    style = TextStyle(
                                        color = GlanceTheme.colors.onSurface,
                                        fontSize = titleSize
                                    ),
                                    maxLines = 1
                                )
                            Text(
                                modifier = GlanceModifier.fillMaxWidth(),
                                text = it.content.splitPropertiesAndContent().second,
                                style = TextStyle(
                                    color = GlanceTheme.colors.onSurfaceVariant,
                                    fontSize = contentSize
                                ),
                                maxLines = textLines
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


private val destinationKey = ActionParameters.Key<Long>(Constants.KEY_DESTINATION)

class NoteAction : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        val noteId = parameters[destinationKey]
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or      // 在新任务中启动
                    Intent.FLAG_ACTIVITY_CLEAR_TASK       // 清除所有已存在的任务
            data = "$LINK/note/$noteId".toUri()
        }
        context.startActivity(intent)
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
