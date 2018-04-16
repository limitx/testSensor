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

    private ArrayList filterX;
    private ArrayList filterY;
    private ArrayList filterZ;
    private static final int filter_size = 5;
    private static final int filter_d_size = filter_size-1;

    private SensorManager mSensorManager;
    private Sensor mSensor;

    private Handler mHandler;

    private MotionListener mListener;
    public interface MotionListener {
        public void  onMotionChanged(int type);
    }


    public void AccUtils(Context context, MotionListener listener){ //Handler handler ) {
        mListener = listener;
        //mHandler = handler;

        mSensorManager = (SensorManager)context.getSystemService(Context.SENSOR_SERVICE);
        mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
    }

    public void enableAccSensor(boolean enable) {
        detectStart = false;
        sflag = false;
        filterX = new ArrayList();
        filterY = new ArrayList();
        filterZ = new ArrayList();

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

    long time = 0;
    public void detectPulse(SensorEvent event) {
        int[] XYZ = new int[3];
        int[] prevXYZ = new int[3];
        int[] sqrXYZ = new int[3];
        int[] sumXYZ  = new int[3];
        boolean flag = false;

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

            if (filterX.size() < filter_size) {
                filterX.add(sqrXYZ[0]);
                filterY.add(sqrXYZ[1]);
                filterZ.add(sqrXYZ[2]);
            } else {
                //Log.i(tag, "detectPulse+ "+filter.size() +" "+sqrXYZ[0]+" / "+filter.toString());
                filterX.remove(0);
                filterX.add(sqrXYZ[0]);
                filterY.remove(1);
                filterY.add(sqrXYZ[1]);
                filterZ.remove(2);
                filterZ.add(sqrXYZ[2]);
            }

            for(int i = 1; i < filterX.size(); i++) {
                sumXYZ[0] += (int)filterX.get(i);
                sumXYZ[1] += (int)filterY.get(i);
                sumXYZ[2] += (int)filterZ.get(i);
            }

            XYZ[0] = sumXYZ[0] / filter_d_size; // divide by filter_d_size
            XYZ[1] = sumXYZ[1] / filter_d_size; // divide by filter_d_size
            XYZ[2] = sumXYZ[2] / filter_d_size; // divide by filter_d_size

            //Log.i(tag, "detectPulse+ " + filteredXYZ[0] +" / "+filteredXYZ[2]);

            if (Math.abs(XYZ[0] - sqrXYZ[0]) > 15 || Math.abs(XYZ[2] - sqrXYZ[2]) > 10) {
                if (System.currentTimeMillis() - time > 160) {
                    if (mHandler.obtainMessage(1) != null) {
                        mHandler.removeMessages(1);
                    }
                    mHandler.sendEmptyMessage(1);

                    //mListener.onMotionChanged(1);

                    time = System.currentTimeMillis();
                }
            } else {//if (filteredXYZ[0] < 10 && filteredXYZ[2] < 10) {
                if(mHandler.obtainMessage(2) != null) {
                    mHandler.removeMessages(2);
                }
                mHandler.sendEmptyMessage(2);

                //mListener.onMotionChanged(2);

                time = System.currentTimeMillis();
                flag = false;
            }

            Log.i(tag, "detectPulse++++ "+flag+" : "+
                    sqrXYZ[0]+" / "+XYZ[0] +" , "+
                    sqrXYZ[1]+" / "+XYZ[1]+" , "+
                    sqrXYZ[2]+" / "+XYZ[2]);
        }
    }
}