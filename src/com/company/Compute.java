package com.company;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Random;

// MG = MD*(MT+MZ)-ME*MM
// A = min(Z)*D*MT+B
public class Compute {
    private final int a;
    private final int b;
    private final int n;
    private final boolean isKahan;

    public Compute(int a, int b, int n, boolean isKahan) {
        this.a = a;
        this.b = b;
        this.n = n;
        this.isKahan = isKahan;
    }

    public float kahanSum(float[] arr) {
        float sum = 0;
        float c = 0;

        for (float num : arr) {
            float y = num - c;
            float t = sum + y;
            c = (t - sum) - y;
            sum = t;
        }

        return sum;
    }

    public float sum(float[] arr) {
        float sum = 0;

        for (float num : arr) {
            sum += num;
        }

        return sum;
    }

    public void diffArrays(float[] arr1, float[] arr2, float[] res) {
        for (int i = a; i < b; i++) {
            res[i] = arr1[i] - arr2[i];
        }
    }

    public void sumArrays(float[] arr1, float[] arr2, float[] res) {
        for (int i = a; i < b; i++) {
            res[i] = arr1[i] + arr2[i];
        }
    }

    public float maxInArray(float[] arr){
        float max = arr[a];
        for (int i = a + 1; i < b; i++) {
            if(max < arr[i]){
                max = arr[i];
            }
        }
        return max;
    }

    public float minInArray(float[] arr){
        float min = arr[a];
        for (int i = a + 1; i < b; i++) {
            if (min > arr[i]){
                min = arr[i];
            }
        }
        return min;
    }

    public void multiplyFloatArray(float[] arr, float num, float[] res) {
        for (int i = a; i < b; i++) {
            res[i] = arr[i] * num;
        }
    }

    public void sumMatrix(float[][] matrix1, float[][] matrix2, float[][] res){
        for (int i = a; i < b; i++) {
            for (int j = 0; j < n; j++) {
                res[i][j] = matrix1[i][j] + matrix2[i][j];
            }
        }
    }

    public void diffMatrix(float[][] matrix1, float[][] matrix2, float[][] res){
        for (int i = a; i < b; i++) {
            for (int j = 0; j < n; j++) {
                res[i][j] = matrix1[i][j] - matrix2[i][j];
            }
        }
    }

    public void multiplyArrayMatrix(float[][] matrix, float[] arr, float[] res) {
        float[] temp = new float[n];
        for (int j = a; j < b; j++) {
            for (int i = 0; i < n; i++) {
                temp[i] = matrix[i][j] * arr[i];
            }
            res[j] = isKahan ? kahanSum(temp) : sum(temp);
        }
    }

    public void multiplyMatrix(float[][] matrix1, float[][] matrix2, float[][] res) {
        for (int i = a; i < b; i++) {
            float[] temp = new float[n];
            for (int j = 0; j < n; j++) {
                res[i][j] = 0;
                for (int k = 0; k < n; k++) {
                    temp[k] = matrix1[i][k] * matrix2[k][j];
                }
                res[i][j] = isKahan ? kahanSum(temp) : sum(temp);
            }
        }
    }

    float[] randomArray(int n) {
        float[] res = new float[n];
        Random r = new Random();
        for (int i = 0; i < n; i++) {
            res[i] = 0 + r.nextFloat() * 100;
        }
        return res;
    }

    float[][] randomMatrix(int n) {
        float[][] res = new float[n][n];
        Random r = new Random();
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                res[i][j] = 0 + r.nextFloat() * 100;
            }
        }
        return res;
    }

    public void writeArray(float[] arr, String file) {
        try(FileWriter writer = new FileWriter(file))
        {
            StringBuilder str = new StringBuilder();
            for (int i = 0; i < n; i++){
                str.append(arr[i]);
                str.append(" ");
            }
            writer.write(String.valueOf(str));
            writer.flush();
        }
        catch(IOException ex){
            System.out.println(ex.getMessage());
        }
    }

    public void writeMatrix(float[][] matrix, String file) {
        try(FileWriter writer = new FileWriter(file))
        {
            StringBuilder str = new StringBuilder();
            for (int i = 0; i < n; i++){
                for (int j = 0; j < n; j++) {
                    str.append(matrix[i][j]);
                    str.append(" ");
                }
                str.append("\n");
            }
            writer.write(String.valueOf(str));
            writer.flush();
        }
        catch(IOException ex){
            System.out.println(ex.getMessage());
        }
    }

    public float[] readArray(String file) throws IOException {
        float[] res = new float[n];
        String str = Files.readString(Path.of(file));
        String[] arr = str.split("\\s+");
        for (int i = 0; i < n; i++) {
            res[i] = Float.parseFloat(arr[i]);
        }
        return res;
    }

    public float[][] readMatrix(String file) throws IOException {
        float[][] res = new float[n][n];
        String str = Files.readString(Path.of(file));
        String[] arr = str.split("\\s+");
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                res[i][j] = Float.parseFloat(arr[i * n + j]);
            }
        }
        return res;
    }

    public void read(Data res) throws IOException {
        String path = "src/data/";
        res.B = readArray(path + "B.txt");
        res.Z = readArray(path + "Z.txt");
        res.D = readArray(path + "D.txt");
        res.MD = readMatrix(path + "MD.txt");
        res.ME = readMatrix(path + "ME.txt");
        res.MM = readMatrix(path + "MM.txt");
        res.MT = readMatrix(path + "MT.txt");
        res.MZ = readMatrix(path + "MZ.txt");
    }

    public void write(Data res, String name) {
        writeArray(res.A,"src/data/A" + name + ".txt");
        writeMatrix(res.MG,"src/data/MG" + name + ".txt");
    }

    public float firstStep(Data res, Compute c) {
        c.sumMatrix(res.MT, res.MZ, res.MA);
        synchronized (res) {
            c.multiplyMatrix(res.MD, res.MA, res.MC);
        }
//        c.multiplyMatrix(res.MA, res.MD, res.MC);
        c.multiplyMatrix(res.ME, res.MM, res.MB);
        c.diffMatrix(res.MC, res.MB, res.MG);

        c.multiplyArrayMatrix(res.MT, res.D, res.C);
        return c.minInArray(res.Z);
    }
}
