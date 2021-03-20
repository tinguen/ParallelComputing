package com.company;

import java.io.IOException;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.Semaphore;

// MG = MD*(MT+MZ)-ME*MM
// A = min(Z)*D*MT+B
public class Lab2 {
    public static void main(String[] args) throws IOException {
        int n = 100;
        int threadCount = 4;

        Data res = new Data(n);
        Compute a = new Compute(0, 100, 100, true);
        CyclicBarrier cb1 = new CyclicBarrier(threadCount);
        CyclicBarrier cb2 = new CyclicBarrier(threadCount);
        CyclicBarrier cb0 = new CyclicBarrier(threadCount);
        Semaphore s1 = new Semaphore(1, true);
        Semaphore s2 = new Semaphore(1, true);

        a.read(res);
        res.startTime = System.nanoTime();
        for (int i = 0; i < threadCount; i++) {
            new Thread(() -> {
                try {
                    thread(res, n, threadCount, cb1, cb2, cb0, s1, s2);
                }
                catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }).start();
        }
    }

    public static void thread(Data res, int n, int threadCount,
                              CyclicBarrier cb1, CyclicBarrier cb2, CyclicBarrier cb0, Semaphore s1, Semaphore s2) throws InterruptedException {
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

        s2.acquire();
        c.multiplyMatrix(res.MD, res.MA, res.MC);
        s2.release();
        c.multiplyMatrix(res.ME, res.MM, res.MB);
        c.diffMatrix(res.MC, res.MB, res.MG);

        c.multiplyArrayMatrix(res.MT, res.D, res.C);
        float min = c.minInArray(res.Z);

        s2.acquire();
        if (res.min > min) {
            res.min = min;
        }
        s2.release();

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
            c.write(res, "Lab2");
        }
    }
}
