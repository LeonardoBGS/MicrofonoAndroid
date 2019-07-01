//https://stackoverflow.com/questions/8499042/android-audiorecord-example
//https://android.googlesource.com/platform/cts/+/master/tests/tests/media/src/android/media/cts/AudioRecordTest.java
//DOC donde dice que requiere API 28
//https://developer.android.com/reference/android/media/AudioRecord.html#getActiveMicrophones()
package com.mycompany.com.myapplication;

import android.app.ActivityManager;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.media.AudioDeviceInfo;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioRecordingConfiguration;
import android.media.MediaRecorder;
import android.media.MicrophoneInfo;
import android.os.Build;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.content.Context;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MyApp";
    Button btn_isMicroInUse;
    TextView tv_isMicroInUse;
    Button btn_isMicroMute;
    TextView tv_isMicroMute;
    Button btn_isInModeCall;
    TextView tv_isInModeCall;
    Button btn_estaMicDisponible;
    TextView tv_estaMicDisponible;

    private static final int RECORDER_SAMPLERATE = 8000;
    private static final int RECORDER_CHANNELS = AudioFormat.CHANNEL_IN_MONO;
    private static final int RECORDER_AUDIO_ENCODING = AudioFormat.ENCODING_PCM_16BIT;
    private int BufferElements2Rec = 1024; // want to play 2048 (2K) since 2 bytes we use only 1024
    private int BytesPerElement = 2; // 2 bytes in 16bit format

    private int colorBtn = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btn_estaMicDisponible = findViewById(R.id.btn_estaMicDisponible);
        tv_estaMicDisponible = findViewById(R.id.tv_estaMicDisponible);

        btn_isMicroInUse= findViewById(R.id.btn_isMicroInUse);
        tv_isMicroInUse = findViewById(R.id.tv_isMicroInUse);

        btn_isInModeCall= findViewById(R.id.btn_isInModeCall);
        tv_isInModeCall= findViewById(R.id.tv_isInModeCall);

        btn_isMicroMute = findViewById(R.id.btn_isMicroMute);
        tv_isMicroMute= findViewById(R.id.tv_isMicroMute);

        btn_estaMicDisponible.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                btn_estaMicDisponible.setBackgroundColor(((colorBtn++) % 2 == 0) ? Color.GREEN : Color.YELLOW);
                tv_estaMicDisponible.setText( ""+estaMicDisponible() );

            }
        });

        btn_isMicroInUse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                btn_isMicroInUse.setBackgroundColor(((colorBtn++) % 2 == 0) ? Color.GREEN : Color.YELLOW);
                try {
                    tv_isMicroInUse.setText( ""+isMicroInUse() );
                } catch (IOException e) {
                    e.printStackTrace();
                }
                iterateInstalledApps();
            }
        });

        btn_isMicroMute.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                btn_isMicroMute.setBackgroundColor(((colorBtn++) % 2 == 0) ? Color.GREEN : Color.YELLOW);
                tv_isMicroMute.setText( ""+isMicroMute() );
            }
        });

        btn_isInModeCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                btn_isInModeCall.setBackgroundColor(((colorBtn++) % 2 == 0) ? Color.GREEN : Color.YELLOW);
                tv_isInModeCall.setText( ""+isModeInCall() );
            }
        });
    }


    private boolean isMicroInUse() throws IOException {
        if (!hasMicrophone())
            return false;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
            List<AudioRecordingConfiguration> listaMicrosActivos = audioManager.getActiveRecordingConfigurations();

            return (listaMicrosActivos.size() > 0) ? true : false;
        } else {
            MediaRecorder mRecorder;
            mRecorder = new MediaRecorder();
            mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.HE_AAC);
                mRecorder.setAudioEncodingBitRate(48000);
            } else {
                mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
                mRecorder.setAudioEncodingBitRate(64000);
            }

            mRecorder.setAudioSamplingRate(16000);
            File mOutputFile = getOutputFile();
            mOutputFile.getParentFile().mkdirs();
            mRecorder.setOutputFile(mOutputFile.getAbsolutePath());



            boolean isMicroOcupado = false;

            try {
                mRecorder.prepare();
                mRecorder.start();
                try {
                    mRecorder.stop();
                    mRecorder.release();
                } catch (RuntimeException stopException) {
                    //android detecta que hay un stop despues de start
                    //lanza exception en automatico
                    //error: MediaRecorder: stop failed: -1007
                }
            } catch (IOException e) {
            } catch (Exception e) {
                //si el micro esta en uso lanza la exception
                //error: MediaRecorder: start failed: -38
                isMicroOcupado = true;
            }
            mRecorder = null;
            return isMicroOcupado;
        }
    }

    private File getOutputFile() {
        return new File(Environment.getExternalStorageDirectory().getAbsolutePath().toString()
                + "/Voice Recorder/RECORDING_"
                + ".m4a");
    }

    private boolean hasMicrophone() {
        return getPackageManager().hasSystemFeature(
                PackageManager.FEATURE_MICROPHONE);
    }

    private boolean isMicroMute() {
        AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        Log.d(TAG, "micro mute?: " + audioManager.isMicrophoneMute());
        return audioManager.isMicrophoneMute();
    }


    private void iterateInstalledApps()
    {
        PackageManager p = this.getPackageManager();
        final List <PackageInfo> appinstall =
                p.getInstalledPackages(PackageManager.GET_PERMISSIONS);
        for(PackageInfo pInfo:appinstall)
        {
            String[] reqPermission=pInfo.requestedPermissions;
            if(reqPermission!=null)
            {
                for(int i=0;i<reqPermission.length;i++)
                {
                    if (((String)reqPermission[i]).equals("android.permission.RECORD_AUDIO") || ((String)reqPermission[i]).equals("android.permission.CALL_PHONE") )
                    {
                        Log.d("iia",pInfo.packageName );
                        Log.d("iia",reqPermission[i]);
                        Log.d("iia", ""+isAppOnForeground(pInfo.packageName));
                    }
                }
            }
        }
    }

    //checar si una app esta en background
    private boolean isAppOnForeground(String appName) {
        ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> appProcesses = activityManager.getRunningAppProcesses();
        if (appProcesses == null) {
            return false;
        }
        for (ActivityManager.RunningAppProcessInfo appProcess : appProcesses) {
            if (appProcess.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND ){//&& appProcess.processName.equals(appName)) {
                return true;
            }
        }
        return false;
    }

    public boolean isModeInCall(){
        AudioManager am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        if (am.getMode() == AudioManager.MODE_NORMAL){
            Log.d(TAG, "normal mode");
        }
        else if(am.getMode() == AudioManager.MODE_IN_CALL){
            Log.d(TAG, "call mode");
            return true;
        }
        else if(am.getMode() == AudioManager.MODE_IN_COMMUNICATION){
            Log.d(TAG, "voIP mode");
        }

        else
            Log.d(TAG, "otro mode");
        return false;
    }

    private boolean estaMicDisponible(){
        Boolean disponible= true;
        AudioRecord recorder =
                new AudioRecord(MediaRecorder.AudioSource.MIC, 44100,
                        AudioFormat.CHANNEL_IN_MONO,
                        AudioFormat.ENCODING_DEFAULT, 44100);
        try{
            if(recorder.getRecordingState() != AudioRecord.RECORDSTATE_STOPPED ){
                disponible= false;
            }
            recorder.startRecording();
            if(recorder.getRecordingState() != AudioRecord.RECORDSTATE_RECORDING){
                recorder.stop();
                disponible = false;
            }
            recorder.stop();
        } finally{
            recorder.release();
            recorder = null;
        }
        return disponible;
    }
}
