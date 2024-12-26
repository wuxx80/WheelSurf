package com.mengpeng.wheelsurf

import android.graphics.BitmapFactory
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val prizesIcon = intArrayOf(
                R.mipmap.lottery1, R.mipmap.lottery2, R.mipmap.lottery3,
                R.mipmap.lottery4, R.mipmap.lottery_btn, R.mipmap.lottery5,
                R.mipmap.lottery6, R.mipmap.lottery2, R.mipmap.lottery9)

        val prizes = ArrayList<Prize>()

        for (x in 0..8) {
            val lottery = Prize()
            lottery.id = x + 1
            lottery.name = "Lottery" + (x + 1)
            val bitmap = BitmapFactory.decodeResource(resources, prizesIcon[x])
            lottery.icon = bitmap

            lottery.bgColor = BitmapFactory.decodeResource(resources, R.mipmap.bg)

            prizes.add(lottery)
        }
        nl.setPrizes(prizes)

        nl.setLottery(8)

        nl.setOnTransferWinningListener({ position ->
            Toast.makeText(applicationContext, prizes[position].name, Toast.LENGTH_SHORT).show()
        })
    }
}
