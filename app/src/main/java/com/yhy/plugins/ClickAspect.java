package com.yhy.plugins;

import android.util.Log;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;

import java.lang.reflect.Method;

/**
 * author : 颜洪毅
 * e-mail : yhyzgn@gmail.com
 * time   : 2019-09-07 14:55
 * version: 1.0.0
 * desc   :
 */
@Aspect
public class ClickAspect {

    private final static long CLICK_INTERVAL = 1000;

    private long lastClickTime;

    private int lastClickViewId;

    private volatile boolean ignore = false;

    @Before("execution(@com.yhy.plugins.annotation.ClickIgnore * *(..))")
    public void clickIgnore() {
        Log.i("Ignore", "忽略了");
        ignore = true;
    }

    @Before("execution(* android.view.View.OnClickListener.onClick(..))")
    public void clickBefore() {
        Log.i("Ignore", "clickBefore");
        ignore = false;
    }

    @Pointcut("execution(* android.view.View.OnClickListener.onClick(..))")
    public void click() {
    }

    @Pointcut("execution(void *..lambda*(..))")
    public void clickLambda() {
    }

    @Around("click()||clickLambda()")
    public void joinPoint(ProceedingJoinPoint joinPoint) {
        Log.i("AOP", "拦截到了" + ignore);
//        Log.i("AOP", "拦截到了" + joinPoint.getTarget());
        Signature sn = joinPoint.getSignature();
        if (sn instanceof MethodSignature) {
            MethodSignature ms = (MethodSignature) sn;
            Object target = joinPoint.getTarget();
            Method method = null;
            try {
                method = target.getClass().getMethod(ms.getName(), ms.getParameterTypes());
                Log.i("AOP", "拦截到了，方法：" + method.getName());
            } catch (Exception ignored) {
            }
        }
    }
}
