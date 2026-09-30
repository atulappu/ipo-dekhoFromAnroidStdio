package com.example.ipotracker.presentation.allotment

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ipotracker.presentation.detail.DetailKeyValueRow
import com.example.ipotracker.utils.DateUtils
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AllotmentScreen(
    viewModel: AllotmentViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var expandedIpoMenu by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("IPO Allotment Status", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            color = SecondaryBlueLight,
                            shape = RoundedCornerShape(4.dp),
                            border = BorderStroke(1.dp, SecondaryBlueContainer)
                        ) {
                            Text(
                                text = "CHECKER",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                fontWeight = FontWeight.Bold,
                                color = SecondaryBlue,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack, modifier = Modifier.size(36.dp)) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", modifier = Modifier.size(20.dp))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Select IPO
            item {
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, BorderLight),
                    shadowElevation = 0.5.dp
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "SELECT IPO",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.5.sp),
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        Box {
                            OutlinedButton(
                                onClick = { expandedIpoMenu = true },
                                modifier = Modifier.fillMaxWidth().height(42.dp),
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(1.dp, BorderLight),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = uiState.selectedIpo?.name ?: "Select an IPO",
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 12.5.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = SecondaryBlue)
                                }
                            }

                            DropdownMenu(
                                expanded = expandedIpoMenu,
                                onDismissRequest = { expandedIpoMenu = false },
                                modifier = Modifier.fillMaxWidth(0.9f)
                            ) {
                                uiState.ipos.forEach { ipo ->
                                    DropdownMenuItem(
                                        text = {
                                            Column {
                                                Text(ipo.name, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                                                Text(
                                                    "Allotment: ${DateUtils.formatDisplayDate(ipo.allotmentDate)} • Registrar: ${ipo.registrar}",
                                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.5.sp),
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        },
                                        onClick = {
                                            viewModel.selectIpo(ipo)
                                            expandedIpoMenu = false
                                        }
                                    )
                                }
                            }
                        }

                        if (uiState.selectedIpo != null) {
                            Spacer(modifier = Modifier.height(10.dp))
                            DetailKeyValueRow("Assigned Registrar", uiState.registrarName, isAlt = false)
                            DetailKeyValueRow("Allotment Date", DateUtils.formatDisplayDate(uiState.selectedIpo!!.allotmentDate), isAlt = true)
                            DetailKeyValueRow("Status", if (uiState.isAllotmentOut) "Allotment Declared" else "Pending / In Progress", isAlt = false)
                        }
                    }
                }
            }

            // Application Details Input Form
            item {
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, BorderLight),
                    shadowElevation = 0.5.dp
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "APPLICANT CREDENTIALS",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.5.sp),
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = uiState.panNumber,
                            onValueChange = { viewModel.updatePan(it.uppercase()) },
                            label = { Text("PAN Number (e.g. ABCDE1234F)", fontSize = 11.5.sp) },
                            singleLine = true,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = PrimaryOrange,
                                unfocusedBorderColor = BorderLight
                            )
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = uiState.applicationNumber,
                            onValueChange = { viewModel.updateApplicationNumber(it) },
                            label = { Text("Application Number / DP ID (Optional)", fontSize = 11.5.sp) },
                            singleLine = true,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = PrimaryOrange,
                                unfocusedBorderColor = BorderLight
                            )
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Button(
                            onClick = {
                                val url = uiState.registrarUrl
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                                context.startActivity(intent)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = SecondaryBlue),
                            shape = RoundedCornerShape(8.dp),
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 1.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp)
                        ) {
                            Icon(Icons.Default.OpenInNew, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("CHECK ON ${uiState.registrarName.uppercase()} PORTAL", fontWeight = FontWeight.Bold, fontSize = 11.5.sp, color = Color.White)
                        }
                    }
                }
            }

            // Master Official RTAs / Registrars Directory (12 verified portals with Admin URL Update)
            item {
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, BorderLight),
                    shadowElevation = 0.5.dp
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "OFFICIAL REGISTRAR DIRECTORY (${uiState.registrars.size})",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                                    fontWeight = FontWeight.Bold,
                                    color = PrimaryOrange
                                )
                                Text(
                                    text = "Room SQLite Database • Live Synced",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                    color = NeutralGray
                                )
                            }
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Button(
                                    onClick = { viewModel.openAddRegistrarDialog() },
                                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryOrange),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                    shape = RoundedCornerShape(6.dp),
                                    modifier = Modifier.height(28.dp)
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = null, tint = Color.White, modifier = Modifier.size(13.dp))
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text("Add Registrar", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Search Bar inside Registrar Table (Matches user screenshot)
                        OutlinedTextField(
                            value = uiState.registrarSearchQuery,
                            onValueChange = { viewModel.onRegistrarSearchChange(it) },
                            placeholder = { Text("Search registrar, URL, or comments...", fontSize = 11.5.sp) },
                            leadingIcon = {
                                Icon(Icons.Default.Search, contentDescription = null, tint = NeutralGray, modifier = Modifier.size(16.dp))
                            },
                            trailingIcon = {
                                if (uiState.registrarSearchQuery.isNotEmpty()) {
                                    IconButton(
                                        onClick = { viewModel.onRegistrarSearchChange("") },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(Icons.Default.Clear, contentDescription = "Clear", modifier = Modifier.size(14.dp))
                                    }
                                }
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = PrimaryOrange,
                                unfocusedBorderColor = BorderLight
                            )
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Table Column Header (Matching Screenshot: Registrar | Issues Managed | Issue Amount)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFFF1F5F9), RoundedCornerShape(6.dp))
                                .padding(horizontal = 8.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Registrar & URL",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF334155),
                                modifier = Modifier.weight(1.8f)
                            )
                            Text(
                                text = "Issues",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF334155),
                                modifier = Modifier.weight(0.7f)
                            )
                            Text(
                                text = "Amount (Cr)",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF334155),
                                modifier = Modifier.weight(1.1f)
                            )
                            Spacer(modifier = Modifier.width(36.dp))
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        val filteredRegistrars = uiState.registrars.filter {
                            it.name.contains(uiState.registrarSearchQuery, ignoreCase = true) ||
                            it.url.contains(uiState.registrarSearchQuery, ignoreCase = true) ||
                            (it.comments?.contains(uiState.registrarSearchQuery, ignoreCase = true) == true)
                        }

                        filteredRegistrars.forEachIndexed { index, reg ->
                            RegistrarRowItem(
                                registrar = reg,
                                onOpenUrl = {
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(reg.url))
                                    context.startActivity(intent)
                                },
                                onEditUrl = {
                                    viewModel.openEditUrlDialog(reg)
                                }
                            )
                            if (index < filteredRegistrars.lastIndex) {
                                HorizontalDivider(color = BorderLight, thickness = 0.6.dp)
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        HorizontalDivider(color = BorderLight, thickness = 0.8.dp)
                        Spacer(modifier = Modifier.height(8.dp))

                        // Stock Exchanges Allotment Portals
                        Text(
                            text = "EXCHANGE BACKUP VERIFICATION",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        RegistrarQuickLink("BSE India Allotment Portal", "https://www.bseindia.com/investors/appli_check.aspx", context)
                        HorizontalDivider(color = BorderLight, thickness = 0.6.dp)
                        RegistrarQuickLink("NSE India Verification Portal", "https://www.nseindia.com/products/content/equities/ipos/ipo_login.htm", context)
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }

    // Admin URL & Registrar Details Update Dialog
    if (uiState.isEditUrlDialogOpen && uiState.editingRegistrar != null) {
        val reg = uiState.editingRegistrar!!
        var editedName by remember(reg) { mutableStateOf(reg.name) }
        var editedUrl by remember(reg) { mutableStateOf(reg.url) }
        var editedIssues by remember(reg) { mutableStateOf(reg.issuesManaged.toString()) }
        var editedAmount by remember(reg) { mutableStateOf(reg.issueAmountCr.toString()) }
        var editedComments by remember(reg) { mutableStateOf(reg.comments ?: "") }
        var editedModifiedBy by remember(reg) { mutableStateOf(reg.modifiedBy) }

        AlertDialog(
            onDismissRequest = { viewModel.closeEditUrlDialog() },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Edit, contentDescription = null, tint = PrimaryOrange, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Edit Registrar Details (Admin)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = editedName,
                        onValueChange = { editedName = it },
                        label = { Text("Registrar Name", fontSize = 11.sp) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryOrange,
                            unfocusedBorderColor = BorderLight
                        )
                    )
                    OutlinedTextField(
                        value = editedUrl,
                        onValueChange = { editedUrl = it },
                        label = { Text("Allotment Portal URL", fontSize = 11.sp) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryOrange,
                            unfocusedBorderColor = BorderLight
                        )
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = editedIssues,
                            onValueChange = { editedIssues = it.filter { ch -> ch.isDigit() } },
                            label = { Text("Issues Managed", fontSize = 11.sp) },
                            singleLine = true,
                            modifier = Modifier.weight(1f),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = PrimaryOrange,
                                unfocusedBorderColor = BorderLight
                            )
                        )
                        OutlinedTextField(
                            value = editedAmount,
                            onValueChange = { editedAmount = it },
                            label = { Text("Amount (Cr)", fontSize = 11.sp) },
                            singleLine = true,
                            modifier = Modifier.weight(1f),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = PrimaryOrange,
                                unfocusedBorderColor = BorderLight
                            )
                        )
                    }
                    OutlinedTextField(
                        value = editedComments,
                        onValueChange = { editedComments = it },
                        label = { Text("Comments / Remarks (Database Column)", fontSize = 11.sp) },
                        maxLines = 2,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryOrange,
                            unfocusedBorderColor = BorderLight
                        )
                    )
                    OutlinedTextField(
                        value = editedModifiedBy,
                        onValueChange = { editedModifiedBy = it },
                        label = { Text("Modified By", fontSize = 11.sp) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryOrange,
                            unfocusedBorderColor = BorderLight
                        )
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Surface(
                        color = Color(0xFFF8FAFC),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(6.dp)) {
                            Text(
                                text = "Created: ${reg.createdDate} • Last Modified: ${reg.modifiedDate}",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                color = NeutralGray
                            )
                            Text(
                                text = "Persisted directly in Room Database & memory",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.5.sp),
                                color = SecondaryBlue
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (editedUrl.isNotBlank() && editedName.isNotBlank()) {
                            viewModel.saveFullRegistrar(
                                id = reg.id,
                                name = editedName,
                                url = editedUrl.trim(),
                                issuesManaged = editedIssues.toIntOrNull() ?: reg.issuesManaged,
                                issueAmountCr = editedAmount.toDoubleOrNull() ?: reg.issueAmountCr,
                                comment = editedComments,
                                modifiedBy = editedModifiedBy
                            )
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryOrange)
                ) {
                    Text("Save in Database", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.closeEditUrlDialog() }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Add New Registrar Dialog
    if (uiState.isAddRegistrarDialogOpen) {
        var newName by remember { mutableStateOf("") }
        var newUrl by remember { mutableStateOf("https://") }
        var newIssues by remember { mutableStateOf("0") }
        var newAmount by remember { mutableStateOf("0.0") }
        var newComment by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { viewModel.closeAddRegistrarDialog() },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Add, contentDescription = null, tint = PrimaryOrange, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Add New Registrar (Admin)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = newName,
                        onValueChange = { newName = it },
                        label = { Text("Registrar Name (e.g. Cameo Corporate)", fontSize = 11.sp) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryOrange,
                            unfocusedBorderColor = BorderLight
                        )
                    )
                    OutlinedTextField(
                        value = newUrl,
                        onValueChange = { newUrl = it },
                        label = { Text("Allotment Portal URL", fontSize = 11.sp) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryOrange,
                            unfocusedBorderColor = BorderLight
                        )
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = newIssues,
                            onValueChange = { newIssues = it.filter { ch -> ch.isDigit() } },
                            label = { Text("Issues Managed", fontSize = 11.sp) },
                            singleLine = true,
                            modifier = Modifier.weight(1f),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = PrimaryOrange,
                                unfocusedBorderColor = BorderLight
                            )
                        )
                        OutlinedTextField(
                            value = newAmount,
                            onValueChange = { newAmount = it },
                            label = { Text("Amount (Cr)", fontSize = 11.sp) },
                            singleLine = true,
                            modifier = Modifier.weight(1f),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = PrimaryOrange,
                                unfocusedBorderColor = BorderLight
                            )
                        )
                    }
                    OutlinedTextField(
                        value = newComment,
                        onValueChange = { newComment = it },
                        label = { Text("Comment / Remarks", fontSize = 11.sp) },
                        maxLines = 2,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryOrange,
                            unfocusedBorderColor = BorderLight
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newName.isNotBlank() && newUrl.isNotBlank()) {
                            viewModel.addNewRegistrar(
                                name = newName,
                                url = newUrl.trim(),
                                issuesManaged = newIssues.toIntOrNull() ?: 0,
                                issueAmountCr = newAmount.toDoubleOrNull() ?: 0.0,
                                comment = newComment
                            )
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryOrange)
                ) {
                    Text("Add to Database", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.closeAddRegistrarDialog() }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun RegistrarRowItem(
    registrar: com.example.ipotracker.data.model.RegistrarItem,
    onOpenUrl: () -> Unit,
    onEditUrl: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onOpenUrl() }
            .padding(vertical = 9.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1.8f)) {
            Text(
                text = registrar.name,
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.5.sp),
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = registrar.url,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.5.sp),
                color = SecondaryBlue,
                maxLines = 1
            )
            if (!registrar.comments.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "💬 ${registrar.comments}",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1
                    )
                }
            }
            Text(
                text = "Mod: ${registrar.modifiedDate} (${registrar.modifiedBy})",
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.5.sp),
                color = NeutralGray
            )
        }

        Text(
            text = "${registrar.issuesManaged}",
            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.5.sp),
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(0.7f)
        )

        Text(
            text = "₹${"%,.1f".format(registrar.issueAmountCr)}",
            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
            fontWeight = FontWeight.Medium,
            color = Color(0xFF0F766E),
            modifier = Modifier.weight(1.1f)
        )

        // Actions: Edit (Admin) & Open
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(
                onClick = onEditUrl,
                modifier = Modifier.size(26.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Edit Registrar (Admin)",
                    tint = PrimaryOrange,
                    modifier = Modifier.size(15.dp)
                )
            }
            IconButton(
                onClick = onOpenUrl,
                modifier = Modifier.size(26.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.OpenInNew,
                    contentDescription = "Open Portal",
                    tint = SecondaryBlue,
                    modifier = Modifier.size(15.dp)
                )
            }
        }
    }
}

@Composable
private fun RegistrarQuickLink(name: String, url: String, context: android.content.Context) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                context.startActivity(intent)
            }
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = name, style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.5.sp), fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
            Text(text = url, style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.5.sp), color = SecondaryBlue, maxLines = 1)
        }
        Icon(Icons.Default.OpenInNew, contentDescription = null, tint = SecondaryBlue, modifier = Modifier.size(14.dp))
    }
}
