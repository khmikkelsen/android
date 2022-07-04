package io.homeassistant.companion.android.sensors

import io.homeassistant.companion.android.common.sensors.SensorManager

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import io.homeassistant.companion.android.common.R as commonR

class HeartRateSensorManager : SensorManager {

    companion object {
        private const val TAG = "HeartRateSensor"
        private val heartRate = SensorManager.BasicSensor(
            "heart_rate",
            "sensor",
            commonR.string.basic_sensor_name_heart_rate,
            commonR.string.sensor_description_heart_rate,
            "mdi:heart-pulse",
            deviceClass = "sensor",
            unitOfMeasurement = "BPM",
            stateClass = SensorManager.STATE_CLASS_MEASUREMENT,
            entityCategory = SensorManager.ENTITY_CATEGORY_DIAGNOSTIC
        )
    }

    override fun docsLink(): String {
        return ""   //"https://companion.home-assistant.io/docs/core/sensors#heart-rate-sensors"
    }

    override val enabledByDefault: Boolean
        get() = false

    override val name: Int
        get() = commonR.string.sensor_name_heart

    override fun getAvailableSensors(context: Context): List<SensorManager.BasicSensor> {
        return listOf(
            heartRate
        )
    }

    override fun requiredPermissions(sensorId: String): Array<String> {
        return emptyArray()
    }

    override fun requestSensorUpdate(
        context: Context
    ) {
        val intent = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        if (intent != null) {
            updateBatteryState(context, intent)
            updateIsCharging(context, intent)
            updateChargerType(context, intent)
            updateBatteryHealth(context, intent)
            updateBatteryTemperature(context, intent)
        }
    }

    private fun updateHeartrateState(context: Context, intent: Intent) {
        if (!isEnabled(context, heartRate.id))
            return

        val chargingStatus = getChargingStatus(intent)

        val icon = "mdi:heart-pulse"

        onSensorUpdated(
            context,
            heartRate,
            chargingStatus,
            icon,
            mapOf()
        )
    }

    private fun getChargerType(intent: Intent): String {
        return when (intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1)) {
            BatteryManager.BATTERY_PLUGGED_AC -> "ac"
            BatteryManager.BATTERY_PLUGGED_USB -> "usb"
            BatteryManager.BATTERY_PLUGGED_WIRELESS -> "wireless"
            else -> "none"
        }
    }

    private fun getChargingStatus(intent: Intent): String {

        return when (intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)) {
            BatteryManager.BATTERY_STATUS_FULL -> "full"
            BatteryManager.BATTERY_STATUS_CHARGING -> "charging"
            BatteryManager.BATTERY_STATUS_DISCHARGING -> "discharging"
            BatteryManager.BATTERY_STATUS_NOT_CHARGING -> "not_charging"
            else -> "unknown"
        }
    }

    private fun getBatteryHealth(intent: Intent): String {
        return when (intent.getIntExtra(BatteryManager.EXTRA_HEALTH, -1)) {
            BatteryManager.BATTERY_HEALTH_COLD -> "cold"
            BatteryManager.BATTERY_HEALTH_DEAD -> "dead"
            BatteryManager.BATTERY_HEALTH_GOOD -> "good"
            BatteryManager.BATTERY_HEALTH_OVERHEAT -> "overheated"
            BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE -> "over_voltage"
            BatteryManager.BATTERY_HEALTH_UNSPECIFIED_FAILURE -> "failed"
            else -> "unknown"
        }
    }

    private fun getBatteryTemperature(intent: Intent): Float {
        return intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0) / 10f
    }
}
