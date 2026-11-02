package com.shiftledger.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.max

data class ShiftEntry(
    val id: String,
    val platform: String,
    val date: String,
    val basePay: Double,
    val tips: Double,
    val miles: Double,
    val hours: Double,
    val tolls: Double = 0.0
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme(colorScheme = darkColorScheme(
                primary = Color(0xFF10B981),
                background = Color(0xFF020617),
                surface = Color(0xFF0F172A)
            )) {
                ShiftLedgerApp()
            }
        }
    }
}

@Composable
fun ShiftLedgerApp() {
    val irsRate = 0.70
    val wearRate = 0.26
    val taxRate = 0.22

    val shifts = remember {
        mutableStateListOf(
            ShiftEntry("1", "Uber", "2026-10-28", 142.0, 38.5, 112.0, 5.5, 6.0),
            ShiftEntry("2", "DoorDash", "2026-10-29", 64.0, 52.0, 58.4, 4.0, 0.0),
            ShiftEntry("3", "Lyft", "2026-10-31", 168.5, 41.0, 128.5, 6.5, 8.5),
            ShiftEntry("4", "Independent 1099", "2026-11-02", 220.0, 25.0, 82.0, 6.0, 4.0)
        )
    }

    val totalGross = shifts.sumOf { it.basePay + it.tips }
    val totalMiles = shifts.sumOf { it.miles }
    val totalHours = shifts.sumOf { it.hours }
    val totalWear = shifts.sumOf { it.miles * wearRate + it.tolls }
    val totalIrsShield = shifts.sumOf { it.miles * irsRate + it.tolls }
    val taxableProfit = max(0.0, totalGross - totalIrsShield)
    val estTaxReserve = taxableProfit * taxRate
    val trueNetProfit = totalGross - totalWear - estTaxReserve
    val realNetHourly = if (totalHours > 0) trueNetProfit / totalHours else 0.0

    Surface(modifier = Modifier.fillMaxSize(), color = Color(0xFF020617)) {
        Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            Text("ShiftLedger • 1099 True-Profit & Tax Vault", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold)
            Text("IRS $0.70/mi Deduction + Vehicle Wear Engine", color = Color(0xFF94A3B8), fontSize = 12.sp)
            Spacer(modifier = Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                MetricCard("Gross Payout", "$%.2f".format(totalGross), Color.White, Modifier.weight(1f))
                MetricCard("True Net Profit", "$%.2f".format(trueNetProfit), Color(0xFF34D399), Modifier.weight(1f))
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                MetricCard("Real Net Hourly", "$%.2f/hr".format(realNetHourly), Color(0xFF38BDF8), Modifier.weight(1f))
                MetricCard("IRS Tax Reserve", "$%.2f".format(estTaxReserve), Color(0xFFFBBF24), Modifier.weight(1f))
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text("Recent Logged Shifts (${totalMiles.toInt()} total mi)", color = Color.White, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(shifts) { shift ->
                    val gross = shift.basePay + shift.tips
                    val wear = shift.miles * wearRate + shift.tolls
                    val shield = shift.miles * irsRate + shift.tolls
                    val tax = max(0.0, gross - shield) * taxRate
                    val net = gross - wear - tax
                    val netHr = if (shift.hours > 0) net / shift.hours else 0.0
                    Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)), shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
                        Row(modifier = Modifier.padding(12.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Column {
                                Text("${shift.platform} • ${shift.date}", color = Color.White, fontWeight = FontWeight.Bold)
                                Text("${shift.hours} hrs • ${shift.miles} mi • Gross: $%.2f".format(gross), color = Color(0xFF94A3B8), fontSize = 12.sp)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("$%.2f net".format(net), color = Color(0xFF34D399), fontWeight = FontWeight.ExtraBold)
                                Text("$%.2f/hr real".format(netHr), color = Color(0xFF38BDF8), fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MetricCard(label: String, value: String, valueColor: Color, modifier: Modifier = Modifier) {
    Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)), shape = RoundedCornerShape(12.dp), modifier = modifier) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(label, color = Color(0xFF94A3B8), fontSize = 11.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(value, color = valueColor, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold)
        }
    }
}
