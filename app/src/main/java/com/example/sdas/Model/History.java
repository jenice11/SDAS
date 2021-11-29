package com.example.sdas.Model;

public class History {
    private String date;
    private String risk;
    private String time;
    private double distance;
    private double latitudeA, latitudeB, longitudeA,longitudeB;

    public History() {
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getRisk() {
        return risk;
    }

    public void setRisk(String risk) {
        this.risk = risk;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    public double getLatitudeA() {
        return latitudeA;
    }

    public void setLatitudeA(double latitudeA) {
        this.latitudeA = latitudeA;
    }

    public double getLatitudeB() {
        return latitudeB;
    }

    public void setLatitudeB(double latitudeB) {
        this.latitudeB = latitudeB;
    }

    public double getLongitudeA() {
        return longitudeA;
    }

    public void setLongitudeA(double longitudeA) {
        this.longitudeA = longitudeA;
    }

    public double getLongitudeB() {
        return longitudeB;
    }

    public void setLongitudeB(double longitudeB) {
        this.longitudeB = longitudeB;
    }
}
