//https://stackoverflow.com/questions/8499042/android-audiorecord-example
//https://android.googlesource.com/platform/cts/+/master/tests/tests/media/src/android/media/cts/AudioRecordTest.java
//DOC donde dice que requiere API 28
//https://developer.android.com/reference/android/media/AudioRecord.html#getActiveMicrophones()
package com.mycompany.com.myapplication;

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
    Button btn;
    TextView txtBox;

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

        btn = findViewById(R.id.button);
        txtBox = findViewById(R.id.textView);

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    btn.setBackgroundColor( ( (colorBtn++)%2==0)? Color.GREEN: Color.YELLOW );
                    txtBox.setText(isMicroInUse()?"Micro en uso":"Micro no esta en uso");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }


    public boolean isMicroInUse() throws IOException {
        if(!hasMicrophone())
            return false;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
            List<AudioRecordingConfiguration> listaMicrosActivos = audioManager.getActiveRecordingConfigurations ();

            return (listaMicrosActivos.size()>0)?true:false;
        }
        else{
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

            boolean isMicroOcupado=false;

            try {
                mRecorder.prepare();
                mRecorder.start();
                try{
                    mRecorder.stop();
                    mRecorder.release();
                }catch(RuntimeException stopException){
                    //android detecta que hay un stop despues de start
                    //lanza exception en automatico
                    //error: MediaRecorder: stop failed: -1007
                }
            } catch (IOException e) {
            }catch (Exception e) {
                //si el micro esta en uso lanza la exception
                //error: MediaRecorder: start failed: -38
                isMicroOcupado=true;
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


    /*
    public boolean isMicroInUse() throws IOException {
        if(!hasMicrophone())
            Log.d(TAG,"no hay microfono");

        AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        Log.d(TAG, "micro mute?: "+audioManager.isMicrophoneMute());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
            List<AudioRecordingConfiguration> listaMicrosActivos = audioManager.getActiveRecordingConfigurations ();

            return (listaMicrosActivos.size()>0)?true:false;
        }
        else{
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

            boolean isMicroOcupado=false;

            try {
                mRecorder.prepare();
                mRecorder.start();
                try{
                    mRecorder.stop();
                    mRecorder.release();
                }catch(RuntimeException stopException){
                    //android detecta que hay un stop despues de start
                    //lanza exception en automatico
                    //error: MediaRecorder: stop failed: -1007
                }
            } catch (IOException e) {
            }catch (Exception e) {
                //si el micro esta en uso lanza la exception
                //error: MediaRecorder: start failed: -38
                isMicroOcupado=true;
            }
            mRecorder = null;
            return isMicroOcupado;
        }
    }

    private File getOutputFile() {
        return new File(Environment.getExternalStorageDirectory().getAbsolutePath()
                + "/Voice Recorder/RECORDING_"
                + ".m4a");
    }

    private boolean hasMicrophone() {
        return getPackageManager().hasSystemFeature(
                PackageManager.FEATURE_MICROPHONE);
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
                    if (((String)reqPermission[i]).equals("android.permission.RECORD_AUDIO"))
                    {
                        Log.d(TAG,pInfo.toString());
                        break;
                    }
                }
            }
        }
    }

    public void isMicroActive1(){
        AudioManager am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        if (am.getMode() == AudioManager.MODE_NORMAL){
            Log.d(TAG, "normal mode");
        }
        else if(am.getMode() == AudioManager.MODE_IN_CALL){
            Log.d(TAG, "call mode");
        }
        else if(am.getMode() == AudioManager.MODE_IN_COMMUNICATION){
            Log.d(TAG, "voIP mode");
        }
        else
            Log.d(TAG, "otro mode");
    }


    /*
    MODE_IN_CALL solo funciona cuando se llama desde agente a viajero



     */
}
