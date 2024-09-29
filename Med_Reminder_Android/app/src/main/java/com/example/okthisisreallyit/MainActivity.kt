package com.example.okthisisreallyit

import android.app.AlarmManager
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TimePicker
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import android.app.AlertDialog
import android.provider.Settings
import android.widget.Toast
import java.util.*
import android.util.Log

class MainActivity : AppCompatActivity() {

    private lateinit var medicineName: EditText
    private lateinit var timePicker: TimePicker
    private lateinit var setReminder: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        medicineName = findViewById(R.id.medicineName)
        timePicker = findViewById(R.id.timePicker)
        setReminder = findViewById(R.id.setReminder)

        setReminder.setOnClickListener {
            checkExactAlarmPermission()
        }
    }

    private fun checkExactAlarmPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
            if (!alarmManager.canScheduleExactAlarms()) {
                Log.d("MainActivity", "Exact alarm permission not granted")
                AlertDialog.Builder(this)
                    .setTitle("Permission Required")
                    .setMessage("This app needs permission to schedule exact alarms. Please grant the permission.")
                    .setPositiveButton("Grant Permission") { _, _ ->
                        startActivity(Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM))
                    }
                    .setNegativeButton("Cancel", null)
                    .show()
                return
            }
        }
        Log.d("MainActivity", "Exact alarm permission granted")
        setNotification()
    }

    private fun setNotification() {
        val medName = medicineName.text.toString()
        val calendar = Calendar.getInstance()
        val hour: Int = timePicker.hour
        val minute: Int = timePicker.minute

        calendar.set(Calendar.HOUR_OF_DAY, hour)
        calendar.set(Calendar.MINUTE, minute)

        Log.d("MainActivity", "Setting alarm for $medName at $hour:$minute")

        val intent = Intent(this, NotificationReceiver::class.java)
        intent.putExtra("medicineName", medName)

        val pendingIntent = PendingIntent.getBroadcast(
            this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)

        Toast.makeText(this, "Reminder set for $medName at $hour:$minute", Toast.LENGTH_SHORT).show()
    }


    class NotificationReceiver : BroadcastReceiver() {
        companion object {
            private const val CHANNEL_ID = "MedicineChannel"
        }

        override fun onReceive(context: Context, intent: Intent) {
            val medName = intent.getStringExtra("medicineName")
            Log.d("NotificationReceiver", "Alarm received for medicine: $medName")

            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(
                    CHANNEL_ID,
                    "Medicine Notification",
                    NotificationManager.IMPORTANCE_HIGH
                )
                notificationManager.createNotificationChannel(channel)
            }

            val notification: Notification = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle("Medicine Reminder")
                .setContentText("It's time to take your medicine: $medName")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .build()

            Log.d("NotificationReceiver", "Displaying notification for medicine: $medName")

            notificationManager.notify(1, notification)
        }

    }
}
