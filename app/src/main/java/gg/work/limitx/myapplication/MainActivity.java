package gg.work.limitx.myapplication;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

/**
 * Created by minche_li on 2018/04
 */

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private final String tag = "MainActivity";

    // Sensors
    private SensorManager mSensorManager;
    private Sensor mACC;
    private Sensor mGYR;
    private Sensor mROTATE;

    // Canvas waveform plot
    private SurfaceView sfv;
    private SurfaceHolder sfh;
    private static final int offset1 = 500;
    private static final int offset2 = 800;
    private Paint mPaint1 = new Paint();
    private Paint mPaint2 = new Paint();
    private Paint mPaint3 = new Paint();
    private Paint mPaint4 = new Paint();
    private Paint mPaint5 = new Paint();
    private Paint mPaint6 = new Paint();
    private Paint mPaintBlack = new Paint();
    private Canvas canvas;
    int xx, oldY, oldY1, oldY2, oldY3, oldY4, oldY5;
    // Screen size for Canvas
    int Height, Width;


    TextView tv1;


    // for other Sensors
    final float alpha = (float) 0.8;
    float[] gravity = new float[3];
    float[] linear_acceleration = new float[3];


    // ACC algorithm
    private boolean detectStart = false;
    int[] xyz, prevXYZ, diffXYZ, sqrXYZ, filteredXYZ;
    private ArrayList filterX = new ArrayList();
    private ArrayList filterY = new ArrayList();
    private ArrayList filterZ = new ArrayList();
    private static final int filter_size = 4;
    private static final int filter_d_size = filter_size - 1;

    kalmanFilter k1,k2,k3;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        //List<Sensor> deviceSensors = mSensorManager.getSensorList(Sensor.TYPE_ALL);
        //mACC = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);//TYPE_LINEAR_ACCELERATION
        mACC = mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        mGYR = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        mROTATE = mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);


        // Canvas waveform plot
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        Height = (int) (metrics.heightPixels * 0.5);
        Width = (int) (metrics.widthPixels * 0.8);

        sfv = findViewById(R.id.sfv);
        sfh = sfv.getHolder();
        sfh.setFormat(PixelFormat.TRANSLUCENT);

        mPaint1.setColor(Color.GREEN);
        mPaint1.setStrokeWidth(3);
        mPaint2.setColor(Color.RED);
        mPaint2.setStrokeWidth(3);
        mPaint3.setColor(Color.BLUE);
        mPaint3.setStrokeWidth(3);
        mPaint4.setColor(Color.WHITE);
        mPaint4.setStrokeWidth(3);
        mPaint5.setColor(Color.YELLOW);
        mPaint5.setStrokeWidth(3);
        mPaint6.setColor(Color.CYAN);
        mPaint6.setStrokeWidth(2);
        mPaintBlack.setColor(Color.BLACK);


        tv1 = findViewById(R.id.textView);

        Button bt1 = findViewById(R.id.bt1);
        bt1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawThread();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        initParameter();
        mSensorManager.registerListener(this, mACC, SensorManager.SENSOR_DELAY_UI);//25Hz
        //mSensorManager.registerListener(this, mACC, SensorManager.SENSOR_DELAY_GAME);//100Hz

        /*mSensorManager.registerListener(this, mACC, SensorManager.SENSOR_DELAY_UI);
        mSensorManager.registerListener(this, mGYR, SensorManager.SENSOR_DELAY_GAME);
        mSensorManager.registerListener(this, mROTATE, SensorManager.SENSOR_DELAY_GAME);*/

        tv1.setText(mACC.getName() + "  width:" + Width);
    }

    @Override
    protected void onPause() {
        super.onPause();
        Thread.interrupted();
        mSensorManager.unregisterListener(this);
    }

    @Override
    public final void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Do something here if sensor accuracy changes.
        Log.i(tag, "onAccuracyChanged");
    }

    @Override
    public final void onSensorChanged(SensorEvent event) {
        //if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {TYPE_LINEAR_ACCELERATION
        if (event.sensor.getType() == Sensor.TYPE_LINEAR_ACCELERATION) {
            showSensorData(event);
            detectPulse();
        }/* else if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE) {
            showSensorData(event);
        } else if (event.sensor.getType() == Sensor.TYPE_ROTATION_VECTOR) {
            showSensorData(event);
        }*/
    }

    Toast toastMessage;
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    if (toastMessage != null) {
                        toastMessage.cancel();
                        toastMessage = null;
                    } else {
                        toastMessage = Toast.makeText(MainActivity.this,
                                "motion detected!!", Toast.LENGTH_SHORT);
                        toastMessage.show();
                    }

                    tv1.setText(filteredXYZ[0] + " / " + filteredXYZ[2]);
                    tv1.setTextColor(Color.RED);
                    Log.i(tag, "toastMessage++");
                    break;
                case 2:
                    if (toastMessage != null) {
                        toastMessage.cancel();
                        toastMessage = null;
                    }
                    tv1.setTextColor(Color.BLACK);
                    Log.i(tag, "toastMessage--");
                    break;
            }
        }
    };

    private void showSensorData(SensorEvent event) {
/*
        // In this example, alpha is calculated as t / (t + dT),
        // where t is the low-pass filter's time-constant and
        // dT is the event delivery rate.

        // Isolate the force of gravity with the low-pass filter.
        gravity[0] = alpha * gravity[0] + (1 - alpha) * event.values[0];
        gravity[1] = alpha * gravity[1] + (1 - alpha) * event.values[1];
        gravity[2] = alpha * gravity[2] + (1 - alpha) * event.values[2];

        // Remove the gravity contribution with the high-pass filter.
        linear_acceleration[0] = event.values[0] - gravity[0];
        linear_acceleration[1] = event.values[1] - gravity[1];
        linear_acceleration[2] = event.values[2] - gravity[2];
*/
        xyz[0] = (int) (event.values[0]);
        xyz[1] = (int) (event.values[1]);
        xyz[2] = (int) (event.values[2]);
        //Log.i(tag, "showSensorData+ "+ xyz.toString());
    }


    private void drawThread() {
        new Thread() {
            @Override
            public void run() {
                super.run();
                while (true) {
                    //detectPulse();
                    try {
                        drawAll();
                        Thread.sleep(10);
                    } catch (InterruptedException exception) {

                    }
                }
            }
        }.start();
    }

    private void drawAll() {
        xx++;
        canvas = sfh.lockCanvas(new Rect(xx - 1, 0, xx, Height));
        if (canvas != null) {
/*
            canvas.drawLine(xx - 1, oldY1, xx, offset1 - xyz[0], mPaint1);
            canvas.drawLine(xx - 1, oldY2, xx, offset1 - xyz[1], mPaint2);
            canvas.drawLine(xx - 1, oldY3, xx, offset1 - xyz[2], mPaint3);
            oldY1 = offset1 - xyz[0];
            oldY2 = offset1 - xyz[1];
            oldY3 = offset1 - xyz[2];
*/
            canvas.drawLine(xx - 1, oldY1, xx, offset1 - sqrXYZ[0], mPaint1);
            canvas.drawLine(xx - 1, oldY2, xx, offset1 - sqrXYZ[1], mPaint2);
            canvas.drawLine(xx - 1, oldY3, xx, offset1 - sqrXYZ[2], mPaint3);
            oldY1 = offset1 - sqrXYZ[0];
            oldY2 = offset1 - sqrXYZ[1];
            oldY3 = offset1 - sqrXYZ[2];

            //++++
            canvas.drawLine(xx - 1, oldY4, xx, offset2 - filteredXYZ[0], mPaint4);
            canvas.drawLine(xx - 1, oldY5, xx, offset2 - filteredXYZ[2], mPaint5);
            canvas.drawLine(0, offset2 - 50, xx, offset2 - 50, mPaint6);
            oldY4 = offset2 - filteredXYZ[0];
            oldY5 = offset2 - filteredXYZ[2];

            sfh.unlockCanvasAndPost(canvas);

            //++++Log.i(tag, "draw+ "+xx+"  "+sqrXYZ[0]+" "+sqrXYZ[1]+" "+sqrXYZ[2]);
        }

        if (xx >= Width) {
            xx = 0; //canvas.drawColor(Color.TRANSPARENT,Mode.CLEAR);
            canvas = sfh.lockCanvas(new Rect(0, 0, Width, Height));
            if (canvas != null) {
                canvas.drawRect(0, 0, Width, Height, mPaintBlack);
            }
            sfh.unlockCanvasAndPost(canvas);
        }
    }

    private void initParameter() {
        detectStart = false;
        xyz = new int[3];
        prevXYZ = new int[3];
        diffXYZ = new int[3];
        sqrXYZ = new int[3];
        filteredXYZ = new int[3];
        filterX = new ArrayList();
        filterY = new ArrayList();
        filterZ = new ArrayList();

        k1 = new kalmanFilter();
        k2 = new kalmanFilter();
        k3 = new kalmanFilter();

        xx = 1;
        oldY = 0;
        oldY1 = 0;
        oldY2 = 0;
        oldY3 = 0;
        oldY4 = 0;
        oldY5 = 0;
    }

    long time = 0;
    boolean sflag = false;

    private void detectPulse() {
        if (!detectStart) {
            detectStart = true;
            time = System.currentTimeMillis();
            prevXYZ[0] = xyz[0];
            prevXYZ[1] = xyz[1];
            prevXYZ[2] = xyz[2];
        } else {
            diffXYZ[0] = xyz[0] - prevXYZ[0];
            diffXYZ[1] = xyz[1] - prevXYZ[1];
            diffXYZ[2] = xyz[2] - prevXYZ[2];

            prevXYZ[0] = xyz[0];
            prevXYZ[1] = xyz[1];
            prevXYZ[2] = xyz[2];

            sqrXYZ[0] = diffXYZ[0] * diffXYZ[0];
            sqrXYZ[1] = diffXYZ[1] * diffXYZ[1];
            sqrXYZ[2] = diffXYZ[2] * diffXYZ[2];

            /*if (filterX.size() < filter_size) {
                filterX.add(sqrXYZ[0]);
                filterY.add(sqrXYZ[1]);
                filterZ.add(sqrXYZ[2]);
            } else {
                //Log.i(tag, "detectPulse+ "+filter.size() +" "+sqrXYZ[0]+" / "+filter.toString());
                filterX.remove(0);
                filterX.add(sqrXYZ[0]);
                filterY.remove(0);
                filterY.add(sqrXYZ[1]);
                filterZ.remove(0);
                filterZ.add(sqrXYZ[2]);
            }*/

            /*int[] sumXYZ = new int[3];
            for (int i = 1; i < filterX.size(); i++) {
                sumXYZ[0] += (int) filterX.get(i);
                sumXYZ[1] += (int) filterX.get(i);
                sumXYZ[2] += (int) filterZ.get(i);
            }

            filteredXYZ[0] = sumXYZ[0] / filter_d_size; // divide by filter_d_size
            filteredXYZ[1] = sumXYZ[1] / filter_d_size; // divide by filter_d_size
            filteredXYZ[2] = sumXYZ[2] / filter_d_size; // divide by filter_d_size*/

            filteredXYZ[0] = k1.kalmanFilter(sqrXYZ[0]);
            filteredXYZ[1] = k2.kalmanFilter(sqrXYZ[1]);
            filteredXYZ[2] = k3.kalmanFilter(sqrXYZ[2]);

            /*if ((filteredXYZ[0] > 9 || Math.abs(filteredXYZ[0] - sqrXYZ[0]) > 6) ||
                    Math.abs(filteredXYZ[1] - sqrXYZ[1]) > 10 ||
                    Math.abs(filteredXYZ[2] - sqrXYZ[2]) > 6) {*/
            if ((filteredXYZ[0] > 4 && (filteredXYZ[1]+filteredXYZ[2] > 2)) ||
                    filteredXYZ[1] > 4 || filteredXYZ[2] > 4) {

                if (!sflag && System.currentTimeMillis() - time > 200) {
                    if (mHandler.obtainMessage(1) != null) {
                        mHandler.removeMessages(1);
                    }
                    mHandler.sendEmptyMessage(1);
                    time = System.currentTimeMillis();
                    sflag = true;

                    /*Log.i(tag, "detectPulse++++ "+sflag+" : "+
                            sqrXYZ[0]+" / "+filteredXYZ[0] +" , "+
                            sqrXYZ[1]+" / "+filteredXYZ[1]+" , "+
                            sqrXYZ[2]+" / "+filteredXYZ[2]);*/
                }
            } else if (sflag && System.currentTimeMillis() - time > 200) {//if (filteredXYZ[0] < 10 && filteredXYZ[2] < 10) {
                if (mHandler.obtainMessage(2) != null) {
                    mHandler.removeMessages(2);
                }
                mHandler.sendEmptyMessage(2);
                time = System.currentTimeMillis();
                sflag = false;
            }

            Log.i(tag, "detectPulse++++ " + sflag + " : " +
                    sqrXYZ[0] + " / " + filteredXYZ[0] + " , " +
                    sqrXYZ[1] + " / " + filteredXYZ[1] + " , " +
                    sqrXYZ[2] + " / " + filteredXYZ[2]);
        }
    }

    private class kalmanFilter {
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