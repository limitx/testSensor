package gg.work.limitx.myapplication;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.androidplot.util.Redrawer;
import com.androidplot.xy.AdvancedLineAndPointRenderer;
import com.androidplot.xy.BoundaryMode;
import com.androidplot.xy.XYPlot;
import com.androidplot.xy.XYSeries;

import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.List;


/**
 * Created by minche_li on 2018/04
 */

public class MainActivityx extends AppCompatActivity implements AccUtils.MotionListener {

    private final String tag = "MainActivity";

    TextView tv1;

    AccUtils test;
    //AccUtils2 test;

    public static txtRW txtR;

    private XYPlot plot;
    private Redrawer redrawer;
    ECGModel ecgSeries1,ecgSeries2,ecgSeries3;

    private static boolean start = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mainx);


        tv1 = findViewById(R.id.textView);
        tv1.setTextSize(20);

        ////
        test = new AccUtils(getApplicationContext(), this);
        //test = new AccUtils2(getApplicationContext(), this);

        txtR = new txtRW();//++++


        // initialize our XYPlot reference:
        plot = findViewById(R.id.plot);
        // ECGModel
        ecgSeries1 = new ECGModel(1500);
        ecgSeries2 = new ECGModel(1500);
        ecgSeries3 = new ECGModel(1500);
        // add a new series' to the xyplot:
        MyFadeFormatter formatter1 =new MyFadeFormatter(1500);
        MyFadeFormatter formatter2 =new MyFadeFormatter(1500);
        MyFadeFormatter formatter3 =new MyFadeFormatter(1500);
        formatter1.setLegendIconEnabled(false);
        formatter2.setLegendIconEnabled(false);
        formatter3.setLegendIconEnabled(false);
        formatter1.getLinePaint().setColor(Color.RED);
        formatter2.getLinePaint().setColor(Color.YELLOW);
        formatter3.getLinePaint().setColor(Color.BLUE);
        plot.addSeries(ecgSeries1, formatter1);
        plot.addSeries(ecgSeries2, formatter2);
        plot.addSeries(ecgSeries3, formatter3);
        plot.setRangeBoundaries(0, 60, BoundaryMode.FIXED);
        plot.setDomainBoundaries(0, 1500, BoundaryMode.FIXED);
        // reduce the number of range labels
        plot.setLinesPerRangeLabel(3);
        // start generating ecg data in the background:
        ecgSeries1.start(new WeakReference<>(plot.getRenderer(AdvancedLineAndPointRenderer.class)));
        ecgSeries2.start(new WeakReference<>(plot.getRenderer(AdvancedLineAndPointRenderer.class)));
        ecgSeries3.start(new WeakReference<>(plot.getRenderer(AdvancedLineAndPointRenderer.class)));
        // set a redraw rate of 30hz and start immediately:
        redrawer = new Redrawer(plot, 30, true);

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

                ecgSeries1.addPt(50);
                Log.v(tag, " ecgSeries ");
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
    public void onStop() {
        super.onStop();
        redrawer.finish();
    }

    @Override
    public void onMotionChanged(int type) {
        mHandler.sendEmptyMessage(type);
    }

    @Override
    public void DrawX(int data1,int data2,int data3) {
        ecgSeries1.addPt(data1);
        ecgSeries2.addPt(data2);
        ecgSeries3.addPt(data3);
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

    public static class MyFadeFormatter extends AdvancedLineAndPointRenderer.Formatter {

        private int trailSize;

        public MyFadeFormatter(int trailSize) {
            this.trailSize = trailSize;
        }

        @Override
        public Paint getLinePaint(int thisIndex, int latestIndex, int seriesSize) {
            // offset from the latest index:
            int offset;
            if(thisIndex > latestIndex) {
                offset = latestIndex + (seriesSize - thisIndex);
            } else {
                offset =  latestIndex - thisIndex;
            }

            float scale = 255f / trailSize;
            int alpha = (int) (255 - (offset * scale));
            getLinePaint().setAlpha(alpha > 0 ? alpha : 0);
            return getLinePaint();
        }
    }

    public static class ECGModel implements XYSeries {
        private final Number[] data;
        private int latestIndex;

        private WeakReference<AdvancedLineAndPointRenderer> rendererRef;

        /**
         *
         * @param size Sample size contained within this model
         */
        public ECGModel(int size) {
            data = new Number[size];
            for(int i = 0; i < data.length; i++) {
                data[i] = 0;
            }
        }

        public void addPt(int x) {
            if (latestIndex >= data.length) {
                latestIndex = 0;
            }

            data[latestIndex] = x;

            if(latestIndex < data.length - 1) {
                // null out the point immediately following i, to disable
                // connecting i and i+1 with a line:
                data[latestIndex +1] = null;
            }

            if(rendererRef.get() != null) {
                rendererRef.get().setLatestIndex(latestIndex);
            }
            latestIndex++;
        }

        public void start(final WeakReference<AdvancedLineAndPointRenderer> rendererRef) {
            this.rendererRef = rendererRef;
        }

        @Override
        public int size() {
            return data.length;
        }

        @Override
        public Number getX(int index) {
            return index;
        }

        @Override
        public Number getY(int index) {
            return data[index];
        }

        @Override
        public String getTitle() {
            return "Signal";
        }
    }
}