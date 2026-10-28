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
fun DashboardScreen(vm: EverSinceViewModel = viewModel(), onPickPartnerPhoto: () -> Unit = {}) {
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


@Composable
fun AppRoot(vm: EverSinceViewModel = viewModel(), onPickPartnerPhoto: () -> Unit = {}) {
    val onboarded by vm.onboarded.collectAsStateWithLifecycle()
    if (!onboarded) {
        OnboardingScreen(vm)
        return
    }
    var selected by remember { mutableStateOf(0) }
    val labels = listOf("Home", "Live", "Moments", "Days", "Settings")
    val icons = listOf(Icons.Default.Home, Icons.Default.AccessTime, Icons.Default.MenuBook, Icons.Default.CalendarMonth, Icons.Default.Settings)
    Scaffold(modifier = Modifier.fillMaxSize(), containerColor = DeepDark, bottomBar = {
        NavigationBar(containerColor = DeepDark.copy(alpha = .96f)) {
            labels.forEachIndexed { index, label -> NavigationBarItem(selected == index, { selected = index }, icon = { Icon(icons[index], label) }, label = { Text(label) }, colors = NavigationBarItemDefaults.colors(selectedIconColor = RosePink, selectedTextColor = SoftPeach, indicatorColor = RosePink.copy(alpha = .16f), unselectedIconColor = Color.White.copy(alpha = .55f), unselectedTextColor = Color.White.copy(alpha = .55f))) }
        }
    }) { padding ->
        Box(Modifier.padding(padding)) {
            when (selected) {
                0 -> DashboardScreen(vm, onPickPartnerPhoto)
                1 -> LiveCounterScreen(vm)
                2 -> MomentsTimelineScreen(vm)
                3 -> SpecialDaysScreen(vm)
                4 -> SettingsScreen(vm)
            }
        }
    }
}

@Composable private fun OnboardingScreen(vm: EverSinceViewModel) {
    var name by remember { mutableStateOf("Madhu") }
    var partner by remember { mutableStateOf("Partner") }
    var date by remember { mutableStateOf("2025-05-27") }
    Box(Modifier.fillMaxSize().background(Brush.linearGradient(listOf(DeepPlum, DeepDark, RoseDark))).padding(24.dp), contentAlignment = Alignment.Center) {
        GlassCard {
            Icon(Icons.Default.Favorite, null, tint = RosePink, modifier = Modifier.size(42.dp).align(Alignment.CenterHorizontally))
            Spacer(Modifier.height(10.dp)); Text("Let’s make your forever visible", color = Color.White, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold); Text("Your details stay on this device.", color = SoftPeach.copy(alpha = .8f)); Spacer(Modifier.height(20.dp))
            OutlinedTextField(name, { name = it }, Modifier.fillMaxWidth(), label = { Text("Your name") }); Spacer(Modifier.height(10.dp)); OutlinedTextField(partner, { partner = it }, Modifier.fillMaxWidth(), label = { Text("Partner name") }); Spacer(Modifier.height(10.dp)); OutlinedTextField(date, { date = it }, Modifier.fillMaxWidth(), label = { Text("Together since (YYYY-MM-DD)") }); Spacer(Modifier.height(18.dp))
            Button(onClick = { if (runCatching { LocalDate.parse(date) }.isSuccess) vm.completeOnboarding(name, partner, date) }, Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = RosePink)) { Text("Start our story") }
        }
    }
}

@Composable private fun LiveCounterScreen(vm: EverSinceViewModel) {
    val relationship by vm.relationship.collectAsStateWithLifecycle(); val liveTime by vm.liveTime.collectAsStateWithLifecycle()
    LazyColumn(contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item { Text("Live counter", color = SoftPeach, style = MaterialTheme.typography.labelLarge); Text("Every second counts", color = Color.White, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold) }
        item { GlassCard { Icon(Icons.Default.Favorite, null, tint = RosePink, modifier = Modifier.size(44.dp)); Spacer(Modifier.height(12.dp)); Text("${relationship.person1} & ${relationship.person2}", color = Color.White, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold); Text("Since ${relationship.startDate}", color = SoftPeach); Spacer(Modifier.height(20.dp)); Text(liveTime, color = Color.White, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.ExtraBold) } }
        item { Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) { CounterTile("Days", liveTime.substringBefore(" days")); CounterTile("Start date", relationship.startDate.toString()) } }
    }
}
@Composable private fun CounterTile(label: String, value: String) = GlassCard { Text(value, color = RosePink, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold); Text(label, color = SoftPeach, style = MaterialTheme.typography.labelSmall) }

@Composable private fun MomentsTimelineScreen(vm: EverSinceViewModel) {
    val moments by vm.moments.collectAsStateWithLifecycle(); var add by remember { mutableStateOf(false) }
    LazyColumn(contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) { item { SectionHeader("Our story", "Add moment", { add = true }); Text("A timeline of the little things that became everything.", color = Color.White.copy(alpha = .7f)) }; if (moments.isEmpty()) item { EmptyState("No moments yet. Add your first date, trip, or tiny win.") }; items(moments, key = { it.id }) { moment -> Row { Column(horizontalAlignment = Alignment.CenterHorizontally) { Box(Modifier.size(14.dp).clip(CircleShape).background(RosePink)); Box(Modifier.width(2.dp).height(100.dp).background(RosePink.copy(alpha = .35f))) }; Spacer(Modifier.width(12.dp)); MomentCard(moment) } } }
    if (add) EntryDialog("New moment", "Save moment", false, { a, b, c, _ -> vm.addMoment(a, b, c); add = false }, { add = false })
}

@Composable private fun SpecialDaysScreen(vm: EverSinceViewModel) {
    val days by vm.specialDays.collectAsStateWithLifecycle(); var add by remember { mutableStateOf(false) }
    LazyColumn(contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) { item { Text("Special days", color = SoftPeach, style = MaterialTheme.typography.labelLarge); Text("Never miss a reason to celebrate", color = Color.White, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold); SectionHeader("Upcoming", "Add day", { add = true }) }; if (days.isEmpty()) item { EmptyState("Save birthdays, anniversaries, trips, and plans.") }; items(days, key = { it.id }) { day -> val remaining = java.time.temporal.ChronoUnit.DAYS.between(LocalDate.now(), day.date).coerceAtLeast(0); GlassCard { Row(verticalAlignment = Alignment.CenterVertically) { Column(Modifier.weight(1f)) { Text(day.title, color = Color.White, fontWeight = FontWeight.Bold); Text("$remaining days to go · ${day.date}", color = SoftPeach); if (day.note.isNotBlank()) Text(day.note, color = Color.White.copy(alpha = .7f)) }; IconButton({ vm.toggleReminder(day) }) { Icon(if (day.reminderEnabled) Icons.Default.NotificationsActive else Icons.Default.Alarm, "Reminder", tint = if (day.reminderEnabled) RosePink else Color.White.copy(alpha = .45f)) } } } } }
    if (add) EntryDialog("Upcoming special day", "Save day", true, { a, b, c, d -> vm.addSpecialDay(a, b, c, d); add = false }, { add = false })
}

@Composable private fun SettingsScreen(vm: EverSinceViewModel) {
    val relationship by vm.relationship.collectAsStateWithLifecycle()
    LazyColumn(contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) { item { Text("Settings", color = SoftPeach, style = MaterialTheme.typography.labelLarge); Text("Make it yours", color = Color.White, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold) }; item { GlassCard { Text("Relationship", color = Color.White, fontWeight = FontWeight.Bold); Text("${relationship.person1} & ${relationship.person2}", color = SoftPeach); Text("Together since ${relationship.startDate}", color = Color.White.copy(alpha = .65f)) } }; item { GlassCard { Row(verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Default.Lock, null, tint = SoftPeach); Spacer(Modifier.width(12.dp)); Column(Modifier.weight(1f)) { Text("Private by design", color = Color.White, fontWeight = FontWeight.Bold); Text("Your story is saved only on this device.", color = Color.White.copy(alpha = .65f)) } } } }; item { OutlinedButton(onClick = vm::resetOnboarding, Modifier.fillMaxWidth(), border = BorderStroke(1.dp, RosePink)) { Text("Edit onboarding details", color = SoftPeach) } } }
}
