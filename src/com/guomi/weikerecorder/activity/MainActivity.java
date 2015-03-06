package com.guomi.weikerecorder.activity;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.media.MediaRecorder;
import android.net.Uri;
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
import android.widget.TextView;
import android.widget.Toast;

import com.guomi.weikerecorder.R;
import com.guomi.weikerecorder.entity.CallbackBundle;
import com.guomi.weikerecorder.entity.CustomView;
import com.guomi.weikerecorder.entity.OpenFileDialog;
import com.guomi.weikerecorder.util.MusicPlayer;
import com.guomi.weikerecorder.util.PaintUtils;

public class MainActivity extends ActionBarActivity implements OnClickListener, OnTouchListener {
    private static final int FILE_SELECT_CODE = 1;
    private static int openfileDialogId = 0;

    private Button btnStart;
    private Button btnStop;
    private Button btnPlay;
    private Button btnPreview;
    private ImageButton previous; // 上一页
    private ImageButton next; // 下一页
    private TextView pageNum;

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
    private int currentPage = 1;
    private int maxPage = 1;
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
        btnPreview = (Button) findViewById(R.id.review);
        paint = (CustomView) findViewById(R.id.paint);
        pageNum = (TextView) findViewById(R.id.pageNum);

        pencil = (ImageButton) findViewById(R.id.pencil);
        eraser = (ImageButton) findViewById(R.id.eraser);
        browser = (ImageButton) findViewById(R.id.browser);
        previous = (ImageButton) findViewById(R.id.previous);
        next = (ImageButton) findViewById(R.id.next);

        refreshTools();

        btnStart.setOnClickListener(this);
        btnStop.setOnClickListener(this);
        btnPlay.setOnClickListener(this);
        btnPreview.setOnClickListener(this);
        previous.setOnClickListener(this);
        next.setOnClickListener(this);

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
        case R.id.review:
            showFileChooser();
            break;
        case R.id.previous:
            if (currentPage == 1) {
                return;
            }
            currentPage--;
            displayImg('l');
            String dl = String.format(";l:%d", System.currentTimeMillis() - timestamp);
            appendData(currentPage + 1, dl);
            break;
        case R.id.next:
            currentPage++;
            int tm = maxPage;
            displayImg('r');
            if (currentPage > tm) {
                json.append("#p#");
            }
            String dr = String.format(";r:%d", System.currentTimeMillis() - timestamp);
            appendData(currentPage - 1, dr);
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
            String[] pages = json.toString().trim().split("#p#");
            String current = "";
            if (pages.length >= currentPage) {
                current = pages[currentPage - 1];
            }

            if (!"".equals(current)) {
                if (paint.isCut()) {
                    current += "#;#";
                } else {
                    current += ";";
                }
            }
            float x = event.getX();
            float y = event.getY();
            String coord = String.format("%c:%f,%f,%d", tools, x, y, System.currentTimeMillis() - timestamp);
            current += coord;
            json = new StringBuilder();
            for (int i = 0; i < pages.length; i++) {
                if (i > 0) {
                    json.append("#p#");
                }
                if (i == currentPage - 1) {
                    json.append(current);
                } else {
                    json.append(pages[i]);
                }
            }
            if (currentPage > pages.length) {
                for (int i = pages.length; i < currentPage; i++) {
                    json.append("#p#");
                }
                json.append(current);
            }
            break;
        default:
            break;
        }
        return false;
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        if (id == openfileDialogId) {
            Map<String, Integer> images = new HashMap<String, Integer>();
            // 下面几句设置各文件类型的图标， 需要你先把图标添加到资源文件夹
            images.put(OpenFileDialog.sRoot, R.drawable.filedialog_root); // 根目录图标
            images.put(OpenFileDialog.sParent, R.drawable.filedialog_folder_up); //返回上一层的图标
            images.put(OpenFileDialog.sFolder, R.drawable.filedialog_folder); //文件夹图标
            images.put("ppt", R.drawable.filedialog_pptfile); //wav文件图标
            images.put(OpenFileDialog.sEmpty, R.drawable.filedialog_root);
            Dialog dialog = OpenFileDialog.createDialog(id, this, "打开文件", new CallbackBundle() {
                @Override
                public void callback(Bundle bundle) {
                    String filepath = bundle.getString("path");
                    setTitle(filepath); // 把文件路径显示在标题上
                    // PPTUtils.doPPTtoImage(new File(filepath)); // 暂时无法实现
                }
            }, ".ppt;", images);
            return dialog;
        }
        return null;
    }

    private void showFileChooser() {
        showDialog(openfileDialogId);
    }

    private void showFileChooser(String dir) {
        if (dir == null) {
            dir = getWeikeRecordDir();
        }
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        Uri uri = Uri.fromFile(new File(dir));
        intent.setDataAndType(uri, "application/msword");

        try {
            startActivityForResult(Intent.createChooser(intent, "请选择PPT文件"), FILE_SELECT_CODE);
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(this, "Please install a File Manager.", Toast.LENGTH_SHORT).show();
        }
    }

    private void displayImg(char direction) {
        pageNum.setText(String.valueOf(currentPage));
        if (currentPage > maxPage) {
            paint.clearCanvas();
            maxPage = currentPage;
            return;
        }
        int prv = currentPage;
        if (direction == 'l') {
            prv = prv + 1;
        } else {
            prv = prv - 1;
        }
        String[] pages = json.toString().split("#p#");
        String display = "";
        if (pages.length >= currentPage) {
            display = pages[currentPage - 1];
        }
        paint.clearCanvas();
        drawWithJson(false, display);
    }

    private void startAudioRecorder() {
        File dir = new File(getWeikeRecordDir());
        if (!dir.exists()) {
            dir.mkdirs();
        }
        recAudioFile = new File(dir, "audio.3gp");
        if (!recAudioFile.exists()) {
            try {
                recAudioFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        mMediaRecorder = new MediaRecorder();
        mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
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
        currentPage = 1;
        maxPage = 1;
        json = new StringBuilder();
        pageNum.setText("1");

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

        currentPage = 1;
        maxPage = 1;
        json = sb;
        String firstPage = json.toString().trim().split("#p#")[0];
        pageNum.setText("1");
        drawWithJson(true, firstPage);
    }

    public static String getWeikeRecordDir() {
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            return Environment.getExternalStorageDirectory().getAbsolutePath() + "/weikeRecord/";
        }

        return Environment.getDataDirectory().getAbsolutePath() + "/weikeRecord/";
    }

    private void drawWithJson(boolean withTime, String json) {
        mBitmap = Bitmap.createBitmap(paint.getWidth(), paint.getHeight(), Bitmap.Config.ARGB_8888);
        mPaint = new Paint();
        mPaint.setStrokeWidth(6);
        if ("".equals(json)) {
            paint.drawImg(mBitmap, mPaint);
            return;
        }

        mCanvas = new Canvas(mBitmap);
        String[] segments = json.split("#;#");
        for (String seg : segments) {
            String[] coords = seg.split(";");
            int last = coords.length - 1;
            for (int i = 0; i < last; i++) {
                String[] p1 = coords[i].split(":");
                char tool1 = p1[0].charAt(0);
                if (tool1 == 'l' || tool1 == 'r') {
                    if (!withTime) {
                        continue;
                    }
                    long times = Long.valueOf(p1[1]);
                    final Runnable mUpdateResults = new UpdateRunnable(tool1, sx, sy, ex, ey);
                    timer.schedule(new TimerTask() {
                        @Override
                        public void run() {
                            handler.post(mUpdateResults);
                        }
                    }, times);
                    continue;
                }
                final String[] xy1 = p1[1].split(",");
                String[] p2 = coords[i + 1].split(":");
                char tool2 = p2[0].charAt(0);
                if (tool2 == 'l' || tool2 == 'r') {
                    if (!withTime) {
                        continue;
                    }
                    long times = Long.valueOf(p2[1]);
                    final Runnable mUpdateResults = new UpdateRunnable(tool2, sx, sy, ex, ey);
                    timer.schedule(new TimerTask() {
                        @Override
                        public void run() {
                            handler.post(mUpdateResults);
                        }
                    }, times);
                    continue;
                }
                final String[] xy2 = p2[1].split(",");
                sx = Float.valueOf(xy1[0]);
                sy = Float.valueOf(xy1[1]);
                ex = Float.valueOf(xy2[0]);
                ey = Float.valueOf(xy2[1]);
                if (withTime) {
                    long times = Long.valueOf(xy1[2]);
                    final Runnable mUpdateResults = new UpdateRunnable(tool1, sx, sy, ex, ey);
                    timer.schedule(new TimerTask() {
                        @Override
                        public void run() {
                            handler.post(mUpdateResults);
                        }
                    }, times);
                } else {
                    updateUI(tool1, sx, sy, ex, ey);
                }
            }
        }
    }

    private void updateUI(char tool, float x1, float y1, float x2, float y2) {
        if (mCanvas == null || mPaint == null || mBitmap == null) {
            return;
        }

        if (tools == 'l') {
            currentPage--;
            pageNum.setText(String.valueOf(currentPage));
            String[] data = json.toString().trim().split("#p#");
            drawWithJson(true, data[currentPage - 1]);
            return;
        }
        if (tools == 'r') {
            currentPage++;
            pageNum.setText(String.valueOf(currentPage));
            String[] data = json.toString().trim().split("#p#");
            drawWithJson(true, data[currentPage - 1]);
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

    private void appendData(int page, String data) {
        boolean appendPg = json.toString().endsWith("#p#");
        String[] pages = json.toString().split("#p#");
        if (page > pages.length) {
            return;
        }
        pages[page - 1] = pages[page - 1] + data;
        json = new StringBuilder();
        for (int i = 0; i < pages.length; i++) {
            if (i > 0) {
                json.append("#p#");
            }
            json.append(pages[i]);
        }
        if (appendPg) {
            json.append("#p#");
        }
    }

}
