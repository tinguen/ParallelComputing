package com.company;

import java.io.IOException;
import java.util.concurrent.*;

// MG = MD*(MT+MZ)-ME*MM
// A = min(Z)*D*MT+B
public class Lab3 {
    public static void main(String[] args) throws IOException, InterruptedException {
        int n = 100;
        int threadCount = 4;

        Data res = new Data(n);
        Compute a = new Compute(0, 100, 100, true);
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        CountDownLatch latch2 = new CountDownLatch(threadCount);
        CountDownLatch latch3 = new CountDownLatch(threadCount);

        a.read(res);
        res.startTime = System.nanoTime();

        for (int i = 0; i < threadCount; i++) {
            executor.execute(new Thread(() -> thread(res, n, threadCount, latch, latch2, latch3)));
        }

        executor.shutdown();
        latch.await();

        long endTime = System.nanoTime();
        System.out.println("Time: " + (endTime - res.startTime) / 1000000 + " ms");
        System.out.println(res.min);
        a.write(res, "Lab3");
    }

    public static void thread(Data res, int n, int threadCount, CountDownLatch latch, CountDownLatch latch2, CountDownLatch latch3) {
        String name = Thread.currentThread().getName();
        int num = Integer.parseInt(name.substring(name.length() - 1)) - 1;
        Compute c = new Compute(n / threadCount * num,
                ((num != threadCount - 1) ? n / threadCount * (num + 1) : n), n, false);
        System.out.println("Task " + (num + 1) + " start");


        c.sumMatrix(res.MT, res.MZ, res.MA);

        latch2.countDown();
        try {
            latch2.await();
        } catch(InterruptedException e) {

        }
        c.multiplyMatrix(res.MD, res.MA, res.MC);

        c.multiplyMatrix(res.ME, res.MM, res.MB);
        c.diffMatrix(res.MC, res.MB, res.MG);

        c.multiplyArrayMatrix(res.MT, res.D, res.C);
        float min = c.minInArray(res.Z);
        latch3.countDown();
        try {
            latch3.await();
        } catch(InterruptedException e) {

        }
        if (res.min > min) {
            res.min = min;
        }

        latch.countDown();

        c.multiplyFloatArray(res.C, res.min, res.E);
        c.sumArrays(res.B, res.E, res.A);

        System.out.println("Task " + (num + 1) + " end");
    }
}
