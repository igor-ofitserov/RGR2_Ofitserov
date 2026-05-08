package com.ofitserov.beans;

public class Data {
    private double x;
    private double y;

    public Data(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public Data() {
        this(0.0, 0.0);
    }

    public double getX() { return x; }
    public void setX(double x) { this.x = x; }

    public double getY() { return y; }
    public void setY(double y) { this.y = y; }
}