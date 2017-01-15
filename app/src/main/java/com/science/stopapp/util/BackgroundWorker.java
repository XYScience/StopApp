package com.science.stopapp.util;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * @author SScience
 * @description 线程池
 * @email chentushen.science@gmail.com,274240671@qq.com
 * @data 2017/1/15
 */

public class BackgroundWorker {
    private ExecutorService mThreadPool;
    private static BackgroundWorker mWorker;

    private BackgroundWorker() {
        mThreadPool = Executors.newCachedThreadPool();
    }

    public static BackgroundWorker getInstance() {
        if (mWorker == null) {
            synchronized (BackgroundWorker.class) {
                if (mWorker == null) {
                    mWorker = new BackgroundWorker();
                }
            }
        }
        return mWorker;
    }

    public Future submitTask(Runnable task) {
        return mThreadPool.submit(task);
    }

    public void executeTask(Runnable task) {
        mThreadPool.execute(task);
    }
}
