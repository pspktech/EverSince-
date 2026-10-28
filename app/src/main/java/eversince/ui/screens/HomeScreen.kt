package eversince.ui.screens

import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.eversince.data.model.SpecialDay
import com.eversince.data.model.SpecialMoment
import com.eversince.ui.theme.*
import com.eversince.viewmodel.EverSinceViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(vm: EverSinceViewModel = viewModel(), onPickPartnerPhoto: () -> Unit = {}) {
    val relationship by vm.relationship.collectAsStateWithLifecycle()
    val liveTime by vm.liveTime.collectAsStateWithLifecycle()
    val moments by vm.moments.collectAsStateWithLifecycle()
    val specialDays by vm.specialDays.collectAsStateWithLifecycle()
    var tab by remember { mutableStateOf(0) }
    var profile by remember { mutableStateOf(false) }
    var addMoment by remember { mutableStateOf(false) }
    var addDay by remember { mutableStateOf(false) }
    var revealed by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { revealed = true }
    Box(Modifier.fillMaxSize().background(Brush.linearGradient(listOf(DeepPlum, DeepDark, RoseDark)))) {
        LazyColumn(contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            item { AnimatedVisibility(revealed, enter = fadeIn() + slideInVertically { -it / 2 }) { Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) { Column { Text("EverSince", color = SoftPeach, fontWeight = FontWeight.Bold); Text("Your little forever", color = Color.White, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold) }; IconButton({ profile = true }) { Icon(Icons.Default.Edit, "Edit", tint = SoftPeach) } } } }
            item { AnimatedVisibility(revealed, enter = fadeIn() + slideInVertically { it / 2 }) { GlassCard { Row(verticalAlignment = Alignment.CenterVertically) { PhotoAvatar(relationship.partnerPhotoUri, onPickPartnerPhoto); Spacer(Modifier.width(16.dp)); Column(Modifier.weight(1f)) { Text("${relationship.person1}  ♥  ${relationship.person2}", color = Color.White, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold); Text("Together since ${relationship.startDate}", color = SoftPeach.copy(alpha = .8f), style = MaterialTheme.typography.bodySmall) }; Icon(Icons.Default.Favorite, "Love", tint = RosePink, modifier = Modifier.size(28.dp)) } } } }
            item { GlassCard { Text("Our time, still ticking", color = SoftPeach); Spacer(Modifier.height(8.dp)); Text(liveTime, color = Color.White, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.ExtraBold); Spacer(Modifier.height(12.dp)); LinearProgressIndicator({ .72f }, Modifier.fillMaxWidth().height(6.dp).clip(CircleShape), color = RosePink, trackColor = Color.White.copy(alpha = .12f)); Spacer(Modifier.height(6.dp)); Text("Every second is a story worth keeping.", color = Color.White.copy(alpha = .62f), style = MaterialTheme.typography.bodySmall) } }
            item { Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).background(Color.White.copy(alpha = .08f)).padding(4.dp)) { listOf("Moments", "Special days").forEachIndexed { i, label -> FilterChip(tab == i, { tab = i }, label = { Text(label) }, leadingIcon = { Icon(if (i == 0) Icons.Default.MenuBook else Icons.Default.CalendarMonth, null) }, modifier = Modifier.weight(1f)) } } }
            if (tab == 0) { item { SectionHeader("Our special moments", "Add a memory", { addMoment = true }) }; if (moments.isEmpty()) item { EmptyState("Your first memory is waiting here.") }; items(moments, key = { it.id }) { MomentCard(it) } }
            else { item { SectionHeader("Upcoming special days", "Save a day", { addDay = true }) }; if (specialDays.isEmpty()) item { EmptyState("Birthdays, anniversaries, and tiny reasons to celebrate.") }; items(specialDays, key = { it.id }) { SpecialDayCard(it, vm::toggleReminder) } }
        }
    }
    if (profile) ProfileDialog(relationship.person1, relationship.person2, relationship.startDate.toString(), { a, b, d -> vm.saveProfile(a, b, d); profile = false }, { profile = false })
    if (addMoment) EntryDialog("New moment", "Save moment", false, { a, b, c, _ -> vm.addMoment(a, b, c); addMoment = false }, { addMoment = false })
    if (addDay) EntryDialog("Upcoming special day", "Save day", true, { a, b, c, d -> vm.addSpecialDay(a, b, c, d); addDay = false }, { addDay = false })
}

@Composable private fun GlassCard(content: @Composable ColumnScope.() -> Unit) = Card(colors = CardDefaults.cardColors(Color.White.copy(alpha = .11f)), border = BorderStroke(1.dp, Color.White.copy(alpha = .18f)), shape = RoundedCornerShape(26.dp), modifier = Modifier.fillMaxWidth()) { Column(Modifier.padding(20.dp), content = content) }

@Composable private fun PhotoAvatar(uri: String?, onClick: () -> Unit) { var bitmap by remember(uri) { mutableStateOf<android.graphics.Bitmap?>(null) }; val context = androidx.compose.ui.platform.LocalContext.current; LaunchedEffect(uri) { bitmap = uri?.let { withContext(Dispatchers.IO) { runCatching { context.contentResolver.openInputStream(Uri.parse(it))?.use(BitmapFactory::decodeStream) }.getOrNull() } } }; Box(Modifier.size(64.dp).clip(CircleShape).background(Brush.linearGradient(listOf(RosePink, SoftPeach))).clickable(onClick = onClick), contentAlignment = Alignment.Center) { if (bitmap != null) Image(bitmap!!.asImageBitmap(), "Partner photo", Modifier.fillMaxSize(), contentScale = ContentScale.Crop) else Icon(Icons.Default.Image, "Add partner photo", tint = Color.White, modifier = Modifier.size(30.dp)) } }
@Composable private fun SectionHeader(title: String, action: String, onClick: () -> Unit) = Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) { Text(title, color = Color.White, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold); TextButton(onClick) { Icon(Icons.Default.Add, null, tint = SoftPeach); Text(action, color = SoftPeach) } }
@Composable private fun EmptyState(text: String) = GlassCard { Text(text, color = Color.White.copy(alpha = .65f)) }
@Composable private fun MomentCard(m: SpecialMoment) = GlassCard { Row(verticalAlignment = Alignment.Top) { Icon(Icons.Default.Favorite, null, tint = RosePink); Spacer(Modifier.width(12.dp)); Column { Text(m.title, color = Color.White, fontWeight = FontWeight.Bold); Text(m.date.toString(), color = SoftPeach, style = MaterialTheme.typography.labelSmall); if (m.note.isNotBlank()) Text(m.note, color = Color.White.copy(alpha = .72f), modifier = Modifier.padding(top = 8.dp)) } } }
@Composable private fun SpecialDayCard(d: SpecialDay, toggle: (SpecialDay) -> Unit) = GlassCard { Row(verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Default.CalendarMonth, null, tint = SoftPeach); Spacer(Modifier.width(12.dp)); Column(Modifier.weight(1f)) { Text(d.title, color = Color.White, fontWeight = FontWeight.Bold); Text(d.date.toString(), color = SoftPeach, style = MaterialTheme.typography.labelSmall); if (d.note.isNotBlank()) Text(d.note, color = Color.White.copy(alpha = .72f), style = MaterialTheme.typography.bodySmall) }; IconButton({ toggle(d) }) { Icon(if (d.reminderEnabled) Icons.Default.NotificationsActive else Icons.Default.Alarm, "Toggle reminder", tint = if (d.reminderEnabled) RosePink else Color.White.copy(alpha = .45f)) } } }

@Composable private fun ProfileDialog(a0: String, b0: String, d0: String, save: (String, String, String) -> Unit, close: () -> Unit) { var a by remember { mutableStateOf(a0) }; var b by remember { mutableStateOf(b0) }; var d by remember { mutableStateOf(d0) }; AlertDialog(close, title = { Text("Your story") }, text = { Column(verticalArrangement = Arrangement.spacedBy(10.dp)) { OutlinedTextField(a, { a = it }, label = { Text("Your name") }); OutlinedTextField(b, { b = it }, label = { Text("Partner name") }); OutlinedTextField(d, { d = it }, label = { Text("Together since (YYYY-MM-DD)") }) } }, confirmButton = { TextButton({ save(a, b, d) }) { Text("Save") } }, dismissButton = { TextButton(close) { Text("Cancel") } }) }
@Composable private fun EntryDialog(title: String, action: String, reminderOption: Boolean, save: (String, String, String, Boolean) -> Unit, close: () -> Unit) { var name by remember { mutableStateOf("") }; var date by remember { mutableStateOf(LocalDate.now().toString()) }; var note by remember { mutableStateOf("") }; var reminder by remember { mutableStateOf(true) }; AlertDialog(close, title = { Text(title) }, text = { Column(verticalArrangement = Arrangement.spacedBy(10.dp)) { OutlinedTextField(name, { name = it }, label = { Text("Title") }); OutlinedTextField(date, { date = it }, label = { Text("Date (YYYY-MM-DD)") }); OutlinedTextField(note, { note = it }, label = { Text("Notes") }); if (reminderOption) Row(verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Default.NotificationsActive, null, tint = RosePink); Text("Remind me", Modifier.weight(1f)); Switch(reminder, { reminder = it }) } } }, confirmButton = { TextButton({ if (name.isNotBlank() && runCatching { LocalDate.parse(date) }.isSuccess) save(name, date, note, reminder) }) { Text(action) } }, dismissButton = { TextButton(close) { Text("Cancel") } }) }
