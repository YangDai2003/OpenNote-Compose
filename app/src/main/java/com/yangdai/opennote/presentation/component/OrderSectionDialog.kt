package com.yangdai.opennote.presentation.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.yangdai.opennote.R
import com.yangdai.opennote.domain.usecase.NoteOrder
import com.yangdai.opennote.domain.usecase.OrderType

@Composable
fun OrderSectionDialog(
    noteOrder: NoteOrder = NoteOrder.Date(OrderType.Descending),
    onOrderChange: (NoteOrder) -> Unit,
    onDismiss: () -> Unit
) {

    var newOrder by remember { mutableStateOf(noteOrder) }

    val typeOptions = listOf(
        stringResource(R.string.title),
        stringResource(R.string.date)
    )

    val orderOptions = listOf(
        stringResource(R.string.ascending),
        stringResource(R.string.descending)
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = stringResource(R.string.sort_by)) },
        text = {
            Column {
                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                    SegmentedButton(
                        shape = SegmentedButtonDefaults.itemShape(
                            index = 0,
                            count = typeOptions.size
                        ),
                        onClick = { newOrder = NoteOrder.Title(noteOrder.orderType) },
                        selected = newOrder is NoteOrder.Title
                    ) {
                        Text(typeOptions[0])
                    }
                    SegmentedButton(
                        shape = SegmentedButtonDefaults.itemShape(
                            index = 1,
                            count = typeOptions.size
                        ),
                        onClick = { newOrder = NoteOrder.Date(noteOrder.orderType) },
                        selected = newOrder is NoteOrder.Date
                    ) {
                        Text(typeOptions[1])
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                    SegmentedButton(
                        shape = SegmentedButtonDefaults.itemShape(
                            index = 0,
                            count = typeOptions.size
                        ),
                        onClick = { newOrder = newOrder.copy(OrderType.Ascending) },
                        selected = newOrder.orderType is OrderType.Ascending
                    ) {
                        Text(orderOptions[0])
                    }
                    SegmentedButton(
                        shape = SegmentedButtonDefaults.itemShape(
                            index = 1,
                            count = typeOptions.size
                        ),
                        onClick = { newOrder = newOrder.copy(OrderType.Descending) },
                        selected = newOrder.orderType is OrderType.Descending
                    ) {
                        Text(orderOptions[1])
                    }
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(id = android.R.string.cancel))
            }
        },
        confirmButton = {
            TextButton(onClick = {
                onDismiss()
                onOrderChange(newOrder)
            }) {
                Text(stringResource(id = android.R.string.ok))
            }
        }
    )
}

@Preview
@Composable
fun OrderSectionDialogPreview() {
    OrderSectionDialog(
        noteOrder = NoteOrder.Date(OrderType.Descending),
        onOrderChange = {},
        onDismiss = {})
}