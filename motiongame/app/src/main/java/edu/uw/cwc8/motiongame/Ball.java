package edu.uw.cwc8.motiongame;

/**
 * A simple struct to hold a shape
 */
public class Ball {

    public float cx; //center
    public float cy;
    public float radius; //radius
    public float dx; //velocity
    public float dy;
    public float gx; //gravity/acceleration
    public float gy;
    public boolean flung;

    public Ball(float cx, float cy, float gx, float gy, float radius, boolean flung) {
        this.cx = cx;
        this.cy = cy;
        this.radius = radius;
        this.dx = 0;
        this.dy = 0;
        this.gx = 0;
        this.gy = 0;
        this.flung = false;
    }

    public float getRadius() {
        return this.radius;
    }

    public void setRadius(float radius){
        this.radius = radius;
    }

    public void setX(float cx){
        this.cx = cx;
    }

    public float getX(){
        return this.cx;
    }

    public void setY(float cy){
        this.cy = cy;
    }

    public float getY() {
        return this.cy;
    }
}