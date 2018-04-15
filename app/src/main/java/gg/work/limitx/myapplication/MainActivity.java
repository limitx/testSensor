package gg.work.limitx.myapplication;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;

import java.util.List;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private final String tag = "MainActivity";
    private SensorManager mSensorManager;
    private Sensor mACC;
    private Sensor mGYR;
    private Sensor mROTATE;


    int Height,Width;

    boolean dflag = false;
    SurfaceView sfv;
    SurfaceHolder sfh;
    Paint mPaint1 = new Paint();
    Paint mPaint2 = new Paint();
    Paint mPaint3 = new Paint();
    Paint mPaint4 = new Paint();
    Canvas canvas;
    int xx=1,oldY=0, oldY1=0 , oldY2=0 , oldY3=0 ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        List<Sensor> deviceSensors = mSensorManager.getSensorList(Sensor.TYPE_ALL);
        mACC = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);//TYPE_LINEAR_ACCELERATION
        mGYR = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        mROTATE = mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);


        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        Height = (int)(metrics.heightPixels * 0.5);
        Width = (int)(metrics.widthPixels * 0.8);

        sfv = findViewById(R.id.sfv);
        sfh = sfv.getHolder();
        sfh.setFormat(PixelFormat.TRANSLUCENT);

        mPaint1.setColor(Color.GREEN);
        mPaint1.setStrokeWidth(3);
        mPaint2.setColor(Color.RED);
        mPaint2.setStrokeWidth(5);
        mPaint3.setColor(Color.BLUE);
        mPaint3.setStrokeWidth(3);
        mPaint4.setColor(Color.BLACK);

        Button bt1 = findViewById(R.id.bt1);
        bt1.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                //dflag = !dflag;
                drawthread();
                //Log.i(tag,"onclick" + dflag);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        mSensorManager.registerListener(this, mACC, SensorManager.SENSOR_DELAY_UI);
        mSensorManager.registerListener(this, mGYR, SensorManager.SENSOR_DELAY_GAME);
        mSensorManager.registerListener(this, mROTATE, SensorManager.SENSOR_DELAY_GAME);
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

        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            showSensorData(event);
        }/* else if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE) {
            showSensorData(event);
        } else if (event.sensor.getType() == Sensor.TYPE_ROTATION_VECTOR) {
            showSensorData(event);
        }*/
    }

    final float alpha = (float) 0.8;
    float[] gravity = new float[3];
    float[] linear_acceleration = new float[3];
    int[] xyz = new int[3];
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
        xyz[0] = (int)(event.values[0]*20)+offset;
        xyz[1] = (int)(event.values[1]*20)+offset;
        xyz[2] = (int)(event.values[2]*20)+offset;
        Log.i(tag, event.sensor.getType() +" xyz: "+xyz);
        /*int x1=5,y1=7,z1=9;
        draw3(x1,y1,z1);*/
        /*if (dflag) {
            draw3((int)(linear_acceleration[0]*20),(int)(linear_acceleration[1]*20),(int)(linear_acceleration[2]*20));
        }*/
    }


    private void drawthread() {
        new Thread() {
            @Override
            public void run() {
                super.run();
                while(true) {
                    drawAll();
                    try {
                        Thread.sleep(2);
                    } catch (InterruptedException exception) {

                    }
                }
            }
        }.start();
    }



    int offset = 150;
    private void draw3(int data1,int data2,int data3) {
        xx++;
        Log.i(tag, "draw+ "+xx+"  "+data1+" "+data2+" "+data3);
        canvas = sfh.lockCanvas(new Rect(xx-1, 0, xx, Height));
        canvas.drawLine(xx-1, oldY1, xx, data1+offset , mPaint1);
        canvas.drawLine(xx-1, oldY2, xx, data2+offset , mPaint2);
        canvas.drawLine(xx-1, oldY3, xx, kalmanFilter(data3+offset) , mPaint3);

        oldY1 = data1+offset;
        oldY2 = data2+offset;
        oldY3 = data3+offset;

        sfh.unlockCanvasAndPost(canvas);

        if (xx>=Width) {
            xx=0; //canvas.drawColor(Color.TRANSPARENT,Mode.CLEAR);
            canvas = sfh.lockCanvas(new Rect(0, 0, Width, Height));
            canvas.drawRect(0, 0, Width, Height , mPaint4);
            sfh.unlockCanvasAndPost(canvas);
        }
    }

    private void drawAll() {
        xx++;
        Log.i(tag, "drawAll+ ");
        canvas = sfh.lockCanvas(new Rect(xx-1, 0, xx, Height));
        if (canvas != null) {
            canvas.drawLine(xx - 1, oldY1, xx, xyz[0], mPaint1);
            canvas.drawLine(xx - 1, oldY2, xx, xyz[1], mPaint2);
            canvas.drawLine(xx - 1, oldY3, xx, xyz[2], mPaint3);

            oldY1 = xyz[0];
            oldY2 = xyz[1];
            oldY3 = xyz[2];

            sfh.unlockCanvasAndPost(canvas);
        }

        if (xx>=Width) {
            xx=0; //canvas.drawColor(Color.TRANSPARENT,Mode.CLEAR);
            canvas = sfh.lockCanvas(new Rect(0, 0, Width, Height));
            canvas.drawRect(0, 0, Width, Height , mPaint4);
            sfh.unlockCanvasAndPost(canvas);
        }
    }

    static double prevData=0,p=12, q=0.003, r=0.1, kGain=0;
    int kalmanFilter(int inData) {
        p += q;
        kGain = p/(p+r);
        inData = (int)(prevData+(kGain*(((double)(inData)-prevData))));
        p *= (1-kGain);
        prevData = inData;

        return inData;
    }
}
