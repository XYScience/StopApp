package com.science.stopapp.util;

import android.os.Handler;
import android.os.Message;

import java.lang.ref.WeakReference;

/**
 * @author SScience
 * @description 弱引用
 * @email chentushen.science@gmail.com,274240671@qq.com
 * @data 2017/1/15
 */

public class WeakHandler extends Handler {

    public interface IHandler {
        void handleMessage(Message msg);
    }

    private WeakReference<IHandler> wf;

    public WeakHandler(IHandler handler) {
        this.wf = new WeakReference<>(handler);
    }

    @Override
    public void handleMessage(Message msg) {
        super.handleMessage(msg);
        IHandler handler = wf.get();
        if (handler != null) {
            handler.handleMessage(msg);
        }
    }
}
