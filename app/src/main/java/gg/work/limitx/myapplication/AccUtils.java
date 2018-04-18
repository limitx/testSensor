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
    private Sensor mSensor,mSensorR;

    //kalmanFilterx k1,k2,k3;
    private ArrayList filterX;
    private ArrayList filterY;
    private ArrayList filterZ;

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
        mSensorR = mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
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
        /*k1 = new kalmanFilterx();
        k2 = new kalmanFilterx();
        k3 = new kalmanFilterx();*/
        filterX = new ArrayList();
        filterY = new ArrayList();
        filterZ = new ArrayList();

        synchronized (this) {
            if (enable) {
                mSensorManager.registerListener(mSensorListener, mSensor,
                        SensorManager.SENSOR_DELAY_UI);
                mSensorManager.registerListener(mSensorListener, mSensorR,
                        SensorManager.SENSOR_DELAY_NORMAL);
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

        //Log.i(tag, "detectPulse raw " + xyz[0]+" "+xyz[1]+" "+xyz[2]);

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

            if (filterX.size() < 3) {
                filterX.add(xyz[0]);
                filterY.add(xyz[1]);
                filterZ.add(xyz[2]);
            } else {
                //Log.i(tag, "detectPulse+ "+filter.size() +" "+sqrXYZ[0]+" / "+filter.toString());
                filterX.remove(0);
                filterX.add(xyz[0]);
                filterY.remove(0);
                filterY.add(xyz[1]);
                filterZ.remove(0);
                filterZ.add(xyz[2]);
            }

            int[] sumXYZ = new int[3];
            for (int i = 1; i < filterX.size(); i++) {
                sumXYZ[0] += Math.abs((int)filterX.get(i));
                sumXYZ[1] += Math.abs((int)filterY.get(i));
                sumXYZ[2] += Math.abs((int)filterZ.get(i));
            }

            filteredXYZ[0] = sqrXYZ[0];
            filteredXYZ[1] = sqrXYZ[1];
            filteredXYZ[2] = sqrXYZ[2];


            if ((filteredXYZ[0]+filteredXYZ[1]+filteredXYZ[2] == 0) ||
                    (filteredXYZ[0] > filteredXYZ[2] && filteredXYZ[1] < 5) ||
                    (filteredXYZ[1] > filteredXYZ[0] && filteredXYZ[2] < 2) ||
                    (filteredXYZ[2] > filteredXYZ[0] && filteredXYZ[1] < 2) ||
                    (filteredXYZ[2] > 15 && filteredXYZ[2] < 100 && filteredXYZ[1] < 2 && filteredXYZ[1] < 2) ||
                    (filteredXYZ[1] < 2 && filteredXYZ[0] > 2 && filteredXYZ[2] > 2)
                    ) {
                if (sflag && System.currentTimeMillis() - time > 200) {
                    onMotionChanged(false);
                }
            } else if (
                    /*(filteredXYZ[1] == 0 && sumXYZ[0] < 6 && sumXYZ[2] > 10 && sumXYZ[0] > sumXYZ[1]) ||
                    (xyz[1] < 0 && sumXYZ[0] == 0 && sumXYZ[1] == 0 && sumXYZ[2] > 8) ||
                    (xyz[1] < 0 && sumXYZ[0] == 1 && sumXYZ[1] == 1 && sumXYZ[2] > 20)*/
                    //
                    //++++
                    (filteredXYZ[0] > 4 && (filteredXYZ[1]+filteredXYZ[2] > 3) && (filteredXYZ[1] > 0 || filteredXYZ[2] > 0))  ||
                    (filteredXYZ[1] > 4 && (filteredXYZ[0]+filteredXYZ[2] > 3) && (filteredXYZ[0] > 0 || filteredXYZ[2] > 0)) ||
                    (filteredXYZ[2] > 10 && (prevXYZ[0]+prevXYZ[1]+prevXYZ[2] < 5 && (filteredXYZ[1] > 0 || filteredXYZ[2] > 0)))
                    ) {

                if (!sflag && System.currentTimeMillis() - time > 400) {
                    onMotionChanged(true);
                    Log.i(tag, "dPulse+ " + xyz[0]+" "+xyz[1]+" "+xyz[2] +" ,"+filteredXYZ[0]+","+filteredXYZ[1]+","+filteredXYZ[2]);
                }
            } else if (sflag && System.currentTimeMillis() - time > 200) {//if (filteredXYZ[0] < 10 && filteredXYZ[2] < 10) {
                onMotionChanged(false);
            }

            prevXYZ[0] = xyz[0];
            prevXYZ[1] = xyz[1];
            prevXYZ[2] = xyz[2];

            if ((filteredXYZ[0]+filteredXYZ[1]+filteredXYZ[2] != 0)) {
                Log.i(tag, "dPulse0 "+sflag +" "+ xyz[0] + " " + xyz[1] + " " + xyz[2] +
                        " / " + filteredXYZ[0] + "," + filteredXYZ[1] + "," + filteredXYZ[2] +
                        " / " + sumXYZ[0] + "," + sumXYZ[1] + "," + sumXYZ[2]);
            }
            /*if ((filteredXYZ[0]+filteredXYZ[1]+filteredXYZ[2] != 0)) {
                Log.i(tag, "dPulse0 "+sflag + xyz[0] + " " + xyz[1] + " " + xyz[2] +
                        " / " + filteredXYZ[0] + "," + filteredXYZ[1] + "," + filteredXYZ[2]);
            }*/
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