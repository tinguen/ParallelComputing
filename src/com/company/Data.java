package com.company;

public class Data {
    public float min = Float.MAX_VALUE;
    public float[] B, Z, D, C, A, E;
    public float[][] MD, ME, MM, MT, MZ, MB, MA, MG, MC;
    public long startTime;

    public Data(int n) {
        this.C = new float[n];
        this.A = new float[n];
        this.E = new float[n];

        this.MB = new float[n][n];
        this.MA = new float[n][n];
        this.MG = new float[n][n];
        this.MC = new float[n][n];
    }
}
