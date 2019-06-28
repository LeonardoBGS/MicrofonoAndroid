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
                    txtBox.setText(getMicrofonosActivos());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }


    public String getMicrofonosActivos() throws IOException {
        Log.d(TAG,"getMicrofonosActivos");
        if(!hasMicrophone())
            return "No hay microfono";
        AudioRecord AR = new AudioRecord(MediaRecorder.AudioSource.MIC,
                RECORDER_SAMPLERATE, RECORDER_CHANNELS,
                RECORDER_AUDIO_ENCODING, BufferElements2Rec * BytesPerElement);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
            List<AudioRecordingConfiguration> listaMicrosActivos = audioManager.getActiveRecordingConfigurations ();

            return "# microfonos activos: "+listaMicrosActivos.size();
        }
        else{
            Log.d(TAG,"API level < 24");

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
                Log.e("Stop recording","TRUE");
                Log.d(TAG,"a parar");
                try{
                    mRecorder.stop();
                }catch(RuntimeException stopException){
                    Log.e("Voice Recorder", "stop() failed "+stopException.getMessage());
                }
                Log.d(TAG,"a release");
                mRecorder.release();
            } catch (IOException e) {

                Log.e("Voice Recorder", "prepare() failed "+e.getMessage());
            }catch (Exception e) {
                isMicroOcupado=true;
                Log.d(TAG,"error e:");
            }

            mRecorder = null;
            return ""+isMicroOcupado;
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
    private void printMicrophoneInfo(MicrophoneInfo microphone) {
        Log.i(TAG, "deviceId:" + microphone.getDescription());
        Log.i(TAG, "portId:" + microphone.getId());
        Log.i(TAG, "type:" + microphone.getType());
        Log.i(TAG, "address:" + microphone.getAddress());
        Log.i(TAG, "deviceLocation:" + microphone.getLocation());
        Log.i(TAG, "deviceGroup:" + microphone.getGroup()
                + " index:" + microphone.getIndexInTheGroup());
        MicrophoneInfo.Coordinate3F position = microphone.getPosition();
        Log.i(TAG, "position:" + position.x + "," + position.y + "," + position.z);
        MicrophoneInfo.Coordinate3F orientation = microphone.getOrientation();
        Log.i(TAG, "orientation:" + orientation.x + "," + orientation.y + "," + orientation.z);
        Log.i(TAG, "frequencyResponse:" + microphone.getFrequencyResponse());
        Log.i(TAG, "channelMapping:" + microphone.getChannelMapping());
        Log.i(TAG, "sensitivity:" + microphone.getSensitivity());
        Log.i(TAG, "max spl:" + microphone.getMaxSpl());
        Log.i(TAG, "min spl:" + microphone.getMinSpl());
        Log.i(TAG, "directionality:" + microphone.getDirectionality());
        Log.i(TAG, "******");
    }*/
}
