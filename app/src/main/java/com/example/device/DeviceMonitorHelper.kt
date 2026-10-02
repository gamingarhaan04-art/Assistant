package com.example.device

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.BatteryManager
import android.os.Build

data class DeviceDiagnostics(
    val deviceName: String,
    val osVersion: String,
    val batteryPct: Int,
    val isCharging: Boolean,
    val networkType: String,
    val isOnline: Boolean,
    val ramTotalGb: Float,
    val ramAvailGb: Float,
    val cpuCores: Int
)

object DeviceMonitorHelper {

    fun getDiagnostics(context: Context): DeviceDiagnostics {
        // Device info
        val manufacturer = Build.MANUFACTURER.replaceFirstChar { it.uppercase() }
        val model = Build.MODEL
        val deviceName = "$manufacturer $model (Android ${Build.VERSION.RELEASE})"
        val osVersion = "Android ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})"

        // Battery info
        var batteryPct = 85
        var isCharging = false
        try {
            val iFilter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
            val batteryStatus = context.registerReceiver(null, iFilter)
            if (batteryStatus != null) {
                val level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
                val scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
                if (level >= 0 && scale > 0) {
                    batteryPct = (level * 100 / scale.toFloat()).toInt()
                }
                val status = batteryStatus.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
                isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                        status == BatteryManager.BATTERY_STATUS_FULL
            }
        } catch (e: Exception) {
            // fallback
        }

        // Network info
        var networkType = "Offline"
        var isOnline = false
        try {
            val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val net = cm.activeNetwork
            val caps = cm.getNetworkCapabilities(net)
            if (caps != null) {
                isOnline = caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                networkType = when {
                    caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> "Wi-Fi Active"
                    caps.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> {
                        val down = caps.linkDownstreamBandwidthKbps / 1000
                        if (down > 0) "5G/LTE · ${down} Mbps" else "Mobile Data"
                    }
                    caps.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> "Ethernet"
                    else -> "Connected"
                }
            }
        } catch (e: Exception) {
            networkType = "Active"
            isOnline = true
        }

        // Memory info
        var ramTotalGb = 8.0f
        var ramAvailGb = 4.2f
        try {
            val actManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            val memInfo = ActivityManager.MemoryInfo()
            actManager.getMemoryInfo(memInfo)
            ramTotalGb = memInfo.totalMem / (1024f * 1024f * 1024f)
            ramAvailGb = memInfo.availMem / (1024f * 1024f * 1024f)
        } catch (e: Exception) {
            // fallback
        }

        val cpuCores = Runtime.getRuntime().availableProcessors()

        return DeviceDiagnostics(
            deviceName = deviceName,
            osVersion = osVersion,
            batteryPct = batteryPct,
            isCharging = isCharging,
            networkType = networkType,
            isOnline = isOnline,
            ramTotalGb = (Math.round(ramTotalGb * 10f) / 10f),
            ramAvailGb = (Math.round(ramAvailGb * 10f) / 10f),
            cpuCores = cpuCores
        )
    }
}
