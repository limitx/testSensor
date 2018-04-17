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

    private Handler mHandler;

    private MotionListener mListener;

    public interface MotionListener {
        void  onMotionChanged(int type);
    }

    public AccUtils(Context context, MotionListener listener){
        mListener = listener;

        mSensorManager = (SensorManager)context.getSystemService(Context.SENSOR_SERVICE);
        mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
    }

    public AccUtils(Context context, Handler handler) {
        mHandler = handler;

        mSensorManager = (SensorManager)context.getSystemService(Context.SENSOR_SERVICE);
        mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
    }

    public void enableAccSensor(boolean enable) {
        detectStart = false;
        sflag = false;
        time = 0;
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
            if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                detectPulse(event);
            }
        }

        public void onAccuracyChanged(Sensor sensor, int accuracy) {
            // ignore
        }
    };

    public void detectPulse(SensorEvent event) {
        int[] XYZ = new int[3];
        int[] prevXYZ = new int[3];
        int[] sqrXYZ = new int[3];

        if (!detectStart) {
            detectStart = true;
            prevXYZ[0] = (int)(event.values[0]);
            prevXYZ[1] = (int)(event.values[1]);
            prevXYZ[2] = (int)(event.values[2]);
        } else {
            XYZ[0] = (int)(event.values[0]) - prevXYZ[0];
            XYZ[1] = (int)(event.values[0]) - prevXYZ[1];
            XYZ[2] = (int)(event.values[0]) - prevXYZ[2];

            sqrXYZ[0] = XYZ[0] * XYZ[0];
            sqrXYZ[1] = XYZ[1] * XYZ[1];
            sqrXYZ[2] = XYZ[2] * XYZ[2];

            XYZ[0] = k1.kalmanFilter(sqrXYZ[0]);
            XYZ[1] = k2.kalmanFilter(sqrXYZ[1]);
            XYZ[2] = k3.kalmanFilter(sqrXYZ[2]);


            if ((XYZ[0] > 4 && (XYZ[1]+XYZ[2] > 2)) ||
                    XYZ[1] > 4 || XYZ[2] > 4) {

                if (!sflag && System.currentTimeMillis() - time > 200) {
                    /*if (mHandler.obtainMessage(1) != null) {
                        mHandler.removeMessages(1);
                    }
                    mHandler.sendEmptyMessage(1);*/
                    if(mListener != null) {
                        mListener.onMotionChanged(1);
                    }

                    time = System.currentTimeMillis();
                    sflag = true;
                }
            } else if (sflag && System.currentTimeMillis() - time > 200) {
                /*if(mHandler.obtainMessage(2) != null) {
                    mHandler.removeMessages(2);
                }
                mHandler.sendEmptyMessage(2);*/
                if(mListener != null) {
                    mListener.onMotionChanged(2);
                }

                time = System.currentTimeMillis();
                sflag = false;
            }

            Log.i(tag, "detectPulse++++ "+sflag+" : "+
                    sqrXYZ[0]+" / "+XYZ[0] +" , "+
                    sqrXYZ[1]+" / "+XYZ[1]+" , "+
                    sqrXYZ[2]+" / "+XYZ[2]);
        }
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