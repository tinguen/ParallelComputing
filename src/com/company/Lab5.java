package com.company;

import java.io.IOException;
import java.util.concurrent.*;
import java.util.concurrent.Callable;

// MG = MD*(MT+MZ)-ME*MM
// A = min(Z)*D*MT+B
class Lab5 {
    public static void main(String[] args) throws InterruptedException, IOException {
        int n = 100;
        int threadCount = 4;

        Data res = new Data(n);
        Compute a = new Compute(0, 100, 100, true);
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CyclicBarrier cb = new CyclicBarrier(threadCount);
        BlockingQueue<Float> queue = new ArrayBlockingQueue<>(threadCount);
        queue.put((float) Float.MAX_VALUE);
        MyThread callable = new MyThread(res, n, threadCount, queue, cb);

        a.read(res);
        res.startTime = System.nanoTime();

        for (int i = 0; i < threadCount; i++) {
            executor.submit(callable);
        }

        executor.shutdown();
        if (executor.awaitTermination(60, TimeUnit.SECONDS)) {
            long endTime = System.nanoTime();
            System.out.println("Time: " + (endTime - res.startTime) / 1000000 + " ms");

            a.write(res, "Lab5");
        }
    }
}

class MyThread implements Callable<String> {

    private final Data res;
    int n;
    int threadCount;
    private final BlockingQueue<Float> queue;
    CyclicBarrier cb;

    MyThread(Data res, int n, int threadCount, BlockingQueue<Float> queue, CyclicBarrier cb) {
        this.res = res;
        this.n = n;
        this.threadCount = threadCount;
        this.queue = queue;
        this.cb = cb;
    }

    @Override
    public String call() throws InterruptedException {
        String name = Thread.currentThread().getName();
        int num = Integer.parseInt(name.substring(name.length() - 1)) - 1;
        Compute c = new Compute(n / threadCount * num,
                ((num != threadCount - 1) ? n / threadCount * (num + 1) : n), n, false);

        System.out.println("Task " + (num + 1) + " start");

        c.sumMatrix(res.MT, res.MZ, res.MA);

        try { cb.await();
        } catch (BrokenBarrierException | InterruptedException e) {
            e.printStackTrace();
        }
        c.multiplyMatrix(res.MD, res.MA, res.MC);

        c.multiplyMatrix(res.ME, res.MM, res.MB);
        c.diffMatrix(res.MC, res.MB, res.MG);

        c.multiplyArrayMatrix(res.MT, res.D, res.C);
        float min = c.minInArray(res.Z);

        min = Math.min(queue.take(), min);
        queue.put(min);
        res.min = min;

        c.multiplyFloatArray(res.C, res.min, res.E);
        c.sumArrays(res.B, res.E, res.A);
        System.out.println("Task " + (num + 1) + " end");
        return null;
    }
}

