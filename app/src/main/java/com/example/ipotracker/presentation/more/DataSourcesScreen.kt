package com.example.ipotracker.presentation.more

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.MarketGreen
import com.example.ui.theme.PrimaryOrange
import com.example.ui.theme.SecondaryBlue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DataSourcesScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("Official Data Sources & URLs", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
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
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(10.dp),
                    border = CardDefaults.outlinedCardBorder()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Info, contentDescription = null, tint = SecondaryBlue, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "REAL-TIME TRANSPARENCY & DATA ACCURACY",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = SecondaryBlue
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Every IPO detail, subscription bid, and Grey Market Premium (GMP) shown in this application is synchronized directly with official stock exchanges and verified financial intelligence desks.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 18.sp
                        )
                    }
                }
            }

            // 1. InvestorGain Live GMP
            item {
                VerifiedSourceCard(
                    title = "InvestorGain Live IPO GMP (Report 331)",
                    category = "Grey Market Premium & Fire Ratings",
                    verifiedUrl = "https://www.investorgain.com/report/live-ipo-gmp/331/",
                    description = "Primary live source for Grey Market Premium (₹ and %), Kostak rates, Subject to Sauda, and investor demand Fire Ratings (🔥 to 🔥🔥🔥🔥🔥).",
                    clarificationNote = "Live GMP rates for Acme India (₹30), SRIT India (₹28), TNA Solutions (₹7), and Vishal Nirmiti are matched 1-to-1 with this report.",
                    onOpen = {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.investorgain.com/report/live-ipo-gmp/331/"))
                        context.startActivity(intent)
                    },
                    onCopy = {
                        clipboardManager.setText(AnnotatedString("https://www.investorgain.com/report/live-ipo-gmp/331/"))
                        Toast.makeText(context, "URL copied to clipboard", Toast.LENGTH_SHORT).show()
                    }
                )
            }

            // 2. NSE India
            item {
                VerifiedSourceCard(
                    title = "NSE India - All Upcoming & Current Issues",
                    category = "National Stock Exchange",
                    verifiedUrl = "https://www.nseindia.com/market-data/all-upcoming-issues-ipo",
                    description = "Official NSE repository for all mainboard & SME IPOs, price bands, issue size, bidding periods, and cumulative bid quantities.",
                    clarificationNote = "Replaced previous deprecated NSE URL (/products-services/initial-public-offerings-current-issues which returned 404).",
                    onOpen = {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.nseindia.com/market-data/all-upcoming-issues-ipo"))
                        context.startActivity(intent)
                    },
                    onCopy = {
                        clipboardManager.setText(AnnotatedString("https://www.nseindia.com/market-data/all-upcoming-issues-ipo"))
                        Toast.makeText(context, "URL copied to clipboard", Toast.LENGTH_SHORT).show()
                    }
                )
            }

            // 3. BSE India
            item {
                VerifiedSourceCard(
                    title = "BSE India - Public Issues & PSO",
                    category = "Bombay Stock Exchange",
                    verifiedUrl = "https://www.bseindia.com/markets/publicissues/ipoissues.aspx?id=1&type=pso",
                    description = "Live tracking of active Mainboard and BSE SME listings, application status, security codes, and final issue closes.",
                    clarificationNote = "Replaced previous Bidding_Status.aspx which was moved by BSE with a redirect notice.",
                    onOpen = {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.bseindia.com/markets/publicissues/ipoissues.aspx?id=1&type=pso"))
                        context.startActivity(intent)
                    },
                    onCopy = {
                        clipboardManager.setText(AnnotatedString("https://www.bseindia.com/markets/publicissues/ipoissues.aspx?id=1&type=pso"))
                        Toast.makeText(context, "URL copied to clipboard", Toast.LENGTH_SHORT).show()
                    }
                )
            }

            // 4. Chittorgarh Report 82
            item {
                VerifiedSourceCard(
                    title = "Chittorgarh IPO List (Report 82)",
                    category = "Capital Structure & Master Directory",
                    verifiedUrl = "https://www.chittorgarh.com/report/ipo-in-india-list-main-board-sme/82/",
                    description = "Master directory of all historical, current, and upcoming Mainboard and SME IPOs in India.",
                    clarificationNote = "Important: Report 82 documents issue dates, fresh issue vs OFS, face values, and lot sizes. It does not publish GMP values. All GMP data is powered by InvestorGain.",
                    onOpen = {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.chittorgarh.com/report/ipo-in-india-list-main-board-sme/82/"))
                        context.startActivity(intent)
                    },
                    onCopy = {
                        clipboardManager.setText(AnnotatedString("https://www.chittorgarh.com/report/ipo-in-india-list-main-board-sme/82/"))
                        Toast.makeText(context, "URL copied to clipboard", Toast.LENGTH_SHORT).show()
                    }
                )
            }

            // 5. Registrars
            item {
                VerifiedSourceCard(
                    title = "SEBI Registered Share Transfer Agents (RTAs)",
                    category = "Allotment Verification Portals",
                    verifiedUrl = "https://in.mpms.mufg.com/Initial_Offer/public-issues.html",
                    description = "Official basis of allotment portals including MUFG Link Intime, KFin Technologies, and Bigshare Services.",
                    clarificationNote = "Users can verify allotment directly with PAN / Application No / DP Client ID without intermediary delays.",
                    onOpen = {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://in.mpms.mufg.com/Initial_Offer/public-issues.html"))
                        context.startActivity(intent)
                    },
                    onCopy = {
                        clipboardManager.setText(AnnotatedString("https://in.mpms.mufg.com/Initial_Offer/public-issues.html"))
                        Toast.makeText(context, "URL copied to clipboard", Toast.LENGTH_SHORT).show()
                    }
                )
            }
        }
    }
}

@Composable
fun VerifiedSourceCard(
    title: String,
    category: String,
    verifiedUrl: String,
    description: String,
    clarificationNote: String? = null,
    onOpen: () -> Unit,
    onCopy: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(10.dp),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = category.uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    color = SecondaryBlue,
                    fontWeight = FontWeight.Bold
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = MarketGreen, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("VERIFIED", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MarketGreen)
                }
            }

            Spacer(modifier = Modifier.height(6.dp))
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

            Spacer(modifier = Modifier.height(6.dp))
            Text(description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, lineHeight = 18.sp)

            clarificationNote?.let {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(8.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(Icons.Default.Info, contentDescription = null, tint = PrimaryOrange, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(it, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface, lineHeight = 16.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                shape = RoundedCornerShape(6.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = verifiedUrl,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.5.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(1f),
                        maxLines = 1
                    )
                    IconButton(onClick = onCopy, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.ContentCopy, contentDescription = "Copy", modifier = Modifier.size(14.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            Button(
                onClick = onOpen,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(6.dp),
                colors = ButtonDefaults.buttonColors(containerColor = SecondaryBlue)
            ) {
                Icon(Icons.Default.OpenInNew, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Open Official Page in Browser", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}
