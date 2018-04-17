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
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class MainActivityx extends AppCompatActivity implements AccUtils.MotionListener {

    private final String tag = "MainActivity";



    TextView tv1;
    AccUtils test;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        tv1 = findViewById(R.id.textView);

        test = new AccUtils(getApplicationContext(), this);

        Button bt1 = findViewById(R.id.bt1);
        bt1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
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
    }

    @Override
    public void onMotionChanged(int type) {
        mHandler.sendEmptyMessage(type);
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