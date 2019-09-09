# GradlePlugins
![plugin-aspectj](https://img.shields.io/badge/plugin--aspectj-2.0.0-brightgreen.svg) [![996.icu](https://img.shields.io/badge/link-996.icu-red.svg)](https://996.icu)  [![LICENSE](https://img.shields.io/badge/license-Anti%20996-blue.svg)](https://github.com/996icu/996.ICU/blob/master/LICENSE) 

> `GradlePlugins`是一些`gradle`插件

----

## `plugin-aspectj`插件

> 为了在`Android`上实现`AOP`功能，需要用到`aspectj`和`gradle`自定义插件

* 1、添加依赖

  > 在项目的`build.gradle`中如下配置

  ```groovy
  dependencies {
      classpath 'com.yhy.plugins:plugin-aspectj:latestVersion'
  }
  ```

* 2、应用插件

  > 在需要`AOP`操作模块的`build.gradle`中如下配置

  ```groovy
  apply plugin: 'com.android.application'
  apply plugin: 'plugin-aspectj'
  ```

* 实现`AOP`功能

  > 定义`Aspectj`处理器，实现`AOP`功能

  ```java
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
  ```

  