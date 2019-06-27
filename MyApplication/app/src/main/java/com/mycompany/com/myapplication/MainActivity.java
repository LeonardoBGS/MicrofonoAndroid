//https://stackoverflow.com/questions/8499042/android-audiorecord-example
//https://android.googlesource.com/platform/cts/+/master/tests/tests/media/src/android/media/cts/AudioRecordTest.java
//DOC donde dice que requiere API 28
//https://developer.android.com/reference/android/media/AudioRecord.html#getActiveMicrophones()
package com.mycompany.com.myapplication;

import android.content.pm.PackageManager;
import android.media.AudioDeviceInfo;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.media.MicrophoneInfo;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.content.Context;

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
        Log.d(TAG,"Hay microfono");
        AudioRecord AR = new AudioRecord(MediaRecorder.AudioSource.MIC,
                RECORDER_SAMPLERATE, RECORDER_CHANNELS,
                RECORDER_AUDIO_ENCODING, BufferElements2Rec * BytesPerElement);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            Log.d(TAG,"VER correcta");
            List<MicrophoneInfo> activeMicrophones = AR.getActiveMicrophones();

            Log.d(TAG,"# microfonos = "+activeMicrophones.size());
            return "# microfonos: "+activeMicrophones.size();
        }
        else{
            Log.d(TAG,"SDK Version muy baja");

            AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

            //https://stackoverflow.com/questions/44949280/how-can-i-get-the-list-of-microphones-on-android-api-17-usb-connected-mics
            //API 23 FUCK! 
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                AudioDeviceInfo[] adi = audioManager.getDevices(AudioManager.GET_DEVICES_OUTPUTS);
                Log.d(TAG,"LIST : "+adi.length);
            }

            return "Se detecto microfono - SDK Version muy baja";
        }
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
