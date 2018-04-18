package gg.work.limitx.myapplication;

import android.os.Environment;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;

public class txtRW {

    private static FileOutputStream fosGR,fosGB,fosGK;
    private static FileOutputStream fosRR,fosRB,fosRK;
    private static FileOutputStream fosBR,fosBB,fosBK;
    private static FileOutputStream fosHR,fosTime,fosACC;
    private static FileOutputStream fosX,fosY;
    private static FileOutputStream fosPP,fosHHR;
    String tmpPath;
    File targetFile;


    public void txtWini(String fpath){
        try{
            if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED))  {
                File sdCardDir= Environment.getExternalStorageDirectory();

                tmpPath = sdCardDir.getCanonicalPath()+"/RR"+fpath;
                targetFile =new File(tmpPath);
                fosRR = new FileOutputStream(targetFile);
                tmpPath = sdCardDir.getCanonicalPath()+"/RB"+fpath;
                targetFile =new File(tmpPath);
                fosRB = new FileOutputStream(targetFile);
                tmpPath = sdCardDir.getCanonicalPath()+"/RK"+fpath;
                targetFile =new File(tmpPath);
                fosRK = new FileOutputStream(targetFile);

                tmpPath = sdCardDir.getCanonicalPath()+"/GR"+fpath;
                targetFile =new File(tmpPath);
                fosGR = new FileOutputStream(targetFile);
                tmpPath = sdCardDir.getCanonicalPath()+"/GB"+fpath;
                targetFile =new File(tmpPath);
                fosGB = new FileOutputStream(targetFile);
                tmpPath = sdCardDir.getCanonicalPath()+"/GK"+fpath;
                targetFile =new File(tmpPath);
                fosGK = new FileOutputStream(targetFile);

                tmpPath = sdCardDir.getCanonicalPath()+"/BR"+fpath;
                targetFile =new File(tmpPath);
                fosBR = new FileOutputStream(targetFile);
                tmpPath = sdCardDir.getCanonicalPath()+"/BB"+fpath;
                targetFile =new File(tmpPath);
                fosBB = new FileOutputStream(targetFile);

                tmpPath = sdCardDir.getCanonicalPath()+"/BK"+fpath;
                targetFile =new File(tmpPath);
                fosBK = new FileOutputStream(targetFile);

            }
        }catch(Exception e){
            e.printStackTrace();
        }
    }
    public void txtWHRini(String fpath){
        try{
            if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED))  {
                File sdCardDir=Environment.getExternalStorageDirectory();

                tmpPath = sdCardDir.getCanonicalPath()+"/HR"+fpath;
                targetFile =new File(tmpPath);
                fosHR = new FileOutputStream(targetFile);

                tmpPath = sdCardDir.getCanonicalPath()+"/Time"+fpath;
                targetFile =new File(tmpPath);
                fosTime = new FileOutputStream(targetFile);

            }
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    public void txtRRini(String fpath){
        try{
            if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED))  {
                File sdCardDir=Environment.getExternalStorageDirectory();
                tmpPath = sdCardDir.getCanonicalPath()+"/R"+fpath;
                targetFile = new File(tmpPath);
                fosRR = new FileOutputStream(targetFile, true);

                Log.i("txtrw", "ok++");
            }
        }catch(Exception e){
            e.printStackTrace();
            Log.i("txtrw", "not ok");
        }
    }

    public void txtEXP2ini(String fpath){
        try{
            if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED))  {
                File sdCardDir=Environment.getExternalStorageDirectory();

                tmpPath = sdCardDir.getCanonicalPath()+"/GR"+fpath;
                targetFile =new File(tmpPath);
                fosGR = new FileOutputStream(targetFile);

                tmpPath = sdCardDir.getCanonicalPath()+"/GB"+fpath;
                targetFile =new File(tmpPath);
                fosGB = new FileOutputStream(targetFile);

                tmpPath = sdCardDir.getCanonicalPath()+"/GK"+fpath;
                targetFile =new File(tmpPath);
                fosGK = new FileOutputStream(targetFile);

                tmpPath = sdCardDir.getCanonicalPath()+"/HR"+fpath;
                targetFile =new File(tmpPath);
                fosHR = new FileOutputStream(targetFile);

                tmpPath = sdCardDir.getCanonicalPath()+"/ACC"+fpath;
                targetFile =new File(tmpPath);
                fosACC = new FileOutputStream(targetFile);

                tmpPath = sdCardDir.getCanonicalPath()+"/X"+fpath;
                targetFile =new File(tmpPath);
                fosX = new FileOutputStream(targetFile);

                tmpPath = sdCardDir.getCanonicalPath()+"/Y"+fpath;
                targetFile =new File(tmpPath);
                fosY = new FileOutputStream(targetFile);

                tmpPath = sdCardDir.getCanonicalPath()+"/Time"+fpath;
                targetFile =new File(tmpPath);
                fosTime = new FileOutputStream(targetFile);

                tmpPath = sdCardDir.getCanonicalPath()+"/_PP"+fpath;
                targetFile =new File(tmpPath);
                fosPP = new FileOutputStream(targetFile);

                tmpPath = sdCardDir.getCanonicalPath()+"/_HHR"+fpath;
                targetFile =new File(tmpPath);
                fosHHR = new FileOutputStream(targetFile);

            }
        }catch(Exception e){
            e.printStackTrace();
        }
    }



    public String txtR(String fpath){
        try{
            if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED))  {
                File sdCardDir=Environment.getExternalStorageDirectory();

                FileInputStream fis=new FileInputStream(sdCardDir.getCanonicalPath()+'/'+fpath);
                BufferedReader br=new BufferedReader(new InputStreamReader(fis));
                StringBuilder sb=new StringBuilder("");
                String line=null;
                while((line=br.readLine())!=null){
                    sb.append(line);
                }
                br.close();
                return sb.toString();
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    ////
    public void txtWrr(String content){
        try{
            String tmp=content+'\n';
            fosRR.write(tmp.getBytes());
        }catch(Exception e){
            e.printStackTrace();
        }
    }
    public void txtWrb(String content){
        try{
            String tmp=content+'\n';
            fosRB.write(tmp.getBytes());
        }catch(Exception e){
            e.printStackTrace();
        }
    }
    public void txtWrk(String content){
        try{
            String tmp=content+'\n';
            fosRK.write(tmp.getBytes());
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    ////
    public void txtWgr(String content){
        try{
            String tmp=content+'\n';
            fosGR.write(tmp.getBytes());
        }catch(Exception e){
            e.printStackTrace();
        }
    }
    public void txtWgb(String content){
        try{
            String tmp=content+'\n';
            fosGB.write(tmp.getBytes());
        }catch(Exception e){
            e.printStackTrace();
        }
    }
    public void txtWgk(String content){
        try{
            String tmp=content+'\n';
            fosGK.write(tmp.getBytes());
        }catch(Exception e){
            e.printStackTrace();
        }
    }
    ////
    public void txtWbr(String content){
        try{
            String tmp=content+'\n';
            fosBR.write(tmp.getBytes());
        }catch(Exception e){
            e.printStackTrace();
        }
    }
    public void txtWbb(String content){
        try{
            String tmp=content+'\n';
            fosBB.write(tmp.getBytes());
        }catch(Exception e){
            e.printStackTrace();
        }
    }
    public void txtWbk(String content){
        try{
            String tmp=content+'\n';
            fosBK.write(tmp.getBytes());
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    public void txtHR(String content){
        try{
            String tmp=content+'\n';
            fosHR.write(tmp.getBytes());
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    public void txtTime(String content){
        try{
            String tmp=content+'\n';
            fosTime.write(tmp.getBytes());
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    public void txtX(String content){
        try{
            String tmp=content+'\n';
            fosX.write(tmp.getBytes());
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    public void txtY(String content){
        try{
            String tmp=content+'\n';
            fosY.write(tmp.getBytes());
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    public void txtACC(String content){
        try{
            String tmp=content+'\n';
            fosACC.write(tmp.getBytes());
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    public void txtPP(String content){
        try{
            String tmp=content+'\n';
            fosPP.write(tmp.getBytes());
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    public void txtHHR(String content){
        try{
            String tmp=content+'\n';
            fosHHR.write(tmp.getBytes());
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    ////
    public void txtWclose(){
        try{
            fosRR.close();
            fosGR.close();
            fosBR.close();
            fosRB.close();
            fosGB.close();
            fosBB.close();
            fosRK.close();
            fosGK.close();
            fosBK.close();
            fosHR.close();

        }catch(Exception e){
            e.printStackTrace();
        }
    }
    public void txtRRclose(){
        try{
            fosRR.close();
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    public void txtWHRclose(){
        try{
            fosHR.close();
            fosTime.close();
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    public void txtEXP2close(){
        try{
            fosGR.close();
            fosGB.close();
            fosGK.close();
            fosHR.close();
            fosACC.close();
            fosTime.close();
            fosX.close();
            fosY.close();
            fosPP.close();
            fosHHR.close();
        }catch(Exception e){
            e.printStackTrace();
        }
    }
}