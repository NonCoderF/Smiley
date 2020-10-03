package com.multilevelcircularprogress.myapplication

import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import kotlinx.android.synthetic.main.activity_main.*
import kotlin.random.Random

val c1 = CircleProgress.CircleObject()
val c2 = CircleProgress.CircleObject()

class MainActivity : AppCompatActivity() {

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        circleProgress.textVisible = true
        circleProgress.fontFamily = ResourcesCompat.getFont(this, R.font.thin)
        circleProgress.startAngle = -90F
        circleProgress.circleGap = 20F

        c1.barWidth = 25F
        c1.totalAmount = 500F
        c1.amountValue = 236.6F
        c1.prefix = "%"
        c1.barColor = Color.parseColor("#FF0044")
        c1.backgroundColor = Color.parseColor("#33FF0044")
        c1.animDuration = 1500

        c2.barWidth = 20F
        c2.amountValue = 50F
        c2.totalAmount = 100F
        c2.suffix = "$"

        c1.observer = { state, value ->
            textView1.text = value.toString()
            Unit
        }

        c2.observer = { state, value ->
            textView2.text = value.toString()
            Unit
        }

        circleProgress.addCircle(c1)
        circleProgress.addCircle(c2)

        circleProgress.render()
    }

    fun animateC11(view: View) {
        c1.amountValue += 40F
        c1.update()
    }

    fun animateC22(view: View) {
        c2.amountValue -= 10F
        c2.update()
    }

    fun animateC21(view: View) {
        c2.amountValue += 10F
        c2.update()
    }

    fun animateC12(view: View) {
        c1.amountValue -= 40F
        c1.update()
    }

    fun testing(view: View) {
        circleProgress.textVisible = Random(100).nextBoolean()
        circleProgress.fontFamily = ResourcesCompat.getFont(this, R.font.thin)
        circleProgress.startAngle = (-2600..2600).random().toFloat()
        circleProgress.circleGap = (-50..50).random().toFloat()

        c1.barWidth = (-100..100).random().toFloat()
        c1.amountValue = (-1000..1000).random().toFloat()
        c1.totalAmount = (-1000..1000).random().toFloat()

        c2.barWidth = (-100..100).random().toFloat()
        c2.amountValue = (-1000..1000).random().toFloat()
        c2.totalAmount = (-1000..1000).random().toFloat()

        c1.update()
        c2.update()

        c1.observer = { state, value ->
            textView1.text = value.toString()
            Unit
        }

        c2.observer = { state, value ->
            textView2.text = value.toString()
            Unit
        }

    }

    fun smileyActivity(view: View) {
        startActivity(Intent(this, SmileyActivity::class.java))
    }
}