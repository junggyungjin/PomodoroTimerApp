package practice.aop.pomodorotimerapp

import android.media.SoundPool
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.widget.SeekBar
import android.widget.TextView
import practice.aop.pomodorotimerapp.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    lateinit var binding: ActivityMainBinding

    private val remainMinutesTextView: TextView by lazy {
        binding.remainMinutesTextView
    }

    private val remainSecondsTextView: TextView by lazy {
        binding.remainSecondsTextView
    }

    private val soundPool = SoundPool.Builder().build()

    private val seekBar: SeekBar by lazy {
        binding.seekBar
    }

    private var currentCountDownTimer: CountDownTimer? =
        null // 카운트다운타이머는 앱이 시작하자마자 생기는게 아니기때문에 초기값에 null
    private var tickingSoundId: Int? = null
    private var bellSoundId: Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        bindView()
        initSounds()
    }

    override fun onResume() {
        super.onResume()
        soundPool.autoResume()
    }

    override fun onPause() {
        super.onPause()
        soundPool.autoPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        soundPool.autoPause()
    }

    //Seekbar 작업
    private fun bindView() {
        seekBar.setOnSeekBarChangeListener(
            object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(
                    seekBar: SeekBar?,
                    progress: Int,
                    fromUser: Boolean,
                ) {
                    // 코드가 아니라 사용자가 실제로 건들였을때에만 업데이트를 해주는 분기처리
                    if (fromUser) {
                        updateRemainTimes(progress * 60 * 1000L)
                    }
                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) {
                    stopCountDown()
                }

                override fun onStopTrackingTouch(seekBar: SeekBar?) {
                    seekBar ?: return // null일경우에 카운트다운을 하지 않고 바로 빠져나와야함

                    if (seekBar.progress == 0) {
                        stopCountDown()
                    } else {
                        startCountDown()
                    }
                }
            }
        )
    }

    // 사운드작업
    private fun initSounds() {
        tickingSoundId = soundPool.load(this@MainActivity, R.raw.timer_ticking, 1)
        bellSoundId = soundPool.load(this@MainActivity, R.raw.timer_bell, 1)
    }

    // 코틀린식 함수만드는 방법
    private fun createCountDownTimer(initiallMills: Long) =
        object : CountDownTimer(initiallMills, 1000L) {
            override fun onTick(millisUntilFinished: Long) {
                updateRemainTimes(millisUntilFinished)
                updateSeekBar(millisUntilFinished)
            }

            override fun onFinish() {
                completeCountDown()
            }
        }

    private fun startCountDown() {
        currentCountDownTimer = createCountDownTimer(seekBar.progress * 60 * 1000L)

        currentCountDownTimer?.start()

        tickingSoundId?.let { soundId ->
            soundPool.play(soundId, 1F, 1F, 0, 0, 1F)
        }
    }

    private fun stopCountDown() {
        currentCountDownTimer?.cancel()
        currentCountDownTimer = null
        soundPool.autoPause()
    }

    private fun completeCountDown() {
        updateRemainTimes(0)
        updateSeekBar(0)

        soundPool.autoPause()
        bellSoundId?.let { soundId ->
            soundPool.play(soundId, 1F, 1F, 0, 0, 1F)
        }
    }

    private fun updateRemainTimes(remainMills: Long) {

        val remainSeconds = remainMills / 1000 // Millis를 1000으로 나누면 Seconds가 됨

        remainMinutesTextView.text = "%02d'".format(remainSeconds / 60) // -> 분
        remainSecondsTextView.text = "%02d".format(remainSeconds % 60) // -> 초
    }

    private fun updateSeekBar(remainMills: Long) {
        seekBar.progress = (remainMills / 1000 / 60).toInt() // 분으로 만들기
    }


}