package gg.work.limitx.myapplication;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by minche_li on 2018/04
 */

public class AccUtils {
    private static final String tag = "AccUtils";

    // ACC algorithm
    private boolean detectStart;
    private static boolean sflag;
    private long time = 0, timeStationary;
    //int[] prevXYZ,diffXYZ,sqrXYZ,filteredXYZ;
    float[] prevXYZ,diffXYZ,sqrXYZ,filteredXYZ;
    private static final int time_interval = 150; //100ms
    private static final double upper_threshold = 18.8;// 9.8 + x
    private static final double lower_threshold = 2;// 9.8 -x
    private static final double other_threshold = 22;// 9.8 + x

    private ArrayList filterX;
    private ArrayList filterY;
    private ArrayList filterZ;

    private SensorManager mSensorManager;
    private Sensor mSensor,mSensorLINEAR, mSensorAnyMotion;
    private static final int ANY_MOTION = 65601; // HTC Gesture sensor
    private static final int stationary_time_interval = 10000; // 10 secs
    private static boolean anyMotionToRegisterSensor = false;

    private Handler mHandler;

    //+
    private static MotionListener mListener;
    public interface MotionListener {
        void  onMotionChanged(int type);
        void  DrawX(int data1,int data2, int data3);
    }
    //-

    /*
            filterX = new ArrayList();
            filterY = new ArrayList();
            filterZ = new ArrayList();

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
    */
    public AccUtils(Context context, MotionListener listener){
        mListener = listener;

        mSensorManager = (SensorManager)context.getSystemService(Context.SENSOR_SERVICE);
        mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mSensorLINEAR = mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);

        List<Sensor> mAvailableSensor = mSensorManager.getSensorList(Sensor.TYPE_ALL);
        for (int i = 0; i < mAvailableSensor.size(); ++i) {
            if ((mAvailableSensor.get(i).getName()).equals("HTC Gesture sensor")) {
                mSensorAnyMotion = mAvailableSensor.get(i);
                break;
            }
        }
    }

    public void enableAccSensor(boolean enable) {
        if (sflag) {
            onMotionChanged(false);
        }

        detectStart = false;
        sflag = false;
        anyMotionToRegisterSensor = false;
        time = 0;
        timeStationary =  System.currentTimeMillis();
        /*prevXYZ = new int[3];
        diffXYZ = new int[3];
        sqrXYZ = new int[3];
        filteredXYZ = new int[3];*/

        prevXYZ = new float[3];
        diffXYZ = new float[3];
        sqrXYZ = new float[3];
        filteredXYZ = new float[3];

        filterX = new ArrayList();
        filterY = new ArrayList();
        filterZ = new ArrayList();


        synchronized (this) {
            if (enable) {
                mSensorManager.registerListener(mSensorListener, mSensor,
                        SensorManager.SENSOR_DELAY_UI);
                mSensorManager.registerListener(mSensorListener, mSensorLINEAR,
                        SensorManager.SENSOR_DELAY_UI);
                /*if (mSensorAnyMotion != null) {
                    mSensorManager.registerListener(mSensorListener, mSensorAnyMotion,
                            SensorManager.SENSOR_DELAY_UI);
                }*/
            } else {
                mSensorManager.unregisterListener(mSensorListener);
            }
        }
    }

    SensorEventListener mSensorListener = new SensorEventListener() {
        public void onSensorChanged(SensorEvent event) {
            if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                detectOrientation(event);
            } else if (event.sensor.getType() == Sensor.TYPE_LINEAR_ACCELERATION) {
                calculateAccVector(event);
            } else if (event.sensor.getType() == ANY_MOTION) {
                // Unregister ANY motion sensor. Register ACC & ACC linear sensors.
                if (anyMotionToRegisterSensor && mSensorAnyMotion != null) {
                    mSensorManager.unregisterListener(mSensorListener, mSensorAnyMotion);
                    enableAccSensor(true);
                    Log.i(tag, "ANY_MOTION+++");
                }
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

        if(mListener != null) {
            mListener.DrawX((int)(X+30),(int)(Y+30),(int)(Z+30));
        }

        if (sflag && System.currentTimeMillis() - time > time_interval) {
            onMotionChanged(false);
        }
        if (!anyMotionToRegisterSensor && filteredXYZ[0]+filteredXYZ[1]+filteredXYZ[2] == 0) {
            if(Math.abs(timeStationary - System.currentTimeMillis()) > stationary_time_interval) {
                // Unregister ACC & ACC linear sensors. Register ANY motion sensor.
                if (mSensorAnyMotion != null) {
                    enableAccSensor(false);
                    anyMotionToRegisterSensor = true;
                    mSensorManager.registerListener(mSensorListener, mSensorAnyMotion,
                            SensorManager.SENSOR_DELAY_UI);
                    //Log.i(tag, "ANY_MOTION---");
                }
                //Log.i(tag, "timeStationary  : "+timeStationary);
            }
            return;
        } else {
            timeStationary =  System.currentTimeMillis();
        }

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
            float angle = (float) Math.atan2(-X, Z) * OneEightyOverPi;
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

        // old Z>9 X<2 Y<2
        if (((Math.abs(360 - orientationXYZ[0]) > 340 || Math.abs(360 - orientationXYZ[0]) < 20) ||
                (Math.abs(270 - orientationXYZ[0]) > 250 && Math.abs(270 - orientationXYZ[0]) < 290)) &&
                (Math.abs(orientationXYZ[1]-orientationXYZ[2]) < 45)) {
            if ((Math.abs(Z) > 7) &&
                    (((orientationXYZ[1] < 315 && orientationXYZ[1] > 225) ||
                            (orientationXYZ[1] > 45 && orientationXYZ[1] < 135)) &&
                            (Math.abs(filteredXYZ[0])+Math.abs(filteredXYZ[1]) < Math.abs(filteredXYZ[2]) &&
                                    (Math.abs(filteredXYZ[2]) >= 1.5)))
                    ) {
                if (!sflag && System.currentTimeMillis() - time > time_interval) {
                    onMotionChanged(true);
                    //Log.i(tag, "detectOriention+ orientation  : "+orientationXYZ[0] +"  "+ orientationXYZ[1]);
                    //Log.i(tag, "detectOriention+  : "+threshold+" = "+ prevXYZ[0] +" "+ prevXYZ[1] +" "+ prevXYZ[2]);
                    //Log.i(tag, "detectOriention++  "+sflag+" : "+ filteredXYZ[0] +" "+ filteredXYZ[1] +" "+ filteredXYZ[2]);
                }
            } else if(threshold > upper_threshold || threshold < lower_threshold) {
                if (!sflag && System.currentTimeMillis() - time > time_interval) {
                    onMotionChanged(true);
                    //Log.i(tag, "+threshold  : " + threshold + " = " + prevXYZ[0] + " " + prevXYZ[1] + " " + prevXYZ[2]);
                    //Log.i(tag, "detectOriention+threshold+  " + sflag + " : " + filteredXYZ[0] + " " + filteredXYZ[1] + " " + filteredXYZ[2]);
                }
            } else {
                if (sflag && System.currentTimeMillis() - time > time_interval) {
                    onMotionChanged(false);
                    //Log.i(tag, "detectOriention-  ");
                }
            }

         if ((int)(filteredXYZ[0]+filteredXYZ[1]+filteredXYZ[2]) != 0) {
            Log.i(tag, "detect+  "+sflag+" : "+ filteredXYZ[0] +" "+ filteredXYZ[1] +" "+ filteredXYZ[2] +" threshold: "+ threshold);
            //Log.i(tag, "detectOriention+  " + orientationXYZ[0] + " " + orientationXYZ[1] + " " + orientationXYZ[2]);
            //Log.i(tag, "detect xyz+  " + X + " " + Y + " " + Z);
         }

        } else {
            if(threshold > upper_threshold || threshold < lower_threshold) {
                if (!sflag && System.currentTimeMillis() - time > time_interval) {
                    onMotionChanged(true);
                    //Log.i(tag, "+threshold  : " + threshold + " = " + prevXYZ[0] + " " + prevXYZ[1] + " " + prevXYZ[2]);
                    //Log.i(tag, "detectOriention+threshold++  " + sflag + " : " + filteredXYZ[0] + " " + filteredXYZ[1] + " " + filteredXYZ[2]);

                }
            } else if (sflag && System.currentTimeMillis() - time > time_interval) {
                onMotionChanged(false);
                //Log.i(tag, "detectOriention--  ");
            }
        }

        /*if (filteredXYZ[0]+filteredXYZ[1]+filteredXYZ[2] != 0) {
            Log.i(tag, "detect+  "+sflag+" : "+ filteredXYZ[0] +" "+ filteredXYZ[1] +" "+ filteredXYZ[2] +" threshold: "+ threshold);
            Log.i(tag, "detectOriention+  " + orientationXYZ[0] + " " + orientationXYZ[1] + " " + orientationXYZ[2]);
            Log.i(tag, "detect xyz+  " + X + " " + Y + " " + Z);
        }*/
    }

    private void calculateAccVector(SensorEvent event) {
        /*int[] xyz = new int[3];
        xyz[0] = (int) (event.values[0] * 4);
        xyz[1] = (int) (event.values[1] * 4);
        xyz[2] = (int) (event.values[2] * 4);*/

        float[] xyz = new float[3];
        xyz[0] = event.values[0];
        xyz[1] = event.values[1];
        xyz[2] = event.values[2];


        if (!detectStart) {
            detectStart = true;
            time = System.currentTimeMillis();
            /*prevXYZ[0] = (int) (event.values[0]);
            prevXYZ[1] = (int) (event.values[1]);
            prevXYZ[2] = (int) (event.values[2]);*/

            prevXYZ[0] = xyz[0];
            prevXYZ[1] = xyz[1];
            prevXYZ[2] = xyz[2];
        } else {
            diffXYZ[0] = xyz[0] - prevXYZ[0];
            diffXYZ[1] = xyz[1] - prevXYZ[1];
            diffXYZ[2] = xyz[2] - prevXYZ[2];

            sqrXYZ[0] = diffXYZ[0] * diffXYZ[0];
            sqrXYZ[1] = diffXYZ[1] * diffXYZ[1];
            sqrXYZ[2] = diffXYZ[2] * diffXYZ[2];

            if (filterX.size() < 12) {
                /*filterX.add(sqrXYZ[0]);
                filterY.add(sqrXYZ[1]);
                filterZ.add(sqrXYZ[2]);*/

                filterX.add(xyz[0]);
                filterY.add(xyz[1]);
                filterZ.add(xyz[2]);
            } else {
                //Log.i(tag, "detectPulse+ "+filter.size() +" "+sqrXYZ[0]+" / "+filter.toString());
                filterX.remove(0);
                filterY.remove(0);
                filterZ.remove(0);
                /*filterX.add(sqrXYZ[0]);
                filterY.add(sqrXYZ[1]);
                filterZ.add(sqrXYZ[2]);*/
                filterX.add(xyz[0]);
                filterY.add(xyz[1]);
                filterZ.add(xyz[2]);
            }

            //int[] sumXYZ = new int[4];
            float[] sumXYZ = new float[12];
            for (int i = 1; i < filterX.size(); i++) {
                /*sumXYZ[0] += Math.abs((float)filterX.get(i));
                sumXYZ[1] += Math.abs((float)filterY.get(i));
                sumXYZ[2] += Math.abs((float)filterZ.get(i));*/
                sumXYZ[0] += (float)(filterX.get(i));
                sumXYZ[1] += (float)filterY.get(i);
                sumXYZ[2] += (float)filterZ.get(i);
            }

            filteredXYZ[0] = sumXYZ[0]/filterX.size();//sqrXYZ[0];
            filteredXYZ[1] = sumXYZ[1]/filterX.size();//sqrXYZ[1];
            filteredXYZ[2] = sumXYZ[2]/filterX.size();//sqrXYZ[2];
            ////Log.i(tag, "detectM+  " + filteredXYZ[0] +" "+ filteredXYZ[1] +" "+ filteredXYZ[2]);
        }
    }
}