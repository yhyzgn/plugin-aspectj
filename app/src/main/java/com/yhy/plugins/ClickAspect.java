package com.yhy.plugins;

import android.util.Log;

import com.yhy.plugins.annotation.ClickIgnored;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;

import java.lang.reflect.Method;
import java.util.Arrays;

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

    @Around("execution(void *..*Activity+.onBackPressed())")
    public void back() {
        Log.i("Back", "返回了");
    }

    @Pointcut("execution(* android.view.View.OnClickListener.onClick(..))")
    public void click() {
    }

    @Pointcut("execution(void *..lambda*(..))")
    public void clickLambda() {
    }

    @Around("click()||clickLambda()")
    public void joinPoint(ProceedingJoinPoint joinPoint) {
        Log.i("AOP", "拦截到了" + Arrays.toString(joinPoint.getArgs()));
        Signature sn = joinPoint.getSignature();
        Method method = null;
        if (sn instanceof MethodSignature) {
            MethodSignature ms = (MethodSignature) sn;
            Object target = joinPoint.getTarget();
            try {
                method = target.getClass().getMethod(ms.getName(), ms.getParameterTypes());
            } catch (Exception ignored) {
            }
        }
        if (null != method && method.isAnnotationPresent(ClickIgnored.class)) {
            // 如果获取到method，就判断该方法是否有忽略注解
            try {
                joinPoint.proceed(joinPoint.getArgs());
                return;
            } catch (Throwable throwable) {
                throwable.printStackTrace();
            }
        }
        Log.i("AOP", "拦截");
    }
}
