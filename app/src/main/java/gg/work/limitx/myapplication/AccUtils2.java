package gg.work.limitx.myapplication;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.util.Log;
import android.view.Surface;
import android.view.WindowManager;

import java.util.ArrayList;

/**
 * Created by minche_li on 2018/04
 */

public class AccUtils2 {
    private static final String tag = "AccUtils2";

    private SensorManager mSensorManager;
    private Sensor mSensor,mSensorLINEAR,mRotationSensor;

    // ACC algorithm
    private boolean detectStart;
    private static boolean sflag;
    private long time = 0;
    int[] prevXYZ,diffXYZ,sqrXYZ,filteredXYZ;
    private static final int time_interval = 150; //100ms
    private static final double upper_threshold = 18;// 9.8 + x
    private static final double lower_threshold = 5.8;// 9.8 -x

    //+
    private MotionListener mListener;
    public interface MotionListener {
        void  onMotionChanged(int type);
    }
    //-

    public AccUtils2(Context context, MotionListener listener){
        mListener = listener;

        mSensorManager = (SensorManager)context.getSystemService(Context.SENSOR_SERVICE);
        mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mSensorLINEAR = mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        mRotationSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
    }



    public void enableAccSensor(boolean enable) {
        detectStart = false;
        sflag = false;
        time = 0;
        prevXYZ = new int[3];
        diffXYZ = new int[3];
        sqrXYZ = new int[3];
        filteredXYZ = new int[3];

        synchronized (this) {
            if (enable) {
                /*mSensorManager.registerListener(mSensorListener, mSensor,
                        SensorManager.SENSOR_DELAY_UI);*/
                mSensorManager.registerListener(mSensorListener, mSensorLINEAR,
                        SensorManager.SENSOR_DELAY_UI);
                mSensorManager.registerListener(mSensorListener, mRotationSensor,
                        SensorManager.SENSOR_DELAY_UI);
            } else {
                mSensorManager.unregisterListener(mSensorListener);
            }
        }
    }

    SensorEventListener mSensorListener = new SensorEventListener() {
        public void onSensorChanged(SensorEvent event) {
            if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {

            } else if (event.sensor.getType() == Sensor.TYPE_LINEAR_ACCELERATION) {
                detectM(event);
            } else if (event.sensor.getType() == Sensor.TYPE_ROTATION_VECTOR) {
                updateOrientation(event.values);
            }
        }

        public void onAccuracyChanged(Sensor sensor, int accuracy) {
            // ignore
        }
    };

    private void onMotionChanged(boolean flag) {
        if(mListener != null) {
            mListener.onMotionChanged(flag? 1 :2);
        }
        time = System.currentTimeMillis();
        sflag = flag;
    }

    private void updateOrientation(float[] rotationVector) {
        float[] rotationMatrix = new float[9];
        SensorManager.getRotationMatrixFromVector(rotationMatrix, rotationVector);

        // Transform rotation matrix into azimuth/pitch/roll
        float[] orientation = new float[3];
        SensorManager.getOrientation(rotationMatrix, orientation);

        // Convert radians to degrees
        //float yaw = orientation[0] * -57;
        float pitch = orientation[1] * -57;
        float roll = orientation[2] * -57;

        if (Math.abs(pitch) <= 45 && Math.abs(pitch) <= 45) {
            if ( (filteredXYZ[2] > 4*filteredXYZ[0] && filteredXYZ[2] > 4*filteredXYZ[1] && filteredXYZ[2] >= 9)) {

            } /*else if(threshold > upper_threshold || threshold < lower_threshold) {
                if (!sflag && System.currentTimeMillis() - time > time_interval) {
                    onMotionChanged(true);
                    //Log.i(tag, "detectOriention+threshold  : " + threshold + " = " + prevXYZ[0] + " " + prevXYZ[1] + " " + prevXYZ[2]);
                    //Log.i(tag, "detectOriention+threshold  " + sflag + " : " + filteredXYZ[0] + " " + filteredXYZ[1] + " " + filteredXYZ[2]);
                }
            }*/

            if (filteredXYZ[0]+filteredXYZ[1]+filteredXYZ[2] != 0) {
                Log.i(tag, "detectM+  " + filteredXYZ[0] +" "+ filteredXYZ[1] +" "+ filteredXYZ[2]);
            }
        }

        /*if (filteredXYZ[0]+filteredXYZ[1]+filteredXYZ[2] != 0) {
            Log.i(tag, "updateOrientation  "+" / "+pitch+" "+roll);
        }*/
    }

    private void detectM(SensorEvent event) {
        int[] xyz = new int[3];
        xyz[0] = (int) (event.values[0] * 4);
        xyz[1] = (int) (event.values[1] * 4);
        xyz[2] = (int) (event.values[2] * 4);

        if (!detectStart) {
            detectStart = true;
            time = System.currentTimeMillis();
            prevXYZ[0] = (int) (event.values[0]);
            prevXYZ[1] = (int) (event.values[1]);
            prevXYZ[2] = (int) (event.values[2]);
        } else {
            diffXYZ[0] = xyz[0] - prevXYZ[0];
            diffXYZ[1] = xyz[1] - prevXYZ[1];
            diffXYZ[2] = xyz[2] - prevXYZ[2];

            sqrXYZ[0] = diffXYZ[0] * diffXYZ[0];
            sqrXYZ[1] = diffXYZ[1] * diffXYZ[1];
            sqrXYZ[2] = diffXYZ[2] * diffXYZ[2];

            filteredXYZ[0] = sqrXYZ[0];
            filteredXYZ[1] = sqrXYZ[1];
            filteredXYZ[2] = sqrXYZ[2];
            ////Log.i(tag, "detectM+  " + filteredXYZ[0] +" "+ filteredXYZ[1] +" "+ filteredXYZ[2]);
        }
    }
}