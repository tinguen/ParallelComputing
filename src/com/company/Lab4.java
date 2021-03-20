package com.company;

import java.io.IOException;
import java.util.concurrent.*;
import java.util.concurrent.locks.ReentrantLock;

// MG = MD*(MT+MZ)-ME*MM
// A = min(Z)*D*MT+B
public class Lab4 {
    public static void main(String[] args) throws IOException, InterruptedException {
        int n = 100;
        int threadCount = 4;

        Data res = new Data(n);
        Compute a = new Compute(0, 100, 100, true);
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CyclicBarrier cb = new CyclicBarrier(threadCount);
        ReentrantLock locker = new ReentrantLock();
        MyThread callable = new MyThread(res, n, threadCount, locker, cb);

        a.read(res);
        res.startTime = System.nanoTime();

        for (int i = 0; i < threadCount; i++) {
            executor.submit(callable);
        }

        executor.shutdown();
        if (executor.awaitTermination(60, TimeUnit.SECONDS)) {
            long endTime = System.nanoTime();
            System.out.println("Time: " + (endTime - res.startTime) / 1000000 + " ms");
            a.write(res, "Lab4");
        }
    }

    static class MyThread implements Callable<String> {

        private final Data res;
        int n;
        int threadCount;
        private final ReentrantLock locker;
        CyclicBarrier cb;

        MyThread(Data res, int n, int threadCount, ReentrantLock locker, CyclicBarrier cb) {
            this.res = res;
            this.n = n;
            this.threadCount = threadCount;
            this.locker = locker;
            this.cb = cb;
        }

        @Override
        public String call() {
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

            locker.lock();
            if (res.min > min) {
                res.min = min;
            }
            locker.unlock();

            c.multiplyFloatArray(res.C, res.min, res.E);
            c.sumArrays(res.B, res.E, res.A);
            System.out.println("Task " + (num + 1) + " end");
            return null;
        }
    }
}
