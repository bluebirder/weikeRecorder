package com.guomi.weikerecorder.activity;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Timer;
import java.util.TimerTask;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
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
import com.guomi.weikerecorder.util.PaintUtils;

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
    private long timestamp;

    private OutputStream out;

    private Timer timer;
    final Handler handler = new Handler();

    private Canvas mCanvas;
    private Bitmap mBitmap;
    private Paint mPaint;
    private float sx;
    private float sy;
    private float ex;
    private float ey;

    private boolean isDrawing = false;

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

    @SuppressLint("ClickableViewAccessibility")
    private void setupViews() {
        btnStart = (Button) findViewById(R.id.start);
        btnStop = (Button) findViewById(R.id.stop);
        btnPlay = (Button) findViewById(R.id.play);
        paint = (CustomView) findViewById(R.id.paint);

        pencil = (ImageButton) findViewById(R.id.pencil);
        eraser = (ImageButton) findViewById(R.id.eraser);
        browser = (ImageButton) findViewById(R.id.browser);

        refreshTools();

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
            btnStart.getBackground().setAlpha(160);
            btnPlay.getBackground().setAlpha(255);
            if (isDrawing) {
                // break; // 是否重新开始
            }
            isDrawing = true;
            timestamp = System.currentTimeMillis();
            startAudioRecorder();
            startDrawRecorder();
            break;
        case R.id.stop:
            if (!isDrawing) {
                break;
            }
            btnStart.getBackground().setAlpha(255);
            btnPlay.getBackground().setAlpha(255);
            isDrawing = false;
            stopAudioRecorder();
            stopDrawRecorder();
            break;
        case R.id.play:
            btnStart.getBackground().setAlpha(255);
            btnPlay.getBackground().setAlpha(160);
            timer = new Timer();
            playAudioFile(recAudioFile);
            playDrawFile(recJsonFile);
            break;
        case R.id.pencil:
            tools = 'p';
            refreshTools();
            break;
        case R.id.eraser:
            tools = 'e';
            refreshTools();
            break;
        case R.id.browser:
            tools = 'b';
            refreshTools();
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
            if (!"".equals(json.toString().trim())) {
                if (paint.isCut()) {
                    json.append("#;#");
                } else {
                    json.append(";");
                }
            }
            float x = event.getX();
            float y = event.getY();
            String coord = String.format("%c:%f,%f,%d", tools, x, y, System.currentTimeMillis() - timestamp);
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
        paint.clearCanvas();

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
        paint.clearCanvas();
        json = new StringBuilder();
    }

    private void playAudioFile(File file) {
        if (file == null) {
            file = new File(getWeikeRecordDir(), "audio.amr");
        }
        if (!file.exists()) {
            return;
        }

        mPlayer = new MusicPlayer(MainActivity.this);
        mPlayer.playMicFile(file);
    }

    private void playDrawFile(File file) {
        if (file == null) {
            file = new File(getWeikeRecordDir(), "draw.json");
        }
        if (!file.exists()) {
            return;
        }

        StringBuilder sb = new StringBuilder();
        try {
            FileInputStream fis = new FileInputStream(file);
            byte[] buf = new byte[1024];
            while ((fis.read(buf)) != -1) {
                sb.append(new String(buf));
                buf = new byte[1024];//重新生成，避免和上次读取的数据重复
            }
            fis.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        mBitmap = Bitmap.createBitmap(paint.getWidth(), paint.getHeight(), Bitmap.Config.ARGB_8888);
        mPaint = new Paint();
        mPaint.setStrokeWidth(6);
        if ("".equals(sb.toString().trim())) {
            paint.drawImg(mBitmap, mPaint);
            return;
        }

        mCanvas = new Canvas(mBitmap);
        String[] segments = sb.toString().trim().split("#;#");
        for (String seg : segments) {
            String[] coords = seg.split(";");
            int last = coords.length - 1;
            for (int i = 0; i < last; i++) {
                String[] p1 = coords[i].split(":");
                final String[] xy1 = p1[1].split(",");
                String[] p2 = coords[i + 1].split(":");
                final String[] xy2 = p2[1].split(",");
                sx = Float.valueOf(xy1[0]);
                sy = Float.valueOf(xy1[1]);
                ex = Float.valueOf(xy2[0]);
                ey = Float.valueOf(xy2[1]);
                // updateUI(sx, sy, ex, ey);
                long times = Long.valueOf(xy1[2]);
                final Runnable mUpdateResults = new UpdateRunnable(p1[0].charAt(0), sx, sy, ex, ey);
                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        handler.post(mUpdateResults);
                    }
                }, times);
            }
        }
    }

    private String getWeikeRecordDir() {
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            return Environment.getExternalStorageDirectory().getAbsolutePath() + "/weikeRecord/";
        }

        return Environment.getDataDirectory().getAbsolutePath() + "/weikeRecord/";
    }

    private void updateUI(char tool, float x1, float y1, float x2, float y2) {
        if (mCanvas == null || mPaint == null || mBitmap == null) {
            return;
        }

        PaintUtils.changeTools(mPaint, tool);

        mCanvas.drawLine(x1, y1, x2, y2, mPaint);
        paint.drawImg(mBitmap, mPaint);
    }

    private class UpdateRunnable implements Runnable {
        private char tool;
        private float x1;
        private float y1;
        private float x2;
        private float y2;

        public UpdateRunnable(char tool, float x1, float y1, float x2, float y2) {
            this.tool = tool;
            this.x1 = x1;
            this.y1 = y1;
            this.x2 = x2;
            this.y2 = y2;
        }

        @Override
        public void run() {
            updateUI(tool, x1, y1, x2, y2);
        }
    }

    private void refreshTools() {
        switch (tools) {
        case 'p':
            pencil.getBackground().setAlpha(180);
            eraser.getBackground().setAlpha(255);
            browser.getBackground().setAlpha(255);
            break;
        case 'e':
            pencil.getBackground().setAlpha(255);
            eraser.getBackground().setAlpha(180);
            browser.getBackground().setAlpha(255);
            break;
        case 'b':
            pencil.getBackground().setAlpha(255);
            eraser.getBackground().setAlpha(255);
            browser.getBackground().setAlpha(180);
            break;
        }
        paint.changeTools(tools);
    }
}
