package com.personal.timealarm;

import android.app.KeyguardManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.Vibrator;
import android.support.v4.app.NotificationCompat;
import java.util.Calendar;

public class SleepMonitorService extends Service {

    private SharedPreferences data;
    private Calendar cal;
    private boolean isOnSleepMonitor;
    Vibrator vibrator;

    public IBinder onBind(Intent intent) {
        return null;
    }

    public void onCreate() {
        super.onCreate();
        isOnSleepMonitor = false;
        data = getSharedPreferences("data", MODE_PRIVATE);
        vibrator = (Vibrator)getSystemService(VIBRATOR_SERVICE);
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        if(!isOnSleepMonitor){
            isOnSleepMonitor = true;
            SleepMonitor sleepmonitor = new SleepMonitor();
            sleepmonitor.start();
        }
        return START_STICKY;
    }

    public void onDestroy() {
        super.onDestroy();
        isOnSleepMonitor = false;
    }

    class SleepMonitor extends Thread{
        public void run(){
            String num[] = data.getString("sleepTime", "23:00").split(":");
            long plan = Long.parseLong(num[0])*1000*60*60 + Long.parseLong(num[1])*1000*60;
            long now;
            while(isOnSleepMonitor) {
                cal = Calendar.getInstance();
                int hour = cal.get(Calendar.HOUR_OF_DAY);
                int minute = cal.get(Calendar.MINUTE);
                now = hour*1000*60*60 + minute*1000*60;
                if (isDuringTime(plan, now)) {
                    boolean isOpen = false;
                    boolean isLocked = false;
                    PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
                    if (powerManager != null) isOpen = powerManager.isScreenOn();
                    KeyguardManager mKeyguardManager = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
                    if (mKeyguardManager != null)
                        isLocked = mKeyguardManager.inKeyguardRestrictedInputMode();
                    if (isOpen && !isLocked) {
                        showNotification();
                    }
                    try {
                        Thread.sleep(1000*60);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        private boolean isDuringTime(long plan, long now){
            if(now>=plan)
                return (now - plan) <= 1000 * 60 * 10;
            else
                return (now + 1000 * 60 * 60 * 24 - plan) <= 1000 * 60 * 10;
        }

        private void showNotification(){
            NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            NotificationCompat.Builder builder = new NotificationCompat.Builder(SleepMonitorService.this, "default")
                    .setContentTitle("睡觉时间到啦")
                    .setSmallIcon(R.drawable.small_icon)
                    .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.new_clock))
                    .setContentText("到点后十分钟内每隔一分钟就检查你一次~")
                    .setAutoCancel(true);
            PendingIntent pIntent = PendingIntent.getActivity(SleepMonitorService.this, 100, new Intent(), PendingIntent.FLAG_UPDATE_CURRENT);
            builder.setContentIntent(pIntent);
            notificationManager.notify(100, builder.build());

            switch(data.getInt("type", 0)){
                case 0:
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        builder.setFullScreenIntent(pIntent, true);
                        builder.setCategory(NotificationCompat.CATEGORY_MESSAGE);
                        builder.setVisibility(NotificationCompat.VISIBILITY_PUBLIC);
                    }
                    long[] patter = {0, 500, 500, 500};
                    vibrator.vibrate(patter, -1);
                    notificationManager.notify(100, builder.build());
                    break;
                case 1:
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        builder.setFullScreenIntent(pIntent, true);
                        builder.setCategory(NotificationCompat.CATEGORY_MESSAGE);
                        builder.setVisibility(NotificationCompat.VISIBILITY_PUBLIC);
                    }
                    Uri uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                    MediaPlayer mediaPlayer = MediaPlayer.create(getApplicationContext(), uri);
                    mediaPlayer.start();
                    break;
                case 2:
                    Intent intent = new Intent(getApplicationContext(), AlarmActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.putExtra("content", "睡觉时间到了哦~");
                    getApplication().startActivity(intent);
                    break;
            }
        }
    }
}
