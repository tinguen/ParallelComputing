package com.company;

import java.io.IOException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;

// MG = MD*(MT+MZ)-ME*MM
// A = min(Z)*D*MT+B
public class Lab6 {

    public static void main(String[] args) throws IOException {
        int n = 100;
        int threadCount = 4;

        Data res = new Data(n);
        Compute a = new Compute(0, 100, 100, true);

        a.read(res);
        res.startTime = System.nanoTime();

        new ForkJoinPool(threadCount).invoke(new SubTask1(res, n, threadCount, 0));
        new ForkJoinPool(threadCount).invoke(new SubTask2(res, n, threadCount, 0));

        long endTime = System.nanoTime();
        System.out.println("Time: " + (endTime - res.startTime) / 1000000 + " ms");

        a.write(res, "Lab6");
    }
}

class SubTask1 extends RecursiveAction {

    private final Data res;
    int n;
    int threadCount;
    int num;

    SubTask1(Data res, int n, int threadCount, int num) {
        this.res = res;
        this.n = n;
        this.threadCount = threadCount;
        this.num = num;
    }

    @Override
    public void compute() {
        Compute c = new Compute(n / threadCount * num,
                ((num != threadCount - 1) ? n / threadCount * (num + 1) : n), n, false);

        if(num < threadCount) {
            SubTask1 subTask1 = new SubTask1(res, n, threadCount, num + 1);
            subTask1.fork();

            System.out.println("Task " + (num + 1) + " start");

            c.sumMatrix(res.MT, res.MZ, res.MA);
            c.multiplyMatrix(res.ME, res.MM, res.MB);
            c.multiplyArrayMatrix(res.MT, res.D, res.C);

            float min = c.minInArray(res.Z);

            if (res.min > min) {
                res.min = min;
            }

            subTask1.join();
        }
    }
}

class SubTask2 extends RecursiveAction {

    private final Data res;
    int n;
    int threadCount;
    int num;

    SubTask2(Data res, int n, int threadCount, int num) {
        this.res = res;
        this.n = n;
        this.threadCount = threadCount;
        this.num = num;
    }

    @Override
    public void compute() {
        Compute c = new Compute(n / threadCount * num,
                ((num != threadCount - 1) ? n / threadCount * (num + 1) : n), n, true);

        if(num < threadCount) {
            SubTask2 subTask2 = new SubTask2(res, n, threadCount, num + 1);
            subTask2.fork();

            c.multiplyMatrix(res.MD, res.MA, res.MC);
            c.diffMatrix(res.MC, res.MB, res.MG);
            c.multiplyFloatArray(res.C, res.min, res.E);
            c.sumArrays(res.B, res.E, res.A);
            System.out.println("Task " + (num + 1) + " end");

            subTask2.join();
        }
    }
}
