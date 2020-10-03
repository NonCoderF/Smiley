package com.multilevelcircularprogress.myapplication

import android.os.Build
import android.os.Bundle
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.warkiz.tickseekbar.OnSeekChangeListener
import com.warkiz.tickseekbar.SeekParams
import com.warkiz.tickseekbar.TickSeekBar
import kotlinx.android.synthetic.main.smiley_activity.*

class SmileyActivity : AppCompatActivity() {

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.smiley_activity)

        smiley.setStrokeColor(getColor(R.color.colorPrimaryDark))

        smiley.setToMood(50)
        seekBar.setProgress(50F)
        text.text = "Mood 50"


        seekBar.onSeekChangeListener = object : OnSeekChangeListener {
            override fun onSeeking(seekParams: SeekParams?) {
                seekParams?.progress?.let { smiley.setToMood(it) }
                text.text = "Mood " + seekParams?.progress;
            }

            override fun onStartTrackingTouch(seekBar: TickSeekBar?) {}

            override fun onStopTrackingTouch(seekBar: TickSeekBar?) {}
        };
    }
}