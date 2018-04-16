package gg.work.limitx.myapplication;

import android.os.Handler;
import android.util.Log;

import java.util.ArrayList;

public class AccUtils {

    private static final String tag = "AccUtils";

    // ACC algorithm
    private boolean detectStart;
    private static boolean sflag;
    int[] xyz,prevXYZ,diffXYZ,sqrXYZ,filteredXYZ;
    private ArrayList filterX;
    private ArrayList filterY;
    private ArrayList filterZ;
    private static final int filter_size = 5;
    private static final int filter_d_size = filter_size-1;

    private Handler mHandler;

    public void AccUtils(Handler mhandler) {
        mHandler = mhandler;
        initParameter();
    }

    private void initParameter() {
        detectStart = false;
        sflag = false;
        xyz = new int[3];
        prevXYZ = new int[3];
        diffXYZ = new int[3];
        sqrXYZ = new int[3];
        filteredXYZ  = new int[3];
        filterX = new ArrayList();
        filterY = new ArrayList();
        filterZ = new ArrayList();
    }

    public void detectPulse() {
        if (!detectStart) {
            detectStart = true;
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

            int[] sumXYZ  = new int[3];
            for(int i = 1; i < filterX.size(); i++) {
                sumXYZ[0] += (int)filterX.get(i);
                sumXYZ[1] += (int)filterY.get(i);
                sumXYZ[2] += (int)filterZ.get(i);
            }

            filteredXYZ[0] = sumXYZ[0] / filter_d_size; // divide by filter_d_size
            filteredXYZ[1] = sumXYZ[1] / filter_d_size; // divide by filter_d_size
            filteredXYZ[2] = sumXYZ[2] / filter_d_size; // divide by filter_d_size

            //Log.i(tag, "detectPulse+ " + filteredXYZ[0] +" / "+filteredXYZ[2]);
            boolean flag = false;
            if (Math.abs(filteredXYZ[0] - sqrXYZ[0]) > 20 || Math.abs(filteredXYZ[2] - sqrXYZ[2]) > 15) {
                flag = true;
                if (sflag != flag) {
                    sflag = flag;
                    if (mHandler.obtainMessage(1) != null) {
                        mHandler.removeMessages(1);
                    }
                    mHandler.sendEmptyMessage(1);
                }
            } else {//if (filteredXYZ[0] < 10 && filteredXYZ[2] < 10) {
                flag = false;
                if (sflag != flag) {
                    sflag = flag;
                    if (mHandler.obtainMessage(2) != null) {
                        mHandler.removeMessages(2);
                    }
                    mHandler.sendEmptyMessage(2);
                }
            }

            Log.i(tag, "detectPulse++++ "+flag+" : "+
                    sqrXYZ[0]+" / "+filteredXYZ[0] +" , "+
                    sqrXYZ[1]+" / "+filteredXYZ[1]+" , "+
                    sqrXYZ[2]+" / "+filteredXYZ[2]);
        }
    }
}
