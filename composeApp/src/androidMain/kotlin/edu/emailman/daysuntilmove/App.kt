package edu.emailman.daysuntilmove

import android.app.DatePickerDialog
import android.content.Context
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.concurrent.TimeUnit
import androidx.core.content.edit

@Composable
@Preview
fun App() {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("move_prefs",
        Context.MODE_PRIVATE) }
    val isoFmt = remember { SimpleDateFormat("yyyy-MM-dd",
        Locale.getDefault()) }
    val displayFmt = remember { SimpleDateFormat("MMMM d, yyyy",
        Locale.getDefault()) }

    var moveDateStr by remember {
        mutableStateOf(prefs.getString("move_date", "") ?: "")
    }

    val todayStr = remember {
        displayFmt.format(Calendar.getInstance().time) }

    val daysUntil: Long? = remember(moveDateStr) {
        if (moveDateStr.isEmpty()) null
        else runCatching {
            val moveDate =
                isoFmt.parse(moveDateStr) ?: return@runCatching null
            val today = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.time
            TimeUnit.MILLISECONDS.toDays(moveDate.time - today.time)
        }.getOrNull()
    }

    MaterialTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp,
                Alignment.CenterVertically),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Field 1: Projected move date — tap to open date picker,
            // persisted in SharedPreferences
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        val cal = Calendar.getInstance()
                        if (moveDateStr.isNotEmpty()) {
                            runCatching { isoFmt.parse(moveDateStr) }
                                .getOrNull()?.let { cal.time = it }
                        }
                        DatePickerDialog(
                            context,
                            { _, year, month, day ->
                                cal.set(year, month, day)
                                val picked = isoFmt.format(cal.time)
                                moveDateStr = picked
                                prefs.edit { putString("move_date",
                                    picked) }
                            },
                            cal.get(Calendar.YEAR),
                            cal.get(Calendar.MONTH),
                            cal.get(Calendar.DAY_OF_MONTH)
                        ).show()
                    }
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Projected Move Date",
                        style = MaterialTheme.typography.labelLarge)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (moveDateStr.isEmpty()) "Tap to set date"
                        else runCatching {
                            displayFmt.format(
                                isoFmt.parse(moveDateStr)!!)
                        }.getOrDefault(moveDateStr),
                        style = MaterialTheme.typography.headlineSmall
                    )
                }
            }

            // Field 2: Today's date
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Today's Date",
                        style = MaterialTheme.typography.labelLarge)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(todayStr,
                        style = MaterialTheme.typography.headlineSmall)
                }
            }

            // Field 3: Days until move
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Days Until Move",
                        style = MaterialTheme.typography.labelLarge)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = when {
                            daysUntil == null -> "Set a move date first"
                            daysUntil < 0 -> "Move date has passed"
                            daysUntil == 0L -> "Moving today!"
                            else -> "$daysUntil days"
                        },
                        style = MaterialTheme.typography.headlineSmall
                    )
                }
            }
        }
    }
}