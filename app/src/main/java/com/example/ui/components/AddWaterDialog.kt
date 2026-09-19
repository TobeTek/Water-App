package com.example.ui.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalDrink
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun AddWaterDialog(
    onDismiss: () -> Unit,
    onConfirm: (amountMl: Int, drinkType: String, note: String) -> Unit,
    initialAmount: Int = 250
) {
    var amountMl by remember { mutableFloatStateOf(initialAmount.toFloat()) }
    var customText by remember { mutableStateOf(initialAmount.toString()) }
    var selectedDrinkType by remember { mutableStateOf("Water") }
    var noteText by remember { mutableStateOf("") }

    val drinkTypes = listOf(
        "Water",
        "Electrolyte",
        "Lemon Water",
        "Tea",
        "Coconut Water",
        "Juice",
        "Coffee"
    )

    val volumePresets = listOf(150, 250, 350, 500, 750, 1000)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.LocalDrink,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Log Hydration",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                // Large volume readout
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Text(
                        text = "${amountMl.toInt()}",
                        style = MaterialTheme.typography.displaySmall.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "ml",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Slider
                Slider(
                    value = amountMl,
                    onValueChange = {
                        val rounded = (it / 10).toInt() * 10f
                        amountMl = rounded
                        customText = rounded.toInt().toString()
                    },
                    valueRange = 50f..1500f,
                    steps = 28,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("volume_slider")
                )

                // Quick preset chips
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    volumePresets.forEach { preset ->
                        val isSelected = amountMl.toInt() == preset
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                amountMl = preset.toFloat()
                                customText = preset.toString()
                            },
                            label = { Text("${preset}ml") },
                            modifier = Modifier.testTag("preset_chip_$preset")
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Beverage Type",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold)
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    drinkTypes.forEach { type ->
                        val isSelected = selectedDrinkType == type
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedDrinkType = type },
                            label = { Text(type) },
                            leadingIcon = if (type == "Water") {
                                {
                                    Icon(
                                        imageVector = Icons.Default.WaterDrop,
                                        contentDescription = null,
                                        modifier = Modifier.padding(2.dp)
                                    )
                                }
                            } else null,
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = noteText,
                    onValueChange = { noteText = it },
                    label = { Text("Note (optional)") },
                    placeholder = { Text("e.g., Post workout, Morning refill") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val finalAmount = amountMl.toInt().coerceAtLeast(10)
                    onConfirm(finalAmount, selectedDrinkType, noteText)
                },
                modifier = Modifier.testTag("confirm_add_water_button")
            ) {
                Text("Add Entry")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("cancel_add_water_button")
            ) {
                Text("Cancel")
            }
        },
        shape = RoundedCornerShape(24.dp)
    )
}
