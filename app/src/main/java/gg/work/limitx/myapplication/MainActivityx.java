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
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.OrientationEventListener;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

/**
 * Created by minche_li on 2018/04
 */

public class MainActivityx extends AppCompatActivity implements AccUtils.MotionListener {

    private final String tag = "MainActivity";

    TextView tv1;

    AccUtils test;
    //AccUtils2 test;

    public static txtRW txtR;

    private static boolean start = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        tv1 = findViewById(R.id.textView);

        ////
        test = new AccUtils(getApplicationContext(), this);
        //test = new AccUtils2(getApplicationContext(), this);

        txtR = new txtRW();//++++

        Button bt1 = findViewById(R.id.bt1);
        bt1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*if(!start) {
                    txtR = new txtRW();//++++
                    SimpleDateFormat sDateFormat = new SimpleDateFormat("MMddhhmm");
                    String date = sDateFormat.format(new java.util.Date());
                    txtR.txtRRini("ACC" + date + ".txt");
                    start = true;
                } else {
                    txtR.txtRRclose();
                    start = false;
                }*/
            }
        });

        /*OrientationEventListener mOrientationListener = new OrientationEventListener(this,
                SensorManager.SENSOR_DELAY_NORMAL) {

            @Override
            public void onOrientationChanged(int orientation) {
                Log.v("++++",
                        "Orientation changed to " + orientation);

            }
        };

        if (mOrientationListener.canDetectOrientation() == true) {
            Log.v("++++", "Can detect orientation");
            mOrientationListener.enable();
        } else {
            Log.v("++++", "Cannot detect orientation");
            mOrientationListener.disable();
        }*/
    }

    @Override
    protected void onResume() {
        super.onResume();
        test.enableAccSensor(true);
    }

    @Override
    protected void onPause() {
        super.onPause();
        test.enableAccSensor(false);

        txtR.txtRRclose();//++++
    }


    @Override
    public void onMotionChanged(int type) {
        mHandler.sendEmptyMessage(type);
    }

    public static void tttt(SensorEvent event) {
        if (start && txtR != null) {
            int[] xyz = new int[3];
            xyz[0] = (int)(event.values[0]*2);
            xyz[1] = (int)(event.values[1]*2);
            xyz[2] = (int)(event.values[2]*2);
            String tmp = String.valueOf(event.values[0]) +" "+String.valueOf(event.values[1]) +" "+ String.valueOf(event.values[2]);
            txtR.txtWrr(tmp);
        }
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
                        toastMessage = Toast.makeText(MainActivityx.this,
                                "motion detected!!", Toast.LENGTH_SHORT);
                        toastMessage.show();
                    }

                    tv1.setText("+++++++");
                    tv1.setTextColor(Color.RED);
                    Log.i(tag, "toastMessage++");
                    break;
                case 2:
                    if (toastMessage != null) {
                        toastMessage.cancel();
                        toastMessage = null;
                    }
                    tv1.setText("------");
                    tv1.setTextColor(Color.BLACK);
                    Log.i(tag, "toastMessage--");
                    break;
            }
        }
    };
}