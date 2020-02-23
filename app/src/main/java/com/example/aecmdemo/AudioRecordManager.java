package com.example.aecmdemo;

import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Environment;
import android.util.Log;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import ru.theeasiestway.libaecm.AEC;

/**
 * @author longqianshan
 * @date 2020/2/20
 */
public class AudioRecordManager {
    private volatile static AudioRecordManager mInstance;
    private static final int mAudioSource = MediaRecorder.AudioSource.MIC;
    //private static final int mAudioSource = MediaRecorder.AudioSource.VOICE_COMMUNICATION;
    //指定采样率 （MediaRecoder 的采样率通常是8000Hz AAC的通常是44100Hz。 设置采样率为44100，目前为常用的采样率，官方文档表示这个值可以兼容所有的设置）
    private static final int mSampleRateInHz = 16000;
    //指定捕获音频的声道数目。在AudioFormat类中指定用于此的常量
    private static final int mChannelConfig = AudioFormat.CHANNEL_CONFIGURATION_MONO; //单声道
    //指定音频量化位数 ,在AudioFormaat类中指定了以下各种可能的常量。通常我们选择ENCODING_PCM_16BIT和ENCODING_PCM_8BIT PCM代表的是脉冲编码调制，它实际上是原始音频样本。
    //因此可以设置每个样本的分辨率为16位或者8位，16位将占用更多的空间和处理能力,表示的音频也更加接近真实。
    private static final int mAudioFormat = AudioFormat.ENCODING_PCM_16BIT;
    //指定缓冲区大小。调用AudioRecord类的getMinBufferSize方法可以获得。
    private int mBufferSizeInBytes;

    private File mRecordingFile;//储存AudioRecord录下来的文件
    private boolean isRecording = false; //true表示正在录音
    private AudioRecord mAudioRecord = null;
    private File mFileRoot = null;//文件目录
    //存放的目录路径名称
    private static final String mPathName = Environment.getExternalStorageDirectory().getAbsolutePath() + "/AudiioRecordFile";
    //保存的音频文件名
    private static final String mFileName = "audiorecordtest.pcm";
    //缓冲区中数据写入到数据，因为需要使用IO操作，因此读取数据的过程应该在子线程中执行。
    private Thread mThread;
    private DataOutputStream mDataOutputStream;

    private AEC mobileAEC;
    private int mDelay = 150;
    private int bufferSize = 320;
    private int mSelectMode = 1;
    private Context mContext;
    private String[] modePath = {"sdcard/Android/audio/no_aec_mic.pcm","sdcard/Android/audio/ori_aec_mic.pcm","sdcard/Android/audio/webrtc_aec_mic.pcm"};

    public static AudioRecordManager getInstance(Context context) {
        if (mInstance == null) {
            synchronized (AudioTrackManager.class) {
                if (mInstance == null) {
                    mInstance = new AudioRecordManager(context);
                }
            }
        }
        return mInstance;
    }

    public AudioRecordManager(Context context) {
        mContext = context;
    }

    //初始化数据
    private void initDatas() {
//        mFileRoot = new File("sdcard/Android/audio/mic.pcm");
//        if(!mFileRoot.exists())
//            mFileRoot.mkdirs();//创建文件夹

    }

    public void startRecord(int mode) {
        mSelectMode = mode;
        if(mode == 1){
            mBufferSizeInBytes = AudioRecord.getMinBufferSize(mSampleRateInHz, mChannelConfig, mAudioFormat);//计算最小缓冲区
            mAudioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, mSampleRateInHz, mChannelConfig,
                    mAudioFormat, mBufferSizeInBytes);//创建AudioRecorder对象
            AudioManager audioManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
            audioManager.setMode(AudioManager.MODE_NORMAL);
            audioManager.setSpeakerphoneOn(false);
        }
        else if (mode == 2){
            mBufferSizeInBytes = AudioRecord.getMinBufferSize(mSampleRateInHz, mChannelConfig, mAudioFormat);//计算最小缓冲区
            mAudioRecord = new AudioRecord(MediaRecorder.AudioSource.VOICE_COMMUNICATION, mSampleRateInHz, mChannelConfig,
                    mAudioFormat, mBufferSizeInBytes);//创建AudioRecorder对象
            AudioManager audioManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
            audioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);
            audioManager.setSpeakerphoneOn(true);
        }
        else if(mode == 3){
            mBufferSizeInBytes = AudioRecord.getMinBufferSize(mSampleRateInHz, mChannelConfig, mAudioFormat);//计算最小缓冲区
            mAudioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, mSampleRateInHz, mChannelConfig,
                    mAudioFormat, mBufferSizeInBytes);//创建AudioRecorder对象
            AudioManager audioManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
            audioManager.setMode(AudioManager.MODE_NORMAL);
            audioManager.setSpeakerphoneOn(false);
        }
        //AudioRecord.getMinBufferSize的参数是否支持当前的硬件设备
        if (AudioRecord.ERROR_BAD_VALUE == mBufferSizeInBytes || AudioRecord.ERROR == mBufferSizeInBytes) {
            throw new RuntimeException("Unable to getMinBufferSize");
        } else {
            destroyThread();
            isRecording = true;
            if (mThread == null) {
                mThread = new Thread(recordRunnable);
                mThread.start();//开启线程
            }
        }
    }

    /**
     * 销毁线程方法
     */
    private void destroyThread() {
        try {
            isRecording = false;
            if (null != mThread && Thread.State.RUNNABLE == mThread.getState()) {
                try {
                    Thread.sleep(500);
                    mThread.interrupt();
                } catch (Exception e) {
                    mThread = null;
                }
            }
            mThread = null;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            mThread = null;
        }
    }

    //停止录音
    public void stopRecord() {
        isRecording = false;
        //停止录音，回收AudioRecord对象，释放内存
        if (mAudioRecord != null) {
            if (mAudioRecord.getState() == AudioRecord.STATE_INITIALIZED) {//初始化成功
                mAudioRecord.stop();
            }
            if (mAudioRecord != null) {
                mAudioRecord.release();
            }
        }
    }

    Runnable recordRunnable = new Runnable() {
        @Override
        public void run() {

            //标记为开始采集状态
            isRecording = true;
            //创建一个流，存放从AudioRecord读取的数据
            mRecordingFile = new File(modePath[mSelectMode-1]);
            if (mRecordingFile.exists()) {//音频文件保存过了删除
                mRecordingFile.delete();
            }
            try {
                mRecordingFile.createNewFile();//创建新文件
            } catch (IOException e) {
                e.printStackTrace();
                Log.e("lu", "创建储存音频文件出错");
            }

            try {
                //获取到文件的数据流
                mDataOutputStream = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(mRecordingFile)));
                byte[] buffer = new byte[bufferSize];

                mAudioRecord.startRecording();//开始录音
                //getRecordingState获取当前AudioReroding是否正在采集数据的状态
                while (isRecording && mAudioRecord.getRecordingState() == AudioRecord.RECORDSTATE_RECORDING) {
                    int bufferReadResult = mAudioRecord.read(buffer, 0, bufferSize);
                    if (mSelectMode == 3) {
                        short[] aecTmpIn = new short[bufferSize / 2];
                        short[] aecTmpOut = new short[bufferSize / 2];

                        ByteBuffer.wrap(buffer).order(ByteOrder.LITTLE_ENDIAN)
                                .asShortBuffer().get(aecTmpIn);

                        aecTmpOut = mobileAEC.echoCancellation(aecTmpIn, null,
                                (short) (bufferSize / 2), (short) mDelay);

                        byte[] aecBuf = new byte[bufferSize];
                        ByteBuffer.wrap(aecBuf).order(ByteOrder.LITTLE_ENDIAN)
                                .asShortBuffer().put(aecTmpOut);
                        for (int i = 0; i < aecBuf.length; i++) {
                            mDataOutputStream.write(aecBuf[i]);
                        }
                    }
                    else{
                        for (int i = 0; i < bufferReadResult; i++) {
                            mDataOutputStream.write(buffer[i]);
                        }
                    }
                }
                mDataOutputStream.close();
            } catch (Throwable t) {
                Log.e("lu", "Recording Failed");
                stopRecord();
            }
        }
    };

    public void onDestroy() {
        destroyThread();
        stopRecord();
    }

    public void setAec(AEC aec){
        this.mobileAEC = aec;
    }

    private byte[] aecm(byte[] buff,int buffSize) throws Exception {

        short[] aecTmpIn = new short[buffSize / 2];
        short[] aecTmpOut = new short[buffSize / 2];

        ByteBuffer.wrap(buff).order(ByteOrder.LITTLE_ENDIAN)
                .asShortBuffer().get(aecTmpIn);

        mobileAEC.farendBuffer(aecTmpIn, buffSize / 2);

        aecTmpOut = mobileAEC.echoCancellation(aecTmpIn, null,
                (short) (buffSize / 2), (short) mDelay);

        byte[] aecBuf = new byte[buffSize];
        ByteBuffer.wrap(aecBuf).order(ByteOrder.LITTLE_ENDIAN)
                .asShortBuffer().put(aecTmpOut);

        return aecBuf;
    }
    public void setDelay(int delay){
      mDelay = delay;
    }
}
