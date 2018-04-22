package gg.work.limitx.myapplication;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.util.Log;

import java.util.ArrayList;

/**
 * Created by minche_li on 2018/04
 */

public class AccUtils {
    private static final String tag = "AccUtils";

    // ACC algorithm
    private boolean detectStart;
    private static boolean sflag;
    private long time = 0;
    private ArrayList filterX;
    private ArrayList filterY;
    private ArrayList filterZ;
    int[] prevXYZ,diffXYZ,sqrXYZ,filteredXYZ;
    private static final int time_interval = 100; //100ms
    private static final double upper_threshold = 18;// 9.8 + x
    private static final double lower_threshold = 5;// 9.8 -x

    private SensorManager mSensorManager;
    private Sensor mSensor,mSensorLINEAR;

    private Handler mHandler;

    //+
    private MotionListener mListener;
    public interface MotionListener {
        void  onMotionChanged(int type);
    }
    //-

    public AccUtils(Context context, MotionListener listener){
        mListener = listener;

        mSensorManager = (SensorManager)context.getSystemService(Context.SENSOR_SERVICE);
        mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mSensorLINEAR = mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
    }

    public AccUtils(Context context, Handler handler) {
        mHandler = handler;

        mSensorManager = (SensorManager)context.getSystemService(Context.SENSOR_SERVICE);
        mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mSensorLINEAR = mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
    }

    public void enableAccSensor(boolean enable) {
        detectStart = false;
        sflag = false;
        time = 0;
        prevXYZ = new int[3];
        diffXYZ = new int[3];
        sqrXYZ = new int[3];
        filteredXYZ = new int[3];
        filterX = new ArrayList();
        filterY = new ArrayList();
        filterZ = new ArrayList();

        synchronized (this) {
            if (enable) {
                mSensorManager.registerListener(mSensorListener, mSensor,
                        SensorManager.SENSOR_DELAY_UI);
                mSensorManager.registerListener(mSensorListener, mSensorLINEAR,
                        SensorManager.SENSOR_DELAY_UI);
            } else {
                mSensorManager.unregisterListener(mSensorListener);
            }
        }
    }

    SensorEventListener mSensorListener = new SensorEventListener() {
        public void onSensorChanged(SensorEvent event) {
            if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                detectOrientation(event);
                /*if(mListener != null) {
                    mListener.onMotionChanged((int)(event.values[0]*4));
                }*/
            } else if (event.sensor.getType() == Sensor.TYPE_LINEAR_ACCELERATION) {
                ////detectPulse(event);
                detectM(event);
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

    static final float OneEightyOverPi = 57.29577957855f;
    private void detectOrientation(SensorEvent event) {
        float[] values = event.values;
        int[] orientationXYZ = new int[3];
        float X = -values[0];
        float Y = -values[1];
        float Z = -values[2];
        float magnitudeXY = X*X + Y*Y;
        float magnitudeXZ = X * X + Z * Z;
        float magnitudeYZ = Y * Y + Z * Z;
        double threshold = Math.sqrt((X * X + Y * Y + Z * Z));

        if (magnitudeXY * 4 >= Z * Z) {
            float angle = (float) Math.atan2(-Y, X) * OneEightyOverPi;
            orientationXYZ[0] = 90 - (int) Math.round(angle);
            // normalize to 0 - 359 range
            while (orientationXYZ[0] >= 360) {
                orientationXYZ[0] -= 360;
            }
            while (orientationXYZ[0] < 0) {
                orientationXYZ[0] += 360;
            }
        }

        if (magnitudeYZ * 4 >= X * X) {
            float angle = (float) Math.atan2(-Y, Z) * OneEightyOverPi;
            orientationXYZ[1] = 90 - (int) Math.round(angle);
            // normalize to 0 - 359 range
            while (orientationXYZ[1] >= 360) {
                orientationXYZ[1] -= 360;
            }
            while (orientationXYZ[1] < 0) {
                orientationXYZ[1] += 360;
            }
        }

        if (magnitudeXZ * 4 >= Y * Y) {
            float angle = (float) Math.atan2(-Y, Z) * OneEightyOverPi;
            orientationXYZ[2] = 90 - (int) Math.round(angle);
            // normalize to 0 - 359 range
            while (orientationXYZ[2] >= 360) {
                orientationXYZ[2] -= 360;
            }
            while (orientationXYZ[2] < 0) {
                orientationXYZ[2] += 360;
            }
        }

        //180 230 230
        //0 270 270
        //360  320 320
        //YZ 0~20 340~360
        //XY 45~135 220~320
        //XZ 45~135 220~320
        if (orientationXYZ[1] == orientationXYZ[2]) {
            if (orientationXYZ[0] == 0 &&
                    Math.abs(Z) > 8 && Math.abs(Y) < 3 && Math.abs(X) < 3 &&
                    (((orientationXYZ[1] < 315 && orientationXYZ[1] > 225) ||
                            (orientationXYZ[1] > 45 && orientationXYZ[1] < 135)) &&
                            (filteredXYZ[2] > 2*filteredXYZ[0] &&
                                    filteredXYZ[2] > 2*filteredXYZ[1] && filteredXYZ[2] >= 9))
                    ) {

                if (!sflag && System.currentTimeMillis() - time > time_interval) {
                    onMotionChanged(true);
                    //Log.i(tag, "detectOriention+ orientation  : "+orientationXYZ[0] +"  "+ orientationXYZ[1]);
                    //Log.i(tag, "detectOriention+  : "+threshold+" = "+ prevXYZ[0] +" "+ prevXYZ[1] +" "+ prevXYZ[2]);
                    //Log.i(tag, "detectOriention+  "+sflag+" : "+ filteredXYZ[0] +" "+ filteredXYZ[1] +" "+ filteredXYZ[2]);
                }
            } else if(threshold > upper_threshold || threshold < lower_threshold) {
                if (!sflag && System.currentTimeMillis() - time > time_interval) {
                    onMotionChanged(true);
                    Log.i(tag, "+threshold  : " + threshold + " = " + prevXYZ[0] + " " + prevXYZ[1] + " " + prevXYZ[2]);
                    //Log.i(tag, "detectOriention+threshold  " + sflag + " : " + filteredXYZ[0] + " " + filteredXYZ[1] + " " + filteredXYZ[2]);

                }
            } else {
                if (sflag && System.currentTimeMillis() - time > time_interval) {
                    onMotionChanged(false);
                }
            }
            if (filteredXYZ[0]+filteredXYZ[1]+filteredXYZ[2] != 0) {
                Log.i(tag, "detectOriention+  "+sflag+" : "+ filteredXYZ[0] +" "+ filteredXYZ[1] +" "+ filteredXYZ[2]);
                Log.i(tag, "detectOriention+  " + orientationXYZ[0] + " " + orientationXYZ[1] + " " + orientationXYZ[2]);
                Log.i(tag, "detectOriention+  " + X + " " + Y + " " + Z);
            }
        } else {
            if(threshold > upper_threshold || threshold < lower_threshold) {
                if (!sflag && System.currentTimeMillis() - time > time_interval) {
                    onMotionChanged(true);
                    Log.i(tag, "+threshold  : " + threshold + " = " + prevXYZ[0] + " " + prevXYZ[1] + " " + prevXYZ[2]);
                    //Log.i(tag, "detectOriention+threshold  " + sflag + " : " + filteredXYZ[0] + " " + filteredXYZ[1] + " " + filteredXYZ[2]);

                }
            }
            else if (sflag && System.currentTimeMillis() - time > time_interval) {
                onMotionChanged(false);
            }
            //Log.i(tag, "detectOriention-  ");
        }
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

    private void detectPulse(SensorEvent event) {
        int[] xyz = new int[3];
        xyz[0] = (int)(event.values[0]*4);
        xyz[1] = (int)(event.values[1]*4);
        xyz[2] = (int)(event.values[2]*4);

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

            if ((filteredXYZ[0]+filteredXYZ[1]+filteredXYZ[2] == 0)
                //++++
                    /*|| (filteredXYZ[0] > filteredXYZ[2] && filteredXYZ[1] < 5) ||
                    (filteredXYZ[1] > filteredXYZ[0] && filteredXYZ[2] < 2) ||
                    (filteredXYZ[2] > filteredXYZ[0] && filteredXYZ[1] < 2) ||
                    (filteredXYZ[0] > 15 && filteredXYZ[0] < 100 && filteredXYZ[1] < 2 && filteredXYZ[2] < 2) ||
                    (filteredXYZ[1] < 2 && filteredXYZ[0] > 2 && filteredXYZ[2] > 2)*/
                    ) {
                if (sflag && System.currentTimeMillis() - time > 200) {
                    onMotionChanged(false);
                }
            } else if (
                    /*(filteredXYZ[1] == 0 && sumXYZ[0] < 6 && sumXYZ[2] > 10 && sumXYZ[0] > sumXYZ[1]) ||
                    (xyz[1] < 0 && sumXYZ[0] == 0 && sumXYZ[1] == 0 && sumXYZ[2] > 8) ||
                    (xyz[1] < 0 && sumXYZ[0] == 1 && sumXYZ[1] == 1 && sumXYZ[2] > 20)*/

                //++++DUT is flat on table and user grab it up.
                    ((xyz[2] > 4 && xyz[2] < 23) && (xyz[1] > -5 && xyz[1] < 2) && (xyz[0] > -9 && xyz[0] < 4) &&
                            (sumXYZ[2] > 3 ? ((sumXYZ[2] - sumXYZ[0]) > 4) : true) &&
                            (sumXYZ[2] > 3 ? ((sumXYZ[2] - sumXYZ[1]) > 4) : true) &&
                            (sumXYZ[2] > 4) && (sumXYZ[1] < 7 && sumXYZ[1] > -1) && (sumXYZ[0] > -1 && sumXYZ[0] < 7))
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
}