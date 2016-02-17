package edu.uw.cwc8.motiongame;

import android.animation.AnimatorInflater;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.SoundPool;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v4.view.MotionEventCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;

import java.util.Random;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private static final String TAG = "MainActivity";
    private DrawingSurfaceView view;
    private Button button;
    private GestureDetectorCompat mDetector;
    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private float gravityX;
    private float gravityY;

    private SoundPool mSoundPool;
    private int[] soundIds;
    private boolean[] loadedSound;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initializeSoundPool();

        view = (DrawingSurfaceView)findViewById(R.id.surfaceView);
        button = (Button)findViewById(R.id.btnReset);

        mDetector = new GestureDetectorCompat(this, new MyGestureListener());
        mSensorManager = (SensorManager)getSystemService(Context.SENSOR_SERVICE);
        //get the first option for the particular sensor
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);
        if (mAccelerometer == null) { //if we don't have accelerometer, exit/finish
            Log.v(TAG, "No accelerometer");
            finish();
        }

        ValueAnimator animator = ValueAnimator.ofFloat(0.0f, 1.0f);
        animator.setDuration(1000);
        animator.start();

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                view.ball.cx = view.getWidth() / 2;
                view.ball.cy = view.getHeight();
                playSound(0);
            }
        });
    }

    //we only register sensor when actually using the app to save battery
    @Override
    protected void onResume() {
        //register sensor
        mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);

        super.onResume();
    }

    @Override
    protected void onPause() {
        //unregister sensor
        mSensorManager.unregisterListener(this, mAccelerometer);

        super.onPause();
    }



    @Override
    public boolean onTouchEvent(MotionEvent event) {
        Log.v(TAG, "" + event);

        boolean gesture = mDetector.onTouchEvent(event);
        if(gesture) return true;

        int action = MotionEventCompat.getActionMasked(event);

        switch(action){
            case MotionEvent.ACTION_DOWN:
                Log.v(TAG,"Finger down!");
                return true;
            default:
                return super.onTouchEvent(event);
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        //Log.v(TAG, "x=" + event.values[0] + ", y="+ event.values[1] +", z=" + event.values[2]);
        gravityX = event.values[0];
        gravityY = event.values[1];
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    //helper method for setting up the sound pool
    @SuppressWarnings("deprecation")
    private void initializeSoundPool(){

        final int MAX_STREAMS = 4;

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            AudioAttributes attribs = new AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .setUsage(AudioAttributes.USAGE_GAME)
                    .build();

            mSoundPool = new SoundPool.Builder()
                    .setMaxStreams(MAX_STREAMS)
                    .setAudioAttributes(attribs)
                    .build();
        } else {
            mSoundPool = new SoundPool(MAX_STREAMS, AudioManager.STREAM_MUSIC, 0);
        }

        soundIds = new int[5];
        loadedSound = new boolean[5];

        mSoundPool.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener() {
            @Override
            public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {
                if (status == 0) {
                    if(sampleId == soundIds[0]) {loadedSound[0] = true; playSound(0);}
                    else if(sampleId == soundIds[1]) loadedSound[1] = true;
                    else if(sampleId == soundIds[2]) loadedSound[2] = true;
                    else if(sampleId == soundIds[3]) loadedSound[3] = true;
                    else if(sampleId == soundIds[4]) loadedSound[4] = true;
                }
            }
        });

        soundIds[0] = mSoundPool.load(this, R.raw.saber_on, 0);
        soundIds[1] = mSoundPool.load(this, R.raw.saber_swing1, 0);
        soundIds[2] = mSoundPool.load(this, R.raw.saber_swing2, 0);
        soundIds[3] = mSoundPool.load(this, R.raw.saber_swing3, 0);
        soundIds[4] = mSoundPool.load(this, R.raw.saber_swing4, 0);
    }

    public void playSound(int index){
        if(loadedSound[index]){
            mSoundPool.play(soundIds[index],1,1,1,0,1);
        }
    }

    class MyGestureListener extends GestureDetector.SimpleOnGestureListener {

        @Override
        public boolean onDown(MotionEvent e) {

            return false; //let others respond as well
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {

            Random rand = new Random();
            int songPick = rand.nextInt(3) + 1;

            float scaleFactor = .01f;

            //fling!
            Log.v(TAG, "Fling! " + velocityX + ", " + velocityY);
            if (velocityY < 0) {
                view.ball.dx = velocityX * scaleFactor * (1 + gravityX * 0.05f);
                view.ball.dy = velocityY * scaleFactor * (1 + gravityY * -0.05f);
                Log.v(TAG, "" + songPick);
                playSound(songPick);
                Log.v(TAG, "" + view.ball.dx);
                Log.v(TAG, "" + view.ball.dy);
            } else {
                playSound(4);
            }

            return true; //we got this
        }
    }
}
