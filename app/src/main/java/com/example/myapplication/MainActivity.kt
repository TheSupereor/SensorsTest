package com.example.myapplication

import android.graphics.Color
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate

class MainActivity : AppCompatActivity(), SensorEventListener {

    private lateinit var sensorManager: SensorManager
    private lateinit var square: TextView
    private lateinit var square2: TextView
    private lateinit var square3: TextView
    private var brightness: Sensor? = null
    private var prox: Sensor? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Fica em night mode
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        square = findViewById(R.id.square)
        square2 = findViewById(R.id.square2)
        square3 = findViewById(R.id.square3)

        setUpSensor()
    }

    private fun setUpSensor() {
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager

        // sensor acelerometro mais rápido, com detalhes mais recentes
        sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)?.also{
            sensorManager.registerListener(
                this,
                it,
                SensorManager.SENSOR_DELAY_FASTEST,
                SensorManager.SENSOR_DELAY_FASTEST)
        }

        brightness = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT)
        prox = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY)
        if (prox == null) {
            Toast.makeText(this, "No proximity sensor found in device..", Toast.LENGTH_SHORT).show()
            finish()
        } else {
            sensorManager.registerListener(
                this,
                prox,
                SensorManager.SENSOR_DELAY_NORMAL
            )
        }
    }

    override fun onSensorChanged(p0: SensorEvent?) {
        if(p0?.sensor?.type == Sensor.TYPE_ACCELEROMETER){
            val sides = p0.values[0]
            val upDown = p0.values[1]

            square.apply {
                rotationX = upDown * 3f
                rotationY = sides * 3f
                rotation = -sides
                translationX = sides * -10
                translationY = upDown * 10
            }

            square.text = "up/down ${upDown.toInt()}\nleft/right ${sides.toInt()}"
        }

        if(p0?.sensor?.type == Sensor.TYPE_LIGHT){
            val light = p0.values[0]

            Log.d("LUZ", "$light + ${brightness(light)}")
            val color = Color.BLACK
            if(light.toInt() == 0) Color.BLACK
            if (light.toInt() in 1..49) Color.GRAY
            if (light.toInt() in 50..5000) Color.LTGRAY
            if (light.toInt() in 5001..25000) Color.WHITE

            square2.setBackgroundColor(color)
            square2.text = "brightness: $light\nColor: ${brightness(light)}"
        }

        if (p0?.sensor?.type == Sensor.TYPE_PROXIMITY) {
            if (p0.values?.get(0) == 0f) {
                square3.text = "Objeto próximo ao sensor"
            } else {
                square3.text = "Objecto longe do sensor"
            }
        }
    }

    override fun onAccuracyChanged(p0: Sensor?, p1: Int) {
        return
    }

    private fun brightness(brightness: Float) : String {
        return when(brightness.toInt()){
            0 -> "Pitch Black"
            in 1..10 -> "Dark"
            in 11..50 -> "Grey"
            in 51..5000 -> "Normal"
            in 5001..25000 -> "Cruel Sun"
            else -> "Are you in the sun?"
        }
    }

    override fun onResume() {
        super.onResume()
        sensorManager.registerListener(
            this,
            brightness,
            SensorManager.SENSOR_DELAY_NORMAL
        )
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
    }

    override fun onDestroy() {
        sensorManager.unregisterListener(this)
        super.onDestroy()
    }
}