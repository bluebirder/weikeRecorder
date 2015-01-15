package com.guomi.weikerecorder.activity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.annotation.SuppressLint;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.ImageButton;

import com.guomi.weikerecorder.R;
import com.guomi.weikerecorder.entity.CustomView;
import com.guomi.weikerecorder.util.MusicPlayer;

public class MainActivity extends ActionBarActivity implements OnClickListener, OnTouchListener {

    private Button btnStart;
    private Button btnStop;
    private Button btnPlay;

    private CustomView paint;

    private ImageButton pencil;
    private ImageButton eraser;
    private ImageButton browser;

    private MediaRecorder mMediaRecorder;
    private File recAudioFile;
    private File recJsonFile;
    private MusicPlayer mPlayer;
    private Character tools = 'p';
    private StringBuilder json = new StringBuilder();

    private InputStream in;
    private OutputStream out;

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

        pencil = (ImageButton) findViewById(R.id.pencil);
        eraser = (ImageButton) findViewById(R.id.eraser);
        browser = (ImageButton) findViewById(R.id.browser);

        btnStart.setOnClickListener(this);
        btnStop.setOnClickListener(this);
        btnPlay.setOnClickListener(this);

        pencil.setOnClickListener(this);
        eraser.setOnClickListener(this);
        browser.setOnClickListener(this);

        paint.setOnTouchListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
        case R.id.start:
            //startAudioRecorder();
            startDrawRecorder();
            break;
        case R.id.stop:
            //stopAudioRecorder();
            stopDrawRecorder();
            break;
        case R.id.play:
            //mPlayer = new MusicPlayer(MainActivity.this);
            //mPlayer.playMicFile(recAudioFile);
            playDrawFile(recJsonFile);
            break;
        case R.id.pencil:
            tools = 'p';
            break;
        case R.id.eraser:
            tools = 'e';
            break;
        case R.id.browser:
            tools = 'b';
            break;
        default:
            break;
        }
    }

    @SuppressLint({ "ClickableViewAccessibility", "DefaultLocale" })
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch (v.getId()) {
        case R.id.paint:
            float x = event.getX();
            float y = event.getY();
            String coord = String.format("%c:%f,%f;", tools, x, y);
            json.append(coord);
            break;
        default:
            break;
        }
        return false;
    }

    private void startAudioRecorder() {
        File dir = new File(getWeikeRecordDir());
        if (!dir.exists()) {
            dir.mkdirs();
        }
        recAudioFile = new File(dir, "audio.amr");
        if (recAudioFile.exists()) {
            recAudioFile.delete();
        }

        mMediaRecorder = new MediaRecorder();
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

    private void startDrawRecorder() {
        File dir = new File(getWeikeRecordDir());
        if (!dir.exists()) {
            dir.mkdirs();
        }
        recJsonFile = new File(dir, "draw.json");
        if (recJsonFile.exists()) {
            recJsonFile.delete();
        }

        try {
            recJsonFile.createNewFile();
            out = new FileOutputStream(recJsonFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void stopAudioRecorder() {
        if (recAudioFile != null) {
            mMediaRecorder.stop();
            mMediaRecorder.release();
        }
    }

    private void stopDrawRecorder() {
        if (out != null) {
            try {
                out.write(json.toString().getBytes());
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        paint = (CustomView) findViewById(R.id.paint);
        json = new StringBuilder();
    }

    private void playDrawFile(File file) {
        if (out != null) {
            try {
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private String getWeikeRecordDir() {
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            return Environment.getExternalStorageDirectory().getAbsolutePath() + "/weikeRecord/";
        }

        return Environment.getDataDirectory().getAbsolutePath() + "/weikeRecord/";
    }
}
