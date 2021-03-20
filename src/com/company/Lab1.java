package com.company;

import java.io.IOException;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;

// MG = MD*(MT+MZ)-ME*MM
// A = min(Z)*D*MT+B
public class Lab1 {

    public static void main(String[] args) throws  IOException {
        int n = 100;
        int threadCount = 4;

        Data res = new Data(n);
        Compute a = new Compute(0, 100, 100, true);
        CyclicBarrier cb1 = new CyclicBarrier(threadCount);
        CyclicBarrier cb2 = new CyclicBarrier(threadCount);
        CyclicBarrier cb0 = new CyclicBarrier(threadCount);

        a.read(res);
        res.startTime = System.nanoTime();

        for (int i = 0; i < threadCount; i++) {
            new Thread(() -> thread(res, n, threadCount, cb1, cb2, cb0)).start();
        }
    }

    public static void thread(Data res, int n, int threadCount,
                              CyclicBarrier cb1, CyclicBarrier cb2, CyclicBarrier cb0) {
        String name = Thread.currentThread().getName();
        int num = Integer.parseInt(name.substring(name.length() - 1));
        Compute c = new Compute(n / threadCount * num,
                ((num != threadCount - 1) ? n / threadCount * (num + 1) : n), n, true);
        System.out.println("Task " + (num + 1) + " start");

        c.sumMatrix(res.MT, res.MZ, res.MA);
        try {
            cb0.await();
        } catch (BrokenBarrierException | InterruptedException e) {
            e.printStackTrace();
        }
        synchronized (res) {
            c.multiplyMatrix(res.MD, res.MA, res.MC);
        }
        c.multiplyMatrix(res.ME, res.MM, res.MB);
        c.diffMatrix(res.MC, res.MB, res.MG);

        c.multiplyArrayMatrix(res.MT, res.D, res.C);
        float min = c.minInArray(res.Z);

        synchronized (res) {
            if (res.min > min) {
                res.min = min;
            }
        }

        try {
            cb1.await();
        } catch (BrokenBarrierException | InterruptedException e) {
            e.printStackTrace();
        }

        c.multiplyFloatArray(res.C, res.min, res.E);
        c.sumArrays(res.B, res.E, res.A);

        try {
            cb2.await();
        } catch (BrokenBarrierException | InterruptedException e) {
            e.printStackTrace();
        }

        System.out.println("Task " + (num + 1) + " end");

        if(num == 0) {
            long endTime = System.nanoTime();
            System.out.println("Time: " + (endTime - res.startTime) / 1000000 + " ms");
            c.write(res, "Lab1");
        }
    }
}
