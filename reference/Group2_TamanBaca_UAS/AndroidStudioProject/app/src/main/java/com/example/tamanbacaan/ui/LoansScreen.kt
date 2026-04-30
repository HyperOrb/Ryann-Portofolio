package com.example.tamanbacaan.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.AssignmentReturn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.tamanbacaan.components.DangerButton
import com.example.tamanbacaan.viewmodel.LoansViewModel
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.ceil

@Composable
fun LoansScreen(vm: LoansViewModel = viewModel()) {
    val activeLoans by vm.activeLoans.collectAsState()
    val historyLoans by vm.historyLoans.collectAsState()
    val isLoading by vm.isLoading.collectAsState()

    val greenPrimary = Color(0xFF4CAF50)
    var selectedTab by remember { mutableStateOf("Aktivitas") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Pinjaman Buku",
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
            color = greenPrimary
        )
        Spacer(Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            PlainTab(title = "Aktivitas", selected = selectedTab == "Aktivitas", accent = greenPrimary) { selectedTab = "Aktivitas" }
            PlainTab(title = "Histori", selected = selectedTab == "Histori", accent = greenPrimary) { selectedTab = "Histori" }
        }

        Spacer(Modifier.height(16.dp))

        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            if (selectedTab == "Aktivitas") {
                LoanList(loans = activeLoans, highlightColor = greenPrimary, onReturn = { borrowId, bookId -> vm.returnBook(borrowId, bookId) })
            } else {
                LoanList(loans = historyLoans, highlightColor = greenPrimary, onReturn = null)
            }
        }
    }
}

@Composable
private fun PlainTab(
    title: String,
    selected: Boolean,
    accent: Color,
    onClick: () -> Unit
) {
    val interaction = remember { MutableInteractionSource() }
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.wrapContentWidth().clickable(interactionSource = interaction, indication = null) { onClick() }
    ) {
        Text(
            text = title,
            style = if (selected) MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold) else MaterialTheme.typography.titleSmall,
            color = if (selected) accent else MaterialTheme.colorScheme.onSurface
        )
        Spacer(Modifier.height(4.dp))
        Box(modifier = Modifier.height(2.dp).width(if (selected) 36.dp else 0.dp).background(if (selected) accent else Color.Transparent))
    }
}

@Composable
private fun LoanList(
    loans: List<com.example.tamanbacaan.viewmodel.BorrowedBookDetails>,
    highlightColor: Color,
    onReturn: ((borrowId: String, bookId: String) -> Unit)?
) {
    if (loans.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize().padding(top = 32.dp), contentAlignment = Alignment.TopCenter) {
            Text("Belum ada data pinjaman.", color = Color.Gray, style = MaterialTheme.typography.bodyMedium)
        }
    } else {
        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(loans, key = { it.borrow.id }) { details ->
                LoanCard(details = details, highlightColor = highlightColor, onReturn = onReturn)
            }
        }
    }
}

@Composable
private fun LoanCard(
    details: com.example.tamanbacaan.viewmodel.BorrowedBookDetails,
    highlightColor: Color,
    onReturn: ((borrowId: String, bookId: String) -> Unit)?
) {
    val book = details.book
    val borrow = details.borrow

    val now = System.currentTimeMillis()
    val daysLeft = ceil((borrow.dueDate - now).toDouble() / (1000 * 60 * 60 * 24)).toInt()
    val formattedDueDate = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(borrow.dueDate))

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = book.coverUrl,
                contentDescription = book.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.size(90.dp).clip(RoundedCornerShape(8.dp))
            )
            Spacer(Modifier.width(16.dp))
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = book.title,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = highlightColor
                )
                if (borrow.returned) {
                    Text("Telah dikembalikan", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                } else {
                    Text(
                        text = "Jatuh tempo: $formattedDueDate",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.DarkGray
                    )
                    Text(
                        text = when {
                            daysLeft > 1 -> "Sisa $daysLeft hari"
                            daysLeft == 1 -> "Sisa 1 hari"
                            daysLeft == 0 -> "Jatuh tempo hari ini"
                            else -> "Terlambat ${-daysLeft} hari"
                        },
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                        color = if (daysLeft <= 2) MaterialTheme.colorScheme.error else highlightColor
                    )
                }
                onReturn?.let {
                    Spacer(Modifier.height(8.dp))
                    DangerButton(
                        text = "Kembalikan",
                        onClick = { onReturn(borrow.id, book.id) },
                        icon = Icons.AutoMirrored.Filled.AssignmentReturn,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}
