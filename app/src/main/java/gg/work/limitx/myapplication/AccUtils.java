package gg.work.limitx.myapplication;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.util.Log;

import java.util.ArrayList;

public class AccUtils {

    private static final String tag = "AccUtils";

    // ACC algorithm
    private boolean detectStart;
    private static boolean sflag;
    private long time = 0;

    private SensorManager mSensorManager;
    private Sensor mSensor;

    kalmanFilterx k1,k2,k3;

    int[] prevXYZ,diffXYZ,sqrXYZ,filteredXYZ;

    private Handler mHandler;

    private MotionListener mListener;

    public interface MotionListener {
        void  onMotionChanged(int type);
    }

    public AccUtils(Context context, MotionListener listener){
        mListener = listener;

        mSensorManager = (SensorManager)context.getSystemService(Context.SENSOR_SERVICE);
        mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
    }

    public AccUtils(Context context, Handler handler) {
        mHandler = handler;

        mSensorManager = (SensorManager)context.getSystemService(Context.SENSOR_SERVICE);
        mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
    }

    public void enableAccSensor(boolean enable) {
        detectStart = false;
        sflag = false;
        time = 0;
        prevXYZ = new int[3];
        diffXYZ = new int[3];
        sqrXYZ = new int[3];
        filteredXYZ = new int[3];
        k1 = new kalmanFilterx();
        k2 = new kalmanFilterx();
        k3 = new kalmanFilterx();

        synchronized (this) {
            if (enable) {
                mSensorManager.registerListener(mSensorListener, mSensor,
                        SensorManager.SENSOR_DELAY_UI);
            } else {
                mSensorManager.unregisterListener(mSensorListener);
            }
        }
    }

    SensorEventListener mSensorListener = new SensorEventListener() {
        public void onSensorChanged(SensorEvent event) {
            if (event.sensor.getType() == Sensor.TYPE_LINEAR_ACCELERATION) {
                detectPulse(event);
            }
        }

        public void onAccuracyChanged(Sensor sensor, int accuracy) {
            // ignore
        }
    };

    private void detectPulse(SensorEvent event) {
        int[] xyz = new int[3];
        xyz[0] = (int)(event.values[0]*2);
        xyz[1] = (int)(event.values[1]*2);
        xyz[2] = (int)(event.values[2]*2);

        Log.i(tag, "detectPulse raw " + xyz[0]+" "+xyz[1]+" "+xyz[2]);

        if (!detectStart) {
            detectStart = true;
            time = System.currentTimeMillis();
            prevXYZ[0] = (int)(event.values[0]);
            prevXYZ[1] = (int)(event.values[1]);
            prevXYZ[2] = (int)(event.values[2]);
        } else {
            diffXYZ[0] = xyz[0] - prevXYZ[0];
            diffXYZ[1] = xyz[1] - prevXYZ[1];
            diffXYZ[2] = xyz[2] - prevXYZ[2];

            sqrXYZ[0] = diffXYZ[0] * diffXYZ[0];
            sqrXYZ[1] = diffXYZ[1] * diffXYZ[1];
            sqrXYZ[2] = diffXYZ[2] * diffXYZ[2];

            /*filteredXYZ[0] = k1.kalmanFilter(sqrXYZ[0]);
            filteredXYZ[1] = k2.kalmanFilter(sqrXYZ[1]);
            filteredXYZ[2] = k3.kalmanFilter(sqrXYZ[2]);*/
            filteredXYZ[0] = sqrXYZ[0];
            filteredXYZ[1] = sqrXYZ[1];
            filteredXYZ[2] = sqrXYZ[2];

            // xyz=0, y>x z=0 , x>z y=0
            if ((filteredXYZ[0]+filteredXYZ[1]+filteredXYZ[2] == 0) ||
                    (filteredXYZ[0] > filteredXYZ[2] && filteredXYZ[1] < 2) ||
                    (filteredXYZ[1] > filteredXYZ[0] && filteredXYZ[2] < 2) ||
                    (filteredXYZ[2] > filteredXYZ[0] && filteredXYZ[1] < 2) ||
                    (filteredXYZ[1] < 2 && filteredXYZ[0] > 2 && filteredXYZ[2] > 2)
                    ) {
                if (sflag && System.currentTimeMillis() - time > 300) {
                    onMotionChanged(false);
                }
            }
            // z>x y=0
            else if ((filteredXYZ[0] > 4 && (filteredXYZ[1]+filteredXYZ[2] > 3) && (filteredXYZ[1] > 0 || filteredXYZ[2] > 0))  ||
                    (filteredXYZ[1] > 4 && (filteredXYZ[0]+filteredXYZ[2] > 3) && (filteredXYZ[0] > 0 || filteredXYZ[2] > 0)) ||
                    (filteredXYZ[2] > 10 && (prevXYZ[0]+prevXYZ[1]+prevXYZ[2] < 5 && (filteredXYZ[1] > 0 || filteredXYZ[2] > 0)))
                    ) {

                if (!sflag && System.currentTimeMillis() - time > 300) {
                    onMotionChanged(true);
                }
            } else if (sflag && System.currentTimeMillis() - time > 300) {//if (filteredXYZ[0] < 10 && filteredXYZ[2] < 10) {
                onMotionChanged(false);
            }

            prevXYZ[0] = xyz[0];
            prevXYZ[1] = xyz[1];
            prevXYZ[2] = xyz[2];

            Log.i(tag, "detectPulse++++ " + sflag + " : " +
                    sqrXYZ[0] + " / " + filteredXYZ[0] + " , " +
                    sqrXYZ[1] + " / " + filteredXYZ[1] + " , " +
                    sqrXYZ[2] + " / " + filteredXYZ[2]);
        }
    }

    private void onMotionChanged(boolean flag) {
        if(mListener != null) {
            mListener.onMotionChanged(flag? 1 :2);
        }
        time = System.currentTimeMillis();
        sflag = flag;
    }

    private class kalmanFilterx {
        double prevData = 0, p = 10, q = 2, r = 1.5, kGain = 0;

        int kalmanFilter(int inData) {
            p += q;
            kGain = p / (p + r);
            inData = (int) (prevData + (kGain * (((double) (inData) - prevData))));
            p *= (1 - kGain);
            prevData = inData;
            return inData;
        }
    }
}