package com.ta.noisecancellation;

import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.media.audiofx.AcousticEchoCanceler;
import android.media.audiofx.AutomaticGainControl;
import android.media.audiofx.NoiseSuppressor;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.View;
import android.widget.Button;

public class NSActivity extends AppCompatActivity {
    AudioManager am = null;
    AudioRecord record = null;
    AudioTrack track = null;
    private Thread mAudioThread;
    private boolean shouldPlay = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ns);
        findViewById(R.id.mode_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                modeChange(v);
            }
        });
        findViewById(R.id.play_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                play(v);
            }
        });

        setVolumeControlStream(AudioManager.MODE_IN_COMMUNICATION);
        init();

       /* findViewById(R.id.record_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });*/
        mAudioThread = new Thread() {
            @Override
            public void run() {
                if (!this.isInterrupted())
                    recordAndPlay();
            }
        };
        mAudioThread.start();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    private void init() {
        int min = AudioRecord.getMinBufferSize(8000, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);
        record = new AudioRecord(MediaRecorder.AudioSource.VOICE_RECOGNITION, 8000, AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT, min);

        int maxJitter = AudioTrack.getMinBufferSize(8000, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT);
        track = new AudioTrack(AudioManager.MODE_IN_COMMUNICATION, 8000, AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT, maxJitter, AudioTrack.MODE_STREAM);

        if (AutomaticGainControl.isAvailable()) {
            AutomaticGainControl.create(record.getAudioSessionId());
          //  AutomaticGainControl.create(track.getAudioSessionId());
        }
        if (NoiseSuppressor.isAvailable()) {
            NoiseSuppressor.create(record.getAudioSessionId());
            NoiseSuppressor.create(track.getAudioSessionId());
        }

        if (AcousticEchoCanceler.isAvailable()) {
            AcousticEchoCanceler.create(record.getAudioSessionId());
            AcousticEchoCanceler.create(track.getAudioSessionId());
        }

    }

    private void recordAndPlay() {
        short[] lin = new short[1024];
        int num = 0;
        am = (AudioManager) this.getSystemService(Context.AUDIO_SERVICE);
        am.setMode(AudioManager.MODE_IN_COMMUNICATION);
        am.setParameters("noise_suppression=auto");
        record.startRecording();
        track.play();

        while (shouldPlay) {
            num = record.read(lin, 0, 1024);
            track.write(lin, 0, num);
        }
    }

    boolean isSpeaker = false;

    public void modeChange(View view) {
        Button modeBtn = (Button) findViewById(R.id.mode_button);
        if (am.isSpeakerphoneOn()) {
            am.setSpeakerphoneOn(false);
            isSpeaker = false;
            modeBtn.setText("Speaker Mode");
        } else {
            am.setSpeakerphoneOn(true);
            isSpeaker = true;
            modeBtn.setText("Call Mode");
        }
    }

    boolean isPlaying = true;

    public void play(View view) {
        Button playBtn = (Button) findViewById(R.id.play_button);
        if (isPlaying) {
            record.stop();
            track.pause();
            isPlaying = false;
            playBtn.setText("Record/Play");
        } else {
            record.startRecording();
            track.play();
            isPlaying = true;
            playBtn.setText("Pause");
        }
    }

    @Override
    public void onBackPressed() {
        if (isPlaying) {
            record.stop();
            track.stop();
            isPlaying = false;
        }
        shouldPlay = false;
        mAudioThread.interrupt();
        record.release();
        track.release();
        super.onBackPressed();

    }

    @Override
    protected void onDestroy() {
        if (isPlaying) {
            record.stop();
            track.stop();
            isPlaying = false;
        }
        shouldPlay = false;
        mAudioThread.interrupt();
        record.release();
        track.release();
        super.onDestroy();
    }

    // onClick of backbutton finishes the activity.
   /* @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            finish();
        }
        return super.onKeyDown(keyCode, event);
    }*/

    /*private class RecordAudio extends AsyncTask<Void, Integer, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            isRecording = true;
            try {
                DataOutputStream dos = new DataOutputStream(
                        new BufferedOutputStream(new FileOutputStream(
                                outputFile)));
              *//*  int bufferSize = AudioRecord.getMinBufferSize(frequency,
                        channelConfiguration, audioEncoding);
*//*
                int bufferSize = AudioRecord.getMinBufferSize(SAMPLING_RATE,
                        AudioFormat.CHANNEL_IN_MONO,
                        AudioFormat.ENCODING_PCM_16BIT);
                *//*AudioRecord audioRecord = new AudioRecord(
                        MediaRecorder.AudioSource.MIC, frequency,
                        channelConfiguration, audioEncoding, bufferSize);*//*

                AudioRecord audioRecord = new AudioRecord(
                        MediaRecorder.AudioSource.MIC, SAMPLING_RATE,
                        AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, bufferSize);

                short[] buffer = new short[bufferSize];
                audioRecord.startRecording();
                int r = 0;
                while (isRecording) {
                    int bufferReadResult = audioRecord.read(buffer, 0,
                            bufferSize);
                    for (int i = 0; i < bufferReadResult; i++) {
                        dos.writeShort(buffer[i]);
                    }
                    publishProgress(r);
                    r++;
                }
                audioRecord.stop();
                audioRecord.release();
                dos.close();
            } catch (Throwable t) {
                Log.e("AudioRecord", "Recording Failed");
            }
            return null;
        }

        protected void onProgressUpdate(Integer... progress) {
            //statusText.setText(progress[0].toString());
            Log.e("Recording", Arrays.toString(progress));
        }

        protected void onPostExecute(Void result) {
            record.setEnabled(true);
            stop.setEnabled(false);
            play.setEnabled(true);
        }
    }*/
}
