package com.mass.audio;

import android.media.AudioFormat;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.mass.audio.library.Recorder;
import com.mass.audio.library.model.OnByteBufferDataChangeListener;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Calendar;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

public class MainActivity extends AppCompatActivity implements OnByteBufferDataChangeListener {

    public static final String DIR_PATH = Environment.getExternalStorageDirectory() + "/心音文件/";
    public String currentFile = "";
    private static final int NUM_SAMPLES = 64;
    private static final int MAX_SIZE = 8*1024*60;
    private int sampleSize = MAX_SIZE;
    private int sampleCount = 0;
    private TextView text_msg;
    private TextView text_time;
    private EditText text_name;
    private GraphView graph;
    LineGraphSeries<DataPoint> seriesLine;

    private String sData;
    private String sResult = "采样分析：";

    private ImageButton mImageButton;

    private Recorder mRecorder;
    private FileOutputStream mStream;
    private FileInputStream inStream;

    private int sampleFileNum = 0;
    private int runFlag = 1;

    private byte[] output;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        text_msg = (TextView) findViewById(R.id.text_msg);
        text_time = (TextView)findViewById(R.id.text_time);
        text_name = (EditText)findViewById(R.id.name);
        graph = (GraphView) findViewById(R.id.graph1);
        sampleSize = MAX_SIZE;
        text_msg.setText("初始状态：");
        mImageButton = (ImageButton) findViewById(R.id.action_image);
        seriesLine = new LineGraphSeries<DataPoint>(new DataPoint[64]);
        graph.getViewport().setYAxisBoundsManual(true);
        graph.getViewport().setMinY(-32768);
        graph.getViewport().setMaxY(32767);

        mImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mRecorder == null) {
                    return;
                }
                if(!isFolderExists(DIR_PATH)){
                    text_msg.setText("无法创建心音文件目录！");
                }

                boolean recording = mRecorder.isRecording();
                if (recording) {
                    ((ImageButton) v).setImageResource(R.drawable.record);
                    mRecorder.stop();
                    text_msg.setText("停止采样：");
                    try {
                        anlayseSampleData(currentFile);
                    }catch (IOException e){
                        e.printStackTrace();
                    }
                    //ds();
                    if (mStream != null){
                        try {
                            mStream.close();
                        }catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                } else {
                    ((ImageButton) v).setImageResource(R.drawable.pause);
                    createOutputFile();
                    sampleSize = MAX_SIZE;
                    graph.removeAllSeries();
                    mRecorder.startRecording();
                    text_msg.setText("开始采样：.........");
                    sampleFileNum++;
                    //yunXing();
                    //ds();
                }
            }
        });

        mRecorder = new Recorder(4000,
                AudioFormat.CHANNEL_IN_MONO/*单双声道*/,
                AudioFormat.ENCODING_PCM_16BIT/*格式*/,
                MediaRecorder.AudioSource.MIC/*AudioSource*/,
                NUM_SAMPLES/*period*/,
                this/*onDataChangeListener*/);
        output = new byte[NUM_SAMPLES * 2];

        //displaySampleData();
        //yunXing();

    }
    public void yunXing() {
        new Thread() {
            public void run() {
                int j = 1000;
                DataPoint[] values = new DataPoint[32];
                boolean recording = mRecorder.isRecording();
                while(true) {
                    while (mRecorder.isRecording()) {
                        graph.removeAllSeries();
                        LineGraphSeries<DataPoint> series = new LineGraphSeries<DataPoint>(values);
                        double x = 0, y;
                        for (int i = 0; i < 32; i++) {
                            x = x + 0.1;
                            values[i] = new DataPoint(x, 10 * i * j * Math.sin(x));
                            //series.appendData(new DataPoint(x, y), true, 100);
                        }
                        series.resetData(values);
                        graph.addSeries(series);
                        //Log.i("xuhaitao", "子线程打印");
                        j--;

                    }
                }
            }
        }.start();  //开启一个线
    }

    public void displaySampleData() {
        new Thread() {
            public void run() {
                while ((sampleFileNum == 0)||(mRecorder == null)) {
                    try {
                        Thread.sleep(100);
                    }catch (InterruptedException e){
                        e.printStackTrace();
                    }
                    //等待采样文件及录音准备完成
                }
                graph.removeAllSeries();
                int fileFlag = 0;
                FileInputStream readFile = null;

                DataPoint[] values = new DataPoint[32];
                int[] y = new int[32];
                int yPosition = 0;
                byte[] b = new byte[64];
                byte[] s = new byte[2];
                int rNumber = 0;
                boolean recording = mRecorder.isRecording();
                while (mRecorder != null) {
                    //graph.removeAllSeries();
                    //有新的采样文件正在采样，打开新的输入流
                    if(fileFlag != sampleFileNum) {
                        //每次实时显示波形时初始化显示数组
                        for(int i = 0;i<32;i++){
                            y[i] = 0;
                        }
                        if (readFile != null) {
                            try {
                                readFile.close();
                            }catch(IOException e){
                                e.printStackTrace();
                            }
                            try {
                                readFile = new FileInputStream(currentFile);
                            } catch (FileNotFoundException e) {
                                e.printStackTrace();
                            }
                        }
                        fileFlag = sampleFileNum;
                    }
                    while(recording && (fileFlag == sampleFileNum)){
                        graph.removeAllSeries();
                        try {
                            rNumber = readFile.read(b);
                        }catch (IOException e){
                            e.printStackTrace();
                        }
                        for(short j=0;j<rNumber/2;j++) {
                            s[0] = b[j * 2];
                            s[1] = b[j * 2 + 1];
                            y[j] = byteToShort(s);
                        }
                        yPosition = (rNumber/2+yPosition)%1000;
                        double a=0,z=0;
                        for(int j=0;j<32;j++){
                            a = a+0.1;
                            z = y[j];
                            values[j] = new DataPoint(a,z);
                        }
                        seriesLine.resetData(values);
                        graph.addSeries(seriesLine);
                        try{
                            Thread.sleep(8);
                        }catch (InterruptedException e){
                            e.printStackTrace();
                        }
                        break;
                    }
                    break;
                }
            }
        }.start();  //开启一个线
    }
    private void anlayseSampleData(String fname) throws IOException {
        LineGraphSeries<DataPoint> series = new LineGraphSeries<DataPoint>();
        graph.removeAllSeries();
        sResult = "采样分析：";

        if (inStream != null){
            inStream.close();
        }
        try {
            inStream = new FileInputStream(fname);
        }catch (FileNotFoundException e) {
            e.printStackTrace();
        };

        byte[] s = new byte[2];
        byte[] b = new byte[4*1024];
        int[] y = new int[2*1024];
        int size = inStream.available();
        int nSeg = size/4/1024;
        if (nSeg < 20){
            text_msg.setText("采样分析：采样时间过短(小于10秒)！");
            return;
        }
        nSeg = 20;
        int[] yAddress = new int[nSeg];
        int[] yMax = new int[nSeg];
        int[] ma = new int[2];

        for(short i =0; i<nSeg; i++) {
            inStream.read(b);
            for(short j=0;j<2*1024;j++) {
                s[0] = b[j * 2];
                s[1] = b[j * 2 + 1];
                y[j] = byteToShort(s);
            }
            ma = findMax(y,10,y.length);
            yMax[i] = ma[0];
            yAddress[i] = ma[1]+ 2*1024*i;
        }
        ma = findMax(yMax,0,yMax.length);
        int maxAddress = ma[1];
        int hrtRate = 75;

        if(maxAddress>=2){
            if(yAddress[maxAddress]-yAddress[maxAddress-1]>2048){
                hrtRate = 60*4096/(yAddress[maxAddress]-yAddress[maxAddress-1]);
            }
            else{
                hrtRate = 60*4096/(yAddress[maxAddress]-yAddress[maxAddress-2]);
            }
        }else{
            if(yAddress[maxAddress+1]-yAddress[maxAddress]>2048){
                hrtRate = 60*4096/(yAddress[maxAddress+1]-yAddress[maxAddress]);
            }else{
                hrtRate = 60*4096/(yAddress[maxAddress+2]-yAddress[maxAddress]);
            }
        }
        int yMean = 0;
        int countM = 0;
        int peakV = 32767;
        for(int i=0;i<nSeg;i++){
            yMean = yMean + Math.abs(yMax[i]);
            if(Math.abs(yMax[i])>= peakV){
                countM = countM + 1;
            }
        }
        if(countM >= nSeg/3){
            sResult = sResult+"心音信号过强！";
        }else{
            if ((yMean=yMean/nSeg) < 5000){
                sResult = sResult+"心音信号过弱！";
            }else{
                sResult = sResult+"心音信号正常！";
                if(hrtRate>90){
                    sResult = sResult+"心率过快（有较大杂音）:";
                } else{
                    sResult = sResult + "心率值：" + hrtRate;
                }
            }
        }

        text_msg.setText(sResult);
    }
    public static short byteToShort(byte[] b) {
        short s = 0;
        short s0 = (short) (b[0] & 0xff);// 最低位
        short s1 = (short) (b[1] & 0xff);
        s1 <<= 8;
        s = (short) (s0 | s1);
        return s;
    }

    private boolean isFolderExists(String strFolder) {
        File file = new File(strFolder);
        if (!file.exists()) {
            if (file.mkdirs()) {
                return true;
            } else {
                return false;

            }
        }
        return true;

    }
    private boolean createOutputFile() {
        currentFile = "";
        try {
            Calendar now = Calendar.getInstance();
            String filename = DIR_PATH+now.get(Calendar.YEAR);
            if((now.get(Calendar.MONTH)+1)<10) {
                filename = filename + "0"+(now.get(Calendar.MONTH) + 1);
            }else{
                filename = filename +(now.get(Calendar.MONTH) + 1);
            }
            if((now.get(Calendar.DAY_OF_MONTH)<10)) {
                filename = filename + "0"+now.get(Calendar.DAY_OF_MONTH);
            }else{
                filename = filename +now.get(Calendar.DAY_OF_MONTH);
            }
            if((now.get(Calendar.HOUR_OF_DAY)<10)) {
                filename = filename + "0"+now.get(Calendar.HOUR_OF_DAY);
            }else{
                filename = filename +now.get(Calendar.HOUR_OF_DAY);
            }
            if((now.get(Calendar.MINUTE)<10)) {
                filename = filename + "0"+now.get(Calendar.MINUTE);
            }else{
                filename = filename +now.get(Calendar.MINUTE);
            }
            if((now.get(Calendar.SECOND)<10)) {
                filename = filename + "0"+now.get(Calendar.SECOND);
            }else{
                filename = filename +now.get(Calendar.SECOND);
            }
            filename = filename + "."+text_name.getText().toString()+".pcm";//text_name.getText().toString()
            currentFile = currentFile + filename;
            mStream = new FileOutputStream(new File(filename));
            return true;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return false;
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        boolean recording = mRecorder.isRecording();
        if (recording) {
            mRecorder.stop();
            if (mStream != null){
                try {
                    mStream.close();
                }catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public void onDataChange(int position, ByteBuffer buffer) {
        try {
            if (mStream != null) {
                mStream.write(buffer.array());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        sampleSize = sampleSize - NUM_SAMPLES*2;
        if(sampleSize <= 0){
            mImageButton.setImageResource(R.drawable.record);
            mRecorder.stop();
            try {
                anlayseSampleData(currentFile);
            }catch(IOException e){
                e.printStackTrace();
            }
            if (mStream != null){
                try {
                    mStream.close();
                }catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }
        sampleCount++;
        if (sampleCount%10 == 0) {
            sampleCount = 0;
            ds(buffer);
        }
        text_time.setText("本次采样时间（秒）："+String.valueOf(60-sampleSize/(8*1024)));
    }

    public String bytes2HexString(byte[] src){
        StringBuilder stringBuilder = new StringBuilder("");
        if (src == null || src.length <= 0) {
            return null;
        }
        for (int i = 0; i < src.length; i++) {
            int v = src[i] & 0xFF;
            String hv = Integer.toHexString(v);
            if (hv.length() < 2) {
                stringBuilder.append(0);
            }
            stringBuilder.append(hv);
        }
        return stringBuilder.toString();
    }

    /**
     * 适用于小端在前的short转换成byte
     *
     * @param value 16位short值
     * @return byte[]数组
     */
    private byte[] short2byte(short value) {
        byte[] data = new byte[2];
        data[0] = (byte) (value & 0xFF);
        data[1] = (byte) ((value >> 8) & 0xFF);
        return data;
    }
    //在short数组中找到数组中绝对值中的最大值及地址并返回：yMax[0]保存最大值，yMax[1]保存地址
    private int[] findMax(int[] y,int b,int e){
        int[] yMax = new int[2];
        yMax[0] = y[b];
        yMax[1] = b;
        for(int i = b;i<e;i++){
            if(Math.abs(y[i])>=Math.abs(yMax[0])){
                yMax[0] = y[i];
                yMax[1] = i;
            }
        }
        return yMax;
    }

    public  void ds(ByteBuffer buffer) {
        DataPoint[] values = new DataPoint[64];
        LineGraphSeries<DataPoint> series = new LineGraphSeries<DataPoint>(values);
        graph.removeAllSeries();
        int[] y = new int[64];
        byte[] b = new byte[128];
        System.arraycopy(buffer.array(),0,b,0,128);
        byte[] s = new byte[2];
        for (short j = 0; j < 64; j++) {
            s[0] = b[j * 2];
            s[1] = b[j * 2 + 1];
            y[j] = byteToShort(s);
        }

        double a = 0, z = 0;
        for (int j = 0; j < 64; j++) {
            a = a + 0.1;
            z = y[j];
            values[j] = new DataPoint(a, z);
        }
        seriesLine.resetData(values);
        graph.addSeries(seriesLine);
    }
}