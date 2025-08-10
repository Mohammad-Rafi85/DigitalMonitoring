package com.example.appusageoverlay

import android.app.AppOpsManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController

class MainActivity : ComponentActivity() {
    private lateinit var appLimitStore: AppLimitStore
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        try {
            appLimitStore = AppLimitStore(this)
            
            // Check and request permissions
            checkAndRequestPermissions()
            
            // Start the monitoring service only if permissions are granted
            if (hasUsageStatsPermission() && Settings.canDrawOverlays(this)) {
                startService(Intent(this, UsageMonitorService::class.java))
            }
            
            setContent {
                MaterialTheme {
                    MainScreen(appLimitStore)
                }
            }
        } catch (e: Exception) {
            // Handle any initialization errors
            Toast.makeText(this, "Error initializing app: ${e.message}", Toast.LENGTH_LONG).show()
            setContent {
                MaterialTheme {
                    ErrorScreen(e.message ?: "Unknown error")
                }
            }
        }
    }
    
    private fun checkAndRequestPermissions() {
        // Check overlay permission
        if (!Settings.canDrawOverlays(this)) {
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:$packageName")
            )
            startActivity(intent)
            Toast.makeText(this, "Please grant overlay permission", Toast.LENGTH_LONG).show()
        }
        
        // Check usage stats permission
        if (!hasUsageStatsPermission()) {
            val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
            startActivity(intent)
            Toast.makeText(this, "Please grant usage stats permission", Toast.LENGTH_LONG).show()
        }
    }
    
    private fun hasUsageStatsPermission(): Boolean {
        return try {
            val appOps = getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
            val mode = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                appOps.unsafeCheckOpNoThrow(
                    AppOpsManager.OPSTR_GET_USAGE_STATS,
                    android.os.Process.myUid(),
                    packageName
                )
            } else {
                @Suppress("DEPRECATION")
                appOps.checkOpNoThrow(
                    AppOpsManager.OPSTR_GET_USAGE_STATS,
                    android.os.Process.myUid(),
                    packageName
                )
            }
            mode == AppOpsManager.MODE_ALLOWED
        } catch (e: Exception) {
            false
        }
    }
}

sealed class Screen(val route: String, val label: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    object Home : Screen("home", "Home", Icons.Filled.Home)
    object Statistics : Screen("statistics", "Statistics", Icons.Filled.List)
    object Profile : Screen("profile", "Profile", Icons.Filled.AccountCircle)
    object AppLimits : Screen("applimits", "App Limits", Icons.Filled.Settings)
}

@Composable
fun MainScreen(appLimitStore: AppLimitStore) {
    val navController = rememberNavController()
    Scaffold(
        bottomBar = { BottomNavigationBar(navController) }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Home.route) { HomeScreen(navController) }
            composable(Screen.Statistics.route) { StatisticsScreen(appLimitStore) }
            composable(Screen.Profile.route) { ProfileScreen() }
            composable(Screen.AppLimits.route) { AppLimitsScreen(appLimitStore) }
        }
    }
}

@Composable
fun BottomNavigationBar(navController: NavHostController) {
    val items = listOf(Screen.Home, Screen.Statistics, Screen.AppLimits, Screen.Profile)
    BottomNavigation(
        backgroundColor = MaterialTheme.colors.primary,
        contentColor = Color.White
    ) {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route
        items.forEach { screen ->
            BottomNavigationItem(
                icon = { Icon(screen.icon, contentDescription = screen.label) },
                label = { Text(screen.label) },
                selected = currentRoute == screen.route,
                onClick = {
                    navController.navigate(screen.route) {
                        popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
    }
}

@Composable
fun HomeScreen(navController: NavHostController) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colors.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Welcome to Digital Monitoring!",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colors.primary
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Monitor your app usage, set time limits, and view statistics.",
                fontSize = 18.sp,
                color = Color.Gray
            )
            Spacer(modifier = Modifier.height(32.dp))
            Button(
                onClick = { navController.navigate(Screen.AppLimits.route) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF1976D2))
            ) {
                Icon(Icons.Filled.Settings, contentDescription = "App Limits", tint = Color.White)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Manage App Limits", color = Color.White, fontSize = 18.sp)
            }
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = { navController.navigate(Screen.Statistics.route) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF388E3C))
            ) {
                Icon(Icons.Filled.List, contentDescription = "Statistics", tint = Color.White)
                Spacer(modifier = Modifier.width(8.dp))
                Text("View Statistics", color = Color.White, fontSize = 18.sp)
            }
        }
    }
}

@Composable
fun StatisticsScreen(appLimitStore: AppLimitStore) {
    val limits by remember { mutableStateOf(appLimitStore.loadLimits().toMutableList()) }
    
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colors.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
        ) {
            Text(
                text = "Usage Statistics",
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colors.primary
            )
            Spacer(modifier = Modifier.height(16.dp))
            
            LazyColumn {
                items(limits) { appLimit ->
                    Card(
                        elevation = 4.dp,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = appLimit.appName,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Used: ${appLimit.usedMinutesToday} minutes / ${appLimit.timeLimitMinutes} minutes",
                                fontSize = 14.sp,
                                color = Color.Gray
                            )
                            LinearProgressIndicator(
                                progress = (appLimit.usedMinutesToday.toFloat() / appLimit.timeLimitMinutes.toFloat()).coerceIn(0f, 1f),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 8.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AppLimitsScreen(appLimitStore: AppLimitStore) {
    var limits by remember { mutableStateOf(appLimitStore.loadLimits().toMutableList()) }
    var showAddDialog by remember { mutableStateOf(false) }
    var selectedApp by remember { mutableStateOf("") }
    var timeLimit by remember { mutableStateOf("") }
    
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colors.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "App Limits",
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colors.primary
                )
                FloatingActionButton(
                    onClick = { showAddDialog = true },
                    backgroundColor = MaterialTheme.colors.primary
                ) {
                    Icon(Icons.Filled.Add, contentDescription = "Add Limit", tint = Color.White)
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            
            LazyColumn {
                items(limits) { appLimit ->
                    Card(
                        elevation = 4.dp,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = appLimit.appName,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = "${appLimit.timeLimitMinutes} minutes limit",
                                    fontSize = 14.sp,
                                    color = Color.Gray
                                )
                            }
                            IconButton(
                                onClick = {
                                    limits = limits.filter { it.packageName != appLimit.packageName }.toMutableList()
                                    appLimitStore.saveLimits(limits)
                                }
                            ) {
                                Icon(Icons.Filled.Delete, contentDescription = "Delete", tint = Color.Red)
                            }
                        }
                    }
                }
            }
        }
        
        if (showAddDialog) {
            AddAppLimitDialog(
                onDismiss = { showAddDialog = false },
                onAdd = { packageName, appName, timeLimit ->
                    val newLimit = AppLimit(packageName, appName, timeLimit.toInt())
                    limits = (limits + newLimit).toMutableList()
                    appLimitStore.saveLimits(limits)
                    showAddDialog = false
                }
            )
        }
    }
}

@Composable
fun AddAppLimitDialog(
    onDismiss: () -> Unit,
    onAdd: (String, String, String) -> Unit
) {
    var selectedApp by remember { mutableStateOf("") }
    var timeLimit by remember { mutableStateOf("") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add App Limit") },
        text = {
            Column {
                TextField(
                    value = selectedApp,
                    onValueChange = { selectedApp = it },
                    label = { Text("App Package Name (e.g., com.whatsapp)") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                TextField(
                    value = timeLimit,
                    onValueChange = { timeLimit = it },
                    label = { Text("Time Limit (minutes)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (selectedApp.isNotEmpty() && timeLimit.isNotEmpty()) {
                        onAdd(selectedApp, selectedApp, timeLimit)
                    }
                }
            ) {
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun ProfileScreen() {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colors.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(Icons.Filled.AccountCircle, contentDescription = null, modifier = Modifier.size(80.dp), tint = MaterialTheme.colors.primary)
            Spacer(modifier = Modifier.height(16.dp))
            Text("Digital Monitoring", fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Text("Version 1.0", fontSize = 16.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Text("Monitor and control your app usage", fontSize = 16.sp, color = Color.Gray)
        }
    }
}

@Composable
fun ErrorScreen(errorMessage: String) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colors.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(Icons.Filled.Warning, contentDescription = null, modifier = Modifier.size(80.dp), tint = Color.Red)
            Spacer(modifier = Modifier.height(16.dp))
            Text("Error", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.Red)
            Spacer(modifier = Modifier.height(8.dp))
            Text(errorMessage, fontSize = 16.sp, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
        }
    }
}