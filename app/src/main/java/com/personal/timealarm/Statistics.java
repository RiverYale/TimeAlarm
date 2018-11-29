package com.personal.timealarm;

import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Statistics extends AppCompatActivity{

    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_program_list);
        setTitle("近7天使用时间统计");
        initList();
    }

    private void initList() {
        ListView view_programList = findViewById(R.id.widget_programlist);
        List<Map<String, Object>> appInfo = getAppList(this);
        StatisticAdapter adapter = new StatisticAdapter(this, appInfo);
        view_programList.setAdapter(adapter);
    }

    private List<Map<String, Object>> getAppList(Context context) {
        List<Map<String, Object>> appInfo = new ArrayList<>();
        PackageManager pm = getPackageManager();
        if (Build.VERSION_CODES.LOLLIPOP <= Build.VERSION.SDK_INT) {
            UsageStatsManager m = (UsageStatsManager) context.getSystemService(Context.USAGE_STATS_SERVICE);
            if (m != null) {
                long end = System.currentTimeMillis();
                Map<String, MyData> map = new HashMap<>();
                for(int i=0;i<7;i++){
                    Map<String, UsageStats> temp = m.queryAndAggregateUsageStats(end-1000*60*60*24, end);
                    for (Map.Entry<String, UsageStats> entry : temp.entrySet()) {
                        UsageStats us = entry.getValue();
                        String key = entry.getKey();
                        if(map.containsKey(entry.getKey())){
                            Long time = map.get(key).getTotalTimeInForeground();
                            map.put(key, new MyData(key, us.getTotalTimeInForeground()+time));
                        }else if(us.getTotalTimeInForeground() > 0){
                            map.put(key, new MyData(key, us.getTotalTimeInForeground()));
                        }
                    }
                    end -= 1000*60*60*24;
                }
                List<MyData> stats = new ArrayList<>();
                for (Map.Entry<String, MyData> entry : map.entrySet()) {
                    MyData temp = entry.getValue();
                    stats.add(temp);
                }
                if (!stats.isEmpty()) {
                    for (int i = 0; i < stats.size(); i++) {
                        String packageName = stats.get(i).getPackageName();
                        PackageInfo packageInfo;
                        try {
                            packageInfo = pm.getPackageInfo(packageName, PackageManager.GET_META_DATA);
                            if(packageName.equals("com.android.browser") || (packageInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0){
                                Map<String, Object> item = getAppInfo(packageName);
                                item.put("proLongValue", stats.get(i).getTotalTimeInForeground());
                                appInfo.add(item);
                            }
                        } catch (PackageManager.NameNotFoundException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
        return appInfo;
    }

    private Map<String,Object> getAppInfo(String packageName) {
        Map<String, Object> map = new HashMap<>();
        PackageManager pm = getPackageManager();
        try {
            ApplicationInfo appInfo = pm.getApplicationInfo(packageName, PackageManager.GET_META_DATA);
            String appName = (String) pm.getApplicationLabel(appInfo);
            Drawable appIcon = pm.getApplicationIcon(appInfo);
            map.put("icon", appIcon);
            map.put("text", appName);
            return map;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return map;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    static class MyData{
        private String packageName;
        private Long time;

        MyData(String packageName, Long time){
            this.packageName = packageName;
            this.time = time;
        }
        private Long getTotalTimeInForeground() {
            return time;
        }

        private String getPackageName() {
            return packageName;
        }
    }
}
