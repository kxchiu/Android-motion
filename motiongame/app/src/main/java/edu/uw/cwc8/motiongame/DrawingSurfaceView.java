package edu.uw.cwc8.motiongame;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

/**
 * An example SurfaceView for generating graphics on
 * @author Joel Ross
 * @version Winter 2016
 */
public class DrawingSurfaceView extends SurfaceView implements SurfaceHolder.Callback {

    private static final String TAG = "SurfaceView";

    private int viewWidth, viewHeight; //size of the view

    private Bitmap bmp; //image to draw on

    private SurfaceHolder mHolder; //the holder we're going to post updates to
    private DrawingRunnable mRunnable; //the code htat we'll want to run on a background thread
    private Thread mThread; //the background thread

    private Paint yellowPaint; //drawing variables (pre-defined for speed)
    public Ball ball;


    /**
     * We need to override all the constructors, since we don't know which will be called
     */
    public DrawingSurfaceView(Context context) {
        this(context, null);
    }

    public DrawingSurfaceView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DrawingSurfaceView(Context context, AttributeSet attrs, int defaultStyle) {
        super(context, attrs, defaultStyle);

        viewWidth = 1; viewHeight = 1; //positive defaults; will be replaced when #surfaceChanged() is called

        // register our interest in hearing about changes to our surface
        mHolder = getHolder();
        mHolder.addCallback(this);

        mRunnable = new DrawingRunnable();

        //set up drawing variables ahead of timme
        yellowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        yellowPaint.setColor(Color.YELLOW);

        ball = new Ball(100, 100, 0, 0, 100, false);
    }


    /**
     * Helper method for the "game loop"
     */
    public void update(){
        if(ball.flung == true){
            //update the "game state" here (move things around, etc.
            ball.cx += ball.dx; //move
            ball.cy += ball.dy;

            Log.v(TAG, "center x: " + ball.cx);
            Log.v(TAG, "center y: " + ball.cy);

            //slow down
            ball.dx *= 0.9 * (1 + ball.gx * 0.01f);
            ball.dy *= 0.9 * (1 + ball.gy * 0.01f);

        /* hit detection */
            if(ball.cx + ball.radius > viewWidth) { //left bound
                ball.cx = viewWidth - ball.radius;
                ball.dx *= -1;
            }
            else if(ball.cx - ball.radius < 0) { //right bound
                ball.cx = ball.radius;
                ball.dx *= -1;
            }
            else if(ball.cy + ball.radius > viewHeight) { //bottom bound
                ball.cy = viewHeight - ball.radius;
                ball.dy *= -1;
            }
            else if(ball.cy - ball.radius < 0) { //top bound
                ball.cy = ball.radius;
                ball.dy *= -1;
            }
        }
    }


    /**
     * Helper method for the "render loop"
     * @param canvas The canvas to draw on
     */
    public void render(Canvas canvas){
        if(canvas == null) return; //if we didn't get a valid canvas for whatever reason

        canvas.drawBitmap(bmp, 0, 0, null); //and then draw the BitMap onto the canvas.

        canvas.drawColor(Color.BLACK); //black out the background

        canvas.drawCircle(ball.cx, ball.cy, ball.radius, yellowPaint); //we can draw directly onto the canvas

        //draw 5 white lines of width 1 pixel
        for(int x=50; x<viewWidth-50; x++) { //most of the width
            for(int y=250; y<260; y++) { //10 pixels high
                bmp.setPixel(x, y, Color.WHITE); //we can also set individual pixels in a Bitmap (like a BufferedImage)
            }
        }
        canvas.drawBitmap(bmp, 0, 0, null); //and then draw the BitMap onto the canvas.
    }


    @Override
    public void surfaceCreated(SurfaceHolder holder) {

        // create thread only; it's started in surfaceCreated()
        Log.v(TAG, "making new thread");
        mThread = new Thread(mRunnable);
        mRunnable.setRunning(true); //turn on the runner
        mThread.start(); //start up the thread when surface is created

    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        synchronized (mHolder) { //synchronized to keep this stuff atomic
            viewWidth = width;
            viewHeight = height;
            bmp = Bitmap.createBitmap(viewWidth, viewHeight, Bitmap.Config.ARGB_8888); //new buffer to draw on

            //Remake ball
            ball = new Ball(viewWidth/2, viewHeight - 100, 0, 0, 100, false);
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        // we have to tell thread to shut down & wait for it to finish, or else
        // it might touch the Surface after we return and explode
        mRunnable.setRunning(false); //turn off
        boolean retry = true;
        while(retry) {
            try {
                mThread.join();
                retry = false;
            } catch (InterruptedException e) {
                //will try again...
            }
        }
        Log.d(TAG, "Drawing thread shut down.");
    }



    /**
     * An inner class representing a runnable that does the drawing. Animation timing could go in here.
     * http://obviam.net/index.php/the-android-game-loop/ has some nice details about using timers to specify animation
     */
    public class DrawingRunnable implements Runnable {

        private boolean isRunning; //whether we're running or not (so we can "stop" the thread)

        public void setRunning(boolean running){
            this.isRunning = running;
        }

        public void run() {
            Canvas canvas;
            while(isRunning)
            {
                canvas = null;
                try {
                    canvas = mHolder.lockCanvas(); //grab the current canvas
                    synchronized (mHolder) {
                        update(); //update the game
                        render(canvas); //redraw the screen
                    }
                }
                finally { //no matter what (even if something goes wrong), make sure to push the drawing so isn't inconsistent
                    if (canvas != null) {
                        mHolder.unlockCanvasAndPost(canvas);
                    }
                }
            }
        }
    }
}