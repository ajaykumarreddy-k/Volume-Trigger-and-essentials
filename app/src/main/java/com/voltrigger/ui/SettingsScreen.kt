package com.voltrigger.ui

import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.provider.Settings
import android.view.Surface
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.voltrigger.R
import com.voltrigger.VolTileDownService
import com.voltrigger.VolTileUpService
import com.voltrigger.services.ControlNotificationService
import com.voltrigger.tiles.DigitalWellbeingTileService
import com.voltrigger.tiles.DndToggleTileService
import com.voltrigger.tiles.FlashlightTileService
import com.voltrigger.tiles.GithubProfileTileService
import com.voltrigger.tiles.QuickNoteTileService
import com.voltrigger.tiles.RotationLockTileService
import kotlinx.coroutines.delay

data class TileInfo(
    val name: String,
    val description: String,
    val iconResId: Int,
    val tileClass: Class<*>
)

val availableTiles = listOf(
    TileInfo("Volume Up", "Increases media volume", R.drawable.ic_tile_vol_up, VolTileUpService::class.java),
    TileInfo("Volume Down", "Decreases media volume", R.drawable.ic_tile_vol_down, VolTileDownService::class.java),
    TileInfo("Digital Wellbeing", "Open app usage settings", R.drawable.ic_tile_wellbeing, DigitalWellbeingTileService::class.java),
    TileInfo("GitHub Profile", "Open your GitHub profile", R.drawable.ic_tile_github, GithubProfileTileService::class.java),
    TileInfo("Flashlight", "Toggle device flashlight", R.drawable.ic_tile_flashlight, FlashlightTileService::class.java),
    TileInfo("Do Not Disturb", "Toggle DND mode", R.drawable.ic_tile_dnd, DndToggleTileService::class.java),
    TileInfo("Quick Note", "Write a quick note", R.drawable.ic_tile_quick_note, QuickNoteTileService::class.java),
    TileInfo("Rotation Lock", "Toggle screen auto-rotate", R.drawable.ic_tile_rotation, RotationLockTileService::class.java)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(viewModel: TileTogglesViewModel = viewModel()) {
    val githubUsername by viewModel.githubUsername.collectAsState()
    val controlCenterEnabled by viewModel.controlCenterEnabled.collectAsState()
    val tileStates by viewModel.tileStates.collectAsState()
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current

    val hasNotificationPermission = remember {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            androidx.core.content.ContextCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS) == android.content.pm.PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    val requestPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            val intent = Intent(context, ControlNotificationService::class.java)
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }
    }

    // Local state for TextField to avoid lag
    var localUsername by remember(githubUsername) { mutableStateOf(githubUsername) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Control Center", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                Spacer(modifier = Modifier.height(16.dp))
                
                // Persistent Notification Toggle
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Silent Features", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text("Enable persistent silent control center", style = MaterialTheme.typography.bodySmall)
                        }
                        Switch(
                            checked = controlCenterEnabled,
                            onCheckedChange = { isChecked ->
                                viewModel.setControlCenterEnabled(isChecked)
                                val intent = Intent(context, ControlNotificationService::class.java)
                                if (isChecked) {
                                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU && !hasNotificationPermission) {
                                        requestPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
                                    } else {
                                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                                            context.startForegroundService(intent)
                                        } else {
                                            context.startService(intent)
                                        }
                                    }
                                } else {
                                    context.stopService(intent)
                                }
                            }
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                VolumeSliderCard(context)
                Spacer(modifier = Modifier.height(24.dp))
                OrientationGridCard(context)
                Spacer(modifier = Modifier.height(24.dp))
                
                // GitHub Username Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("GitHub Profile", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = localUsername,
                            onValueChange = { localUsername = it },
                            placeholder = { Text("Enter username...") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                            keyboardActions = KeyboardActions(
                                onDone = {
                                    viewModel.setGithubUsername(localUsername)
                                    focusManager.clearFocus()
                                }
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )
                        Text(
                            text = "Press enter/done on keyboard to save",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 4.dp, start = 4.dp)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(32.dp))
                Text(
                    text = "Quick Settings Tiles",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                )
            }

            items(availableTiles) { tile ->
                val isEnabled = tileStates[tile.tileClass.name] ?: true
                TileToggleRow(
                    tileInfo = tile,
                    isEnabled = isEnabled,
                    onToggle = { checked ->
                        viewModel.toggleTile(tile.tileClass, checked)
                    }
                )
                Spacer(modifier = Modifier.height(12.dp))
            }
            
            item {
                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }
}

@Composable
fun VolumeSliderCard(context: Context) {
    val audioManager = remember { context.getSystemService(Context.AUDIO_SERVICE) as AudioManager }
    val maxVolume = remember { audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC).toFloat() }
    var currentVolume by remember { mutableStateOf(audioManager.getStreamVolume(AudioManager.STREAM_MUSIC).toFloat()) }
    
    // Poll volume to keep in sync if changed outside
    LaunchedEffect(Unit) {
        while (true) {
            val vol = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC).toFloat()
            if (vol != currentVolume) {
                currentVolume = vol
            }
            delay(1000)
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Media Volume", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                Icon(painter = painterResource(android.R.drawable.ic_lock_silent_mode_off), contentDescription = null, tint = MaterialTheme.colorScheme.onPrimaryContainer)
            }
            Spacer(modifier = Modifier.height(16.dp))
            
            // Custom Thick Horizontal Slider
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.inversePrimary.copy(alpha = 0.5f))
            ) {
                BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                    val width = maxWidth
                    val progress = if (maxVolume > 0) currentVolume / maxVolume else 0f
                    
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .width(width * progress)
                            .clip(CircleShape)
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(
                                        MaterialTheme.colorScheme.primary,
                                        MaterialTheme.colorScheme.tertiary
                                    )
                                )
                            )
                    )
                    
                    // Invisible overlay for gestures
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .pointerInput(Unit) {
                                detectTapGestures { offset ->
                                    val newProgress = (offset.x / size.width.toFloat()).coerceIn(0f, 1f)
                                    currentVolume = newProgress * maxVolume
                                    audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, currentVolume.toInt(), 0)
                                }
                            }
                            .pointerInput(Unit) {
                                detectHorizontalDragGestures { change, _ ->
                                    val newProgress = (change.position.x / size.width.toFloat()).coerceIn(0f, 1f)
                                    currentVolume = newProgress * maxVolume
                                    audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, currentVolume.toInt(), 0)
                                }
                            }
                    )
                }
            }
        }
    }
}

@Composable
fun OrientationGridCard(context: Context) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text("Screen Orientation", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OrientationButton(modifier = Modifier.weight(1f), label = "Portrait", rotation = Surface.ROTATION_0, context = context)
                OrientationButton(modifier = Modifier.weight(1f), label = "Land. Right", rotation = Surface.ROTATION_90, context = context)
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OrientationButton(modifier = Modifier.weight(1f), label = "Land. Left", rotation = Surface.ROTATION_270, context = context)
                OrientationButton(modifier = Modifier.weight(1f), label = "Upside Down", rotation = Surface.ROTATION_180, context = context)
            }
        }
    }
}

@Composable
fun OrientationButton(modifier: Modifier = Modifier, label: String, rotation: Int, context: Context) {
    Box(
        modifier = modifier
            .height(60.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.secondaryContainer)
            .clickable {
                if (!Settings.System.canWrite(context)) {
                    val intent = Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(intent)
                } else {
                    try {
                        Settings.System.putInt(context.contentResolver, Settings.System.ACCELEROMETER_ROTATION, 0)
                        Settings.System.putInt(context.contentResolver, Settings.System.USER_ROTATION, rotation)
                    } catch (e: Exception) {
                        // ignore
                    }
                }
            },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label, 
            style = MaterialTheme.typography.labelLarge, 
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSecondaryContainer
        )
    }
}

@Composable
fun TileToggleRow(tileInfo: TileInfo, isEnabled: Boolean, onToggle: (Boolean) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = tileInfo.iconResId),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = tileInfo.name, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                Text(text = tileInfo.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Switch(
                checked = isEnabled,
                onCheckedChange = onToggle,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = MaterialTheme.colorScheme.primary,
                    checkedTrackColor = MaterialTheme.colorScheme.primaryContainer,
                    uncheckedThumbColor = MaterialTheme.colorScheme.outline,
                    uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant
                )
            )
        }
    }
}
