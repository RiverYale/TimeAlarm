package com.personal.timealarm;

import android.app.KeyguardManager;
import android.app.Service;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.Vibrator;
import android.util.Log;

import java.util.List;

public class MonitorService extends Service {

    private SharedPreferences data;
    private boolean isMonitor = false;
    private boolean isOnAlarm;
    private long curLastTime = 0;
    private long curStopTime = 0;
    private int type;
    private boolean isMonitorStart = false;
    private String lastApp = "";

    private String[] items;

    Vibrator vibrator;

    public IBinder onBind(Intent intent) {
        return new MyMsg();
    }

    public void onCreate() {
        super.onCreate();
        data = getSharedPreferences("data", MODE_PRIVATE);
        items = data.getString("packageNames", " ").split(" ");

        type = data.getInt("type", 0);
        vibrator = (Vibrator)getSystemService(VIBRATOR_SERVICE);
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        if(!isMonitor){
            isMonitor = true;
            Monitor monitor = new Monitor();
            monitor.start();
        }
        return START_STICKY;
    }

    public void onDestroy() {
        super.onDestroy();
        isMonitor = false;
        vibrator.cancel();
    }

    /**
     * 获取顶端app包名
     */
    private String getTopApp(Context context) {
        if (Build.VERSION_CODES.LOLLIPOP <= Build.VERSION.SDK_INT) {
            UsageStatsManager m = (UsageStatsManager) context.getSystemService(Context.USAGE_STATS_SERVICE);
            if (m != null) {
                long now = System.currentTimeMillis();
                //获取?秒之内的应用数据
                List<UsageStats> stats = m.queryUsageStats(UsageStatsManager.INTERVAL_BEST, now - 60000, now);
                String topActivity = "";
                //取得最近运行的一个app，即当前运行的app
                if ((stats != null) && (!stats.isEmpty())) {
                    int j = 0;
                    for (int i = 0; i < stats.size(); i++) {
                        if (stats.get(i).getLastTimeUsed() > stats.get(j).getLastTimeUsed()) {
                            j = i;
                        }
                    }
                    topActivity = stats.get(j).getPackageName();
                }
                return topActivity;
            }
        }
        return "";
    }

    /**
     * 判断是否是被监控的app
     */
    public boolean isOnList(String item) {
        for (String item1 : items) {
            if (item.equals(item1)) {
                return true;
            }
        }
        return false;
    }

    protected long getCurLastTime() {
        return curLastTime;
    }

    public long getCurStopTime() {
        return curStopTime;
    }

    public class MyMsg extends Binder{
        public long getCurLastTime() {
            return MonitorService.this.getCurLastTime();
        }

        public long getCurStopTime() {
            return MonitorService.this.getCurStopTime();
        }
    }

    class Monitor extends Thread{
        public void run() {
            long last = System.currentTimeMillis();
            while (isMonitor) {
                try {
                    boolean needCount = isNeedCount();
                    long current = System.currentTimeMillis();
                    if (needCount){
                        curLastTime += current - last;
                        curStopTime = 0;
                    }else if(curLastTime > 0){
                        curStopTime += current - last;
                        if (curStopTime >= data.getLong("stopTime", 5L) * 60000) {
                            curLastTime = 0;
                        }
                    }
                    if(curLastTime >= /*data.getLong("lastTime",30L) * 60000*/ 5000 && needCount){
                        if(!isOnAlarm){
                            startAlarm();
                            isOnAlarm = true;
                        }
                    }else{
                        stopAlarm();
                        isOnAlarm = false;
                    }
                    last = current;

                    Thread.sleep(500);
                    Log.d("zc", lastApp+" "+curLastTime+" "+curStopTime+" "+getId());
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        /**
         * 是否需要计玩耍时间
         */
        private boolean isNeedCount(){
            String topApp;
            boolean flag, isLocked = false, isOpen = false;

            PowerManager powerManager = (PowerManager)getSystemService(Context.POWER_SERVICE);
            if (powerManager != null) isOpen = powerManager.isScreenOn();

            KeyguardManager mKeyguardManager = (KeyguardManager)getSystemService(Context.KEYGUARD_SERVICE);
            if (mKeyguardManager != null) isLocked = mKeyguardManager.inKeyguardRestrictedInputMode();

            topApp = getTopApp(MonitorService.this);
            if("".equals(topApp)) topApp = lastApp;
            else lastApp = topApp;

            flag = isOnList(topApp);

            return flag && isOpen && !isLocked;
        }

        private void startAlarm(){
            switch (type) {
                case 0:
                    long[] patter = {0, 3000, 1000};
                    vibrator.vibrate(patter, 0);
                    break;
                case 1:
                    //TODO
                    break;
                case 2:
                    Intent intent = new Intent(getApplicationContext(), AlarmActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    getApplication().startActivity(intent);
                    break;
            }
        }

        private void stopAlarm() {
            switch (type) {
                case 0:
                    vibrator.cancel();
                    break;
                case 1:
                    //TODO
                    break;
                case 2:

                    break;
            }
        }
    }
}
