package com.guomi.weikerecorder.activity;

import java.io.File;
import java.io.IOException;

import android.annotation.SuppressLint;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.Button;

import com.guomi.weikerecorder.R;
import com.guomi.weikerecorder.entity.CustomView;
import com.guomi.weikerecorder.util.MusicPlayer;

public class MainActivity extends ActionBarActivity implements OnClickListener, OnTouchListener {

    private Button btnStart;
    private Button btnStop;
    private Button btnPlay;

    private CustomView paint;

    private MediaRecorder mMediaRecorder;
    private File recAudioFile;
    private MusicPlayer mPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setupViews();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setupViews() {
        btnStart = (Button) findViewById(R.id.start);
        btnStop = (Button) findViewById(R.id.stop);
        btnPlay = (Button) findViewById(R.id.play);
        paint = (CustomView) findViewById(R.id.paint);

        btnStart.setOnClickListener(this);
        btnStop.setOnClickListener(this);
        btnPlay.setOnClickListener(this);
        paint.setOnTouchListener(this);

        recAudioFile = new File("/mnt/sdcard", "new.amr");
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
        case R.id.start:
            startRecorder();
            break;
        case R.id.stop:
            stopRecorder();
            break;
        case R.id.play:
            mPlayer = new MusicPlayer(MainActivity.this);
            mPlayer.playMicFile(recAudioFile);
            break;
        default:
            break;
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch (v.getId()) {
        case R.id.paint:
            System.out.println("touch");
            break;
        default:
            break;
        }
        return false;
    }

    private void startRecorder() {
        mMediaRecorder = new MediaRecorder();
        if (recAudioFile.exists()) {
            recAudioFile.delete();
        }

        mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.DEFAULT);
        mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);
        mMediaRecorder.setOutputFile(recAudioFile.getAbsolutePath());
        try {
            mMediaRecorder.prepare();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        mMediaRecorder.start();
    }

    private void stopRecorder() {
        if (recAudioFile != null) {
            mMediaRecorder.stop();
            mMediaRecorder.release();
        }
    }
}
