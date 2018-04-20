package gg.work.limitx.myapplication;

public class CommonUtils {

    /* kalmanFilterx k1;
     k1 = new kalmanFilterx();
     k1.kalmanFilter(sqrXYZ[0]);
     */
    public class kalmanFilterx {
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
