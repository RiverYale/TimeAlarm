package com.personal.timealarm;

import android.app.KeyguardManager;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.Vibrator;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import java.util.Calendar;

public class SleepMonitorService extends Service {

    private SharedPreferences data;
    private Calendar cal;
    private boolean isOnSleepMonitor;
    private String last_date;
    Vibrator vibrator;

    public IBinder onBind(Intent intent) {
        return null;
    }

    public void onCreate() {
        super.onCreate();
        isOnSleepMonitor = true;
        data = getSharedPreferences("data", MODE_PRIVATE);
        vibrator = (Vibrator)getSystemService(VIBRATOR_SERVICE);
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        cal = Calendar.getInstance();
        last_date = cal.get(Calendar.MONTH)+" "+(cal.get(Calendar.DAY_OF_MONTH)-1);
        sleepMonitor sleepmonitor = new sleepMonitor();
        sleepmonitor.start();
        return START_STICKY;
    }

    public void onDestroy() {
        super.onDestroy();
        isOnSleepMonitor = false;
    }


    class sleepMonitor extends Thread{
        public void run() {
            Log.i("running","开启睡觉提醒");
            String today;
            int n_hour;
            int n_minute;
            boolean isOpen = false;
            boolean isLocked;
            String sleepTime = data.getString("sleepTime","23:00");
            int hour = Integer.valueOf(sleepTime.substring(0,sleepTime.indexOf(':')));
            int minute = Integer.valueOf(sleepTime.substring(sleepTime.indexOf(':')+1));
            Log.i("sleepTime","睡觉时间为："+hour+":"+minute+",也就是 "+data.getString("sleepTime","23:00"));
            while(isOnSleepMonitor){
                try {
                    cal = Calendar.getInstance();
                    n_hour = cal.get(Calendar.HOUR);
                    n_minute = cal.get(Calendar.MINUTE);
                    today = cal.get(Calendar.MONTH)+" "+cal.get(Calendar.DAY_OF_MONTH);


                    int n_time = n_hour*60+n_minute;
                    int time = hour*60+minute;
                    Log.i("onMonitor","监控中，距离响铃还剩"+(time-n_time)+"min");
                    if(!last_date.equals(today))
                    {
                        //如果今天还没有提醒过
//                        int n_time = n_hour*60+n_minute;
//                        int time = hour*60+minute;
                        if(n_time-time>=0&&n_time-time<=10)
                        {
                            //如果时间到了并且在10分钟之内
                            PowerManager powerManager = (PowerManager)getSystemService(Context.POWER_SERVICE);
                            if (powerManager != null) {
                                isOpen = powerManager.isScreenOn();
                            }
                            KeyguardManager mKeyguardManager = (KeyguardManager)getSystemService(Context.KEYGUARD_SERVICE);
                            isLocked = mKeyguardManager.inKeyguardRestrictedInputMode();
                            if(isOpen&&!isLocked)
                            {

                                //展示有震动效果的通知
                                NotificationManager mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                                long[] vibrate = new long[]{0, 500, 1000, 1500};
                                NotificationCompat.Builder builder = new NotificationCompat.Builder(SleepMonitorService.this)
                                        .setSmallIcon(R.mipmap.ic_launcher)
                                        .setContentTitle("睡觉时间到啦")
                                        .setContentText("不要再玩手机啦，到睡觉时间啦")
                                        .setVibrate(vibrate);
                                mNotificationManager.notify(1, builder.build());
                                Log.i("Notification","通知成功");

                                //之后将最后一次提醒过的时间设置为今天
                                last_date = today;
                            }

                        }
                    }
                    Thread.sleep(5000);
                }catch(Exception e){
                    e.printStackTrace();
                }
            }
        }
    }
}
