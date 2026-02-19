package org.sammomanyi.mediaccess.features.queue.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import org.sammomanyi.mediaccess.features.pharmacy.domain.model.PrescriptionItem

data class PrescriptionItemDraft(
    val medicationName: String = "",
    val dosage: String = "",
    val frequency: String = "",
    val duration: String = "",
    val quantity: String = "1"
)

@Composable
fun PrescriptionDialog(
    patientName: String,
    onDismiss: () -> Unit,
    onSubmit: (List<PrescriptionItem>, String) -> Unit
) {
    var medications by remember { mutableStateOf(listOf(PrescriptionItemDraft())) }
    var notes by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier.width(600.dp).heightIn(max = 700.dp),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text(
                    "Create Prescription",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "Patient: $patientName",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(Modifier.height(16.dp))

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    itemsIndexed(medications) { index, item ->
                        MedicationItemCard(
                            item = item,
                            onUpdate = { updated ->
                                medications = medications.toMutableList().apply { set(index, updated) }
                            },
                            onRemove = {
                                medications = medications.toMutableList().apply { removeAt(index) }
                            },
                            canRemove = medications.size > 1
                        )
                    }
                }

                Spacer(Modifier.height(8.dp))

                OutlinedButton(
                    onClick = { medications = medications + PrescriptionItemDraft() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Add Medication")
                }

                Spacer(Modifier.height(16.dp))

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Additional Notes") },
                    placeholder = { Text("Special instructions...") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3,
                    shape = RoundedCornerShape(8.dp)
                )

                Spacer(Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancel")
                    }
                    Button(
                        onClick = {
                            val valid = medications.all {
                                it.medicationName.isNotBlank() &&
                                        it.dosage.isNotBlank() &&
                                        it.frequency.isNotBlank() &&
                                        it.duration.isNotBlank() &&
                                        it.quantity.toIntOrNull() != null
                            }
                            if (valid) {
                                onSubmit(
                                    medications.map {
                                        PrescriptionItem(
                                            medicationName = it.medicationName,
                                            dosage = it.dosage,
                                            frequency = it.frequency,
                                            duration = it.duration,
                                            quantity = it.quantity.toInt()
                                        )
                                    },
                                    notes
                                )
                            }
                        },
                        modifier = Modifier.weight(1f),
                        enabled = medications.all {
                            it.medicationName.isNotBlank() &&
                                    it.dosage.isNotBlank() &&
                                    it.frequency.isNotBlank() &&
                                    it.duration.isNotBlank()
                        }
                    ) {
                        Text("Create & Send to Pharmacy")
                    }
                }
            }
        }
    }
}

@Composable
private fun MedicationItemCard(
    item: PrescriptionItemDraft,
    onUpdate: (PrescriptionItemDraft) -> Unit,
    onRemove: () -> Unit,
    canRemove: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Medication ${if (item.medicationName.isBlank()) "" else "- ${item.medicationName}"}",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )
                if (canRemove) {
                    IconButton(onClick = onRemove, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Remove", modifier = Modifier.size(18.dp))
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            OutlinedTextField(
                value = item.medicationName,
                onValueChange = { onUpdate(item.copy(medicationName = it)) },
                label = { Text("Name") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(8.dp)
            )

            Spacer(Modifier.height(8.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = item.dosage,
                    onValueChange = { onUpdate(item.copy(dosage = it)) },
                    label = { Text("Dosage") },
                    placeholder = { Text("e.g., 500mg") },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    shape = RoundedCornerShape(8.dp)
                )
                OutlinedTextField(
                    value = item.frequency,
                    onValueChange = { onUpdate(item.copy(frequency = it)) },
                    label = { Text("Frequency") },
                    placeholder = { Text("e.g., 2x daily") },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    shape = RoundedCornerShape(8.dp)
                )
            }

            Spacer(Modifier.height(8.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = item.duration,
                    onValueChange = { onUpdate(item.copy(duration = it)) },
                    label = { Text("Duration") },
                    placeholder = { Text("e.g., 7 days") },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    shape = RoundedCornerShape(8.dp)
                )
                OutlinedTextField(
                    value = item.quantity,
                    onValueChange = { if (it.all { c -> c.isDigit() }) onUpdate(item.copy(quantity = it)) },
                    label = { Text("Quantity") },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    shape = RoundedCornerShape(8.dp)
                )
            }
        }
    }
}