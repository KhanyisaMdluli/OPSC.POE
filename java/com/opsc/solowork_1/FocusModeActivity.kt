package com.opsc.solowork_1

import android.app.NotificationManager
import android.content.Context
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.media.ToneGenerator
import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.opsc.solowork_1.databinding.ActivityFocusModeBinding
import com.opsc.solowork_1.model.FocusSession
import com.opsc.solowork_1.utils.AuthUtils
import com.opsc.solowork_1.utils.FocusModeUtils
import com.opsc.solowork_1.utils.LanguageManager
import java.util.*
import java.util.concurrent.TimeUnit

class FocusModeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFocusModeBinding
    private var countDownTimer: CountDownTimer? = null
    private var isTimerRunning = false
    private var timeLeftInMillis: Long = 0
    private var totalTimeInMillis: Long = 0
    private var mediaPlayer: MediaPlayer? = null
    private var sessionStartTime: Long = 0
    private lateinit var languageManager: LanguageManager

    // Timer presets in minutes
    private val timerPresets = arrayOf(25, 45, 60, 90, 120)

    override fun onCreate(savedInstanceState: Bundle?) {
        languageManager = LanguageManager(this)
        languageManager.applySavedLanguage(this)
        super.onCreate(savedInstanceState)
        binding = ActivityFocusModeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupPresetButtons()
        setupClickListeners()
        updateTimerDisplay(25 * 60 * 1000) // Default 25 minutes
    }

    override fun onResume() {
        super.onResume()
        languageManager.applySavedLanguage(this)
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun setupPresetButtons() {
        val presetContainer = binding.presetContainer
        presetContainer.removeAllViews()

        timerPresets.forEach { minutes ->
            val button = Button(this).apply {
                text = context.getString(R.string.minutes_format, minutes)
                setOnClickListener {
                    setTimerDuration(minutes * 60 * 1000L)
                }
                setBackgroundColor(ContextCompat.getColor(context, R.color.primary_color))
                setTextColor(ContextCompat.getColor(context, R.color.white))
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(8, 8, 8, 8)
                }
            }
            presetContainer.addView(button)
        }
    }

    private fun setupClickListeners() {
        binding.btnStartPause.setOnClickListener {
            if (isTimerRunning) {
                pauseTimer()
            } else {
                startTimer()
            }
        }

        binding.btnReset.setOnClickListener {
            resetTimer()
        }

        binding.btnSetCustom.setOnClickListener {
            showCustomTimeDialog()
        }

        binding.switchDnd.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                enableDoNotDisturb()
            } else {
                disableDoNotDisturb()
            }
        }
    }

    private fun setTimerDuration(durationInMillis: Long) {
        if (isTimerRunning) {
            Toast.makeText(this, getString(R.string.stop_timer_first), Toast.LENGTH_SHORT).show()
            return
        }
        timeLeftInMillis = durationInMillis
        totalTimeInMillis = durationInMillis
        updateTimerDisplay(durationInMillis)
    }

    private fun startTimer() {
        if (timeLeftInMillis <= 0) {
            Toast.makeText(this, getString(R.string.set_timer_duration_first), Toast.LENGTH_SHORT).show()
            return
        }

        sessionStartTime = System.currentTimeMillis()

        countDownTimer = object : CountDownTimer(timeLeftInMillis, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                timeLeftInMillis = millisUntilFinished
                updateTimerDisplay(millisUntilFinished)
                updateProgressBar()
            }

            override fun onFinish() {
                onTimerFinish()
            }
        }.start()

        isTimerRunning = true
        updateButtonStates()
        binding.tvStatus.text = getString(R.string.focus_session_active)
        binding.tvStatus.setTextColor(ContextCompat.getColor(this, R.color.success_green))
    }

    private fun pauseTimer() {
        countDownTimer?.cancel()
        isTimerRunning = false
        updateButtonStates()
        binding.tvStatus.text = getString(R.string.timer_paused)
        binding.tvStatus.setTextColor(ContextCompat.getColor(this, R.color.warning_orange))
    }

    private fun resetTimer() {
        countDownTimer?.cancel()
        isTimerRunning = false
        timeLeftInMillis = totalTimeInMillis
        updateTimerDisplay(timeLeftInMillis)
        updateProgressBar()
        updateButtonStates()
        binding.tvStatus.text = getString(R.string.ready_to_focus)
        binding.tvStatus.setTextColor(ContextCompat.getColor(this, R.color.grey))
    }

    private fun onTimerFinish() {
        isTimerRunning = false
        timeLeftInMillis = 0
        updateTimerDisplay(0)
        updateProgressBar()
        updateButtonStates()

        // Play completion sound
        playCompletionSound()

        // Save session to database
        saveFocusSession()

        // Show completion message
        binding.tvStatus.text = getString(R.string.focus_session_completed)
        binding.tvStatus.setTextColor(ContextCompat.getColor(this, R.color.success_green))

        Toast.makeText(this, getString(R.string.focus_session_completed_message), Toast.LENGTH_LONG).show()

        // Disable DND if enabled
        binding.switchDnd.isChecked = false
        disableDoNotDisturb()
    }

    private fun updateTimerDisplay(millisUntilFinished: Long) {
        val hours = TimeUnit.MILLISECONDS.toHours(millisUntilFinished)
        val minutes = TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished) % 60
        val seconds = TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) % 60

        val timeFormatted = if (hours > 0) {
            String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, seconds)
        } else {
            String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
        }

        binding.tvTimer.text = timeFormatted

        // Update title based on time left
        if (millisUntilFinished <= 0) {
            binding.tvTimerTitle.text = getString(R.string.times_up)
        } else {
            binding.tvTimerTitle.text = getString(R.string.focus_time_remaining)
        }
    }

    private fun updateProgressBar() {
        if (totalTimeInMillis > 0) {
            val progress = ((totalTimeInMillis - timeLeftInMillis) * 100 / totalTimeInMillis).toInt()
            binding.progressBar.progress = progress
        }
    }

    private fun updateButtonStates() {
        if (isTimerRunning) {
            binding.btnStartPause.text = getString(R.string.pause)
            binding.btnStartPause.setBackgroundColor(ContextCompat.getColor(this, R.color.warning_orange))
            binding.btnReset.isEnabled = false
            binding.btnSetCustom.isEnabled = false
        } else {
            binding.btnStartPause.text = getString(R.string.start)
            binding.btnStartPause.setBackgroundColor(ContextCompat.getColor(this, R.color.success_green))
            binding.btnReset.isEnabled = true
            binding.btnSetCustom.isEnabled = true
        }
    }

    private fun showCustomTimeDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_custom_time, null)
        val etHours = dialogView.findViewById<EditText>(R.id.etHours)
        val etMinutes = dialogView.findViewById<EditText>(R.id.etMinutes)

        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle(getString(R.string.set_custom_focus_time))
            .setView(dialogView)
            .setPositiveButton(getString(R.string.set)) { dialog, _ ->
                val hours = etHours.text.toString().toIntOrNull() ?: 0
                val minutes = etMinutes.text.toString().toIntOrNull() ?: 0

                val totalMinutes = (hours * 60) + minutes
                if (totalMinutes > 0) {
                    setTimerDuration(totalMinutes * 60 * 1000L)
                } else {
                    Toast.makeText(this, getString(R.string.please_enter_valid_time), Toast.LENGTH_SHORT).show()
                }
                dialog.dismiss()
            }
            .setNegativeButton(getString(R.string.cancel)) { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun enableDoNotDisturb() {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        Toast.makeText(this, getString(R.string.focus_mode_notifications_muted), Toast.LENGTH_SHORT).show()
    }

    private fun disableDoNotDisturb() {
        Toast.makeText(this, getString(R.string.focus_mode_notifications_restored), Toast.LENGTH_SHORT).show()
    }

    private fun playCompletionSound() {
        try {
            // First try to use the custom sound from raw folder
            mediaPlayer = MediaPlayer.create(this, R.raw.focus_complete)
            mediaPlayer?.start()
        } catch (e: Exception) {
            try {
                // Fallback to system notification sound
                val notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                val r = RingtoneManager.getRingtone(applicationContext, notification)
                r.play()
            } catch (e2: Exception) {
                // Final fallback to system beep
                val toneGen = ToneGenerator(AudioManager.STREAM_NOTIFICATION, 100)
                toneGen.startTone(ToneGenerator.TONE_PROP_BEEP)
            }
        }
    }

    private fun saveFocusSession() {
        val userId = AuthUtils.getCurrentUser()?.uid ?: return

        val session = FocusSession(
            duration = totalTimeInMillis,
            completedAt = Date(),
            userId = userId
        )

        FocusModeUtils.saveFocusSession(
            session = session,
            onSuccess = {
                // Session saved successfully
                Toast.makeText(this, getString(R.string.focus_session_saved), Toast.LENGTH_SHORT).show()
            },
            onError = { error ->
                Toast.makeText(this, getString(R.string.failed_to_save_session, error), Toast.LENGTH_SHORT).show()
            }
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        countDownTimer?.cancel()
        mediaPlayer?.release()
        // Ensure DND is disabled when activity is destroyed
        if (binding.switchDnd.isChecked) {
            disableDoNotDisturb()
        }
    }
}