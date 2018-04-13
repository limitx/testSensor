package gg.work.limitx.myapplication;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import java.util.List;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private final String tag = "MainActivity";
    private SensorManager mSensorManager;
    private Sensor mACC;
    private Sensor mGYR;
    private Sensor mROTATE;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        List<Sensor> deviceSensors = mSensorManager.getSensorList(Sensor.TYPE_ALL);
        mACC = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mGYR = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        mROTATE = mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);

    }

    @Override
    public final void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Do something here if sensor accuracy changes.
        Log.i(tag, "onAccuracyChanged");
    }

    @Override
    public final void onSensorChanged(SensorEvent event) {

        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            showSensorData(event);
        } else if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE) {
            showSensorData(event);
        } else if (event.sensor.getType() == Sensor.TYPE_ROTATION_VECTOR) {
            showSensorData(event);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mSensorManager.registerListener(this, mACC, SensorManager.SENSOR_DELAY_GAME);
        mSensorManager.registerListener(this, mGYR, SensorManager.SENSOR_DELAY_GAME);
        mSensorManager.registerListener(this, mROTATE, SensorManager.SENSOR_DELAY_GAME);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(this);
    }

    private void showSensorData(SensorEvent event) {
        float x = event.values[0];
        float y = event.values[1];
        float z = event.values[2];
        Log.i(tag, event.sensor.getType() +" xyz: " +x+" , "+y+" , "+z);
    }
}
