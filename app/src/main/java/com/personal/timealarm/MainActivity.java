package com.personal.timealarm;

import android.app.AlertDialog;
import android.app.AppOpsManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private Switch view_onAlarm;
    private Switch view_onSleepAlarm;
    private ListView view_program;

    private ProgramListAdapter adapter;
    private List<Map<String, Object>> onListAppInfo;
    private SharedPreferences data;
    private SharedPreferences.Editor editor;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        view_onAlarm = findViewById(R.id.widget_onAlarm);
        view_program = findViewById(R.id.widget_program);

        data = getSharedPreferences("data", MODE_PRIVATE);
        editor = data.edit();

        onListAppInfo = getOnListAppInfo();
        adapter = new ProgramListAdapter(this, onListAppInfo);
        view_program.setAdapter(adapter);

        final Intent service = new Intent(MainActivity.this, MonitorService.class);
        final Intent sleepMonitorService = new Intent(MainActivity.this, SleepMonitorService.class);

        boolean temp_isOnAlarm = data.getBoolean("isOnAlarm", false);
        view_onAlarm.setChecked(temp_isOnAlarm);
        if(temp_isOnAlarm){
            startService(service);
        }

        view_onAlarm.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(b){
                    editor.putBoolean("isOnAlarm", true);
                    startService(service);
                }else{
                    editor.putBoolean("isOnAlarm", false);
                    stopService(service);
                }
                editor.apply();
            }
        });


        /*
        接下来的几行是睡觉检测的初始化和listener
        * */
        view_onSleepAlarm = findViewById(R.id.widget_onSleepAlarm);
        Boolean isOnSleepAlarm  = data.getBoolean("isOnSleepAlarm",false);
        view_onSleepAlarm.setChecked(isOnSleepAlarm);
        if(isOnSleepAlarm)
        {
            startService(sleepMonitorService);
        }
        view_onSleepAlarm.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(b) {
                    String sleepTime = data.getString("sleepTime",null);
                    if(sleepTime==null) {
                        Toast.makeText(MainActivity.this, "还没有设置过睡觉时间，默认为23:00", Toast.LENGTH_SHORT).show();
                    }
                    editor.putBoolean("isOnSleepAlarm",true);
                    startService(sleepMonitorService);
//                    Log.i("start","开启睡觉提醒");
                } else {
                    editor.putBoolean("isOnSleepAlarm",false);
                    stopService(sleepMonitorService);
//                    Log.i("stop","关闭睡觉提醒");
                }
                editor.apply();
            }
        });

        view_program.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                Map<String, Object> tempMap = (Map<String, Object>)adapterView.getAdapter().getItem(i);
                if (data.getBoolean("isOnAlarm", false)) {
                    Toast.makeText(MainActivity.this, "请先关闭提醒", Toast.LENGTH_SHORT).show();
                    return true;
                }
                String packageName = (String)tempMap.get("id");
                for(int j = 0; j < onListAppInfo.size(); j++) {
                    if (onListAppInfo.get(j).get("id").equals(packageName)) {
                        onListAppInfo.remove(j);
                    }
                }
                putItems();
                adapter = new ProgramListAdapter(MainActivity.this, onListAppInfo);
                view_program.setAdapter(adapter);
                return true;
            }
        });
        if(data.getBoolean("first_open",true)){
            Log.i("showHowToUse","进入说明页面");
            editor.putBoolean("first_open",false);
            editor.apply();
            Intent intent = new Intent(MainActivity.this,HowToUse.class);
            startActivity(intent);
        }

    }

    protected void onResume() {
        super.onResume();
        askForPermissions();
    }

    private void askForPermissions() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return;
        }
        AppOpsManager appOps = (AppOpsManager) getSystemService(this.APP_OPS_SERVICE);
        int mode = 0;
        mode = appOps.checkOpNoThrow("android:get_usage_stats", android.os.Process.myUid(), getPackageName());
        boolean granted = mode == AppOpsManager.MODE_ALLOWED;
        if (!granted) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("权限申请");
            builder.setMessage("请在权限隐私中开启“应用使用情况查看”相关权限！");
            builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.dismiss();
                    Intent intent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
                    startActivity(intent);
                }
            });
            builder.setCancelable(false);
            builder.show();
        }
    }

    /**
     * 获取被监控的app信息
     */
    private List<Map<String,Object>> getOnListAppInfo() {
        List<Map<String,Object>> temp = new ArrayList<>();
        String strs[] = data.getString("packageNames", " ").split(" ");
        for(int i=0;i<strs.length;i++) {
            Map<String, Object> map = getAppInfo(strs[i]);
            if(map.size()>0){
                temp.add(map);
            }
        }
        return temp;
    }

    /**
     * 根据app包名获取图标和名字
     */
    private Map<String,Object> getAppInfo(String packageName) {
        Map<String, Object> map = new HashMap<>();
        PackageManager pm = getPackageManager();
        try {
            ApplicationInfo appInfo = pm.getApplicationInfo(packageName, PackageManager.GET_META_DATA);
            String appName = (String) pm.getApplicationLabel(appInfo);
            Drawable appIcon = pm.getApplicationIcon(appInfo);
            map.put("icon", appIcon);
            map.put("text", appName);
            map.put("id", packageName);
            return map;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return map;
    }

    /**
     * 更新data中被监控的app的包名
     */
    private void putItems() {
        String str="";
        for(int i = 0; i< onListAppInfo.size(); i++) {
            str += onListAppInfo.get(i).get("id")+" ";
        }
        editor.putString("packageNames", str);
        editor.apply();
    }

    public void onClick(View view) {
        Intent intent;
        switch (view.getId()) {
            case R.id.widget_add:
                if (data.getBoolean("isOnAlarm", false)) {
                    Toast.makeText(MainActivity.this, "请先关闭提醒", Toast.LENGTH_SHORT).show();
                    return;
                }
                intent = new Intent(MainActivity.this, ProgramList.class);
                startActivityForResult(intent,0);
                break;
            case R.id.widget_setting:
                intent = new Intent(MainActivity.this, MySettings.class);
                startActivity(intent);
                break;
            case R.id.widget_statistics:
                intent = new Intent(MainActivity.this, Statistics.class);
                startActivity(intent);
                break;
        }
    }

    protected void onActivityResult(int requestCode, int resultCode,Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data != null && requestCode == 0) {                                         //请求码和结果码可标识来源
            Bundle bundle = data.getExtras();
            String packageName = bundle.getString("packageName");
            if (resultCode==1){
                onListAppInfo.add(getAppInfo(packageName));
                adapter = new ProgramListAdapter(this, onListAppInfo);
                view_program.setAdapter(adapter);
                putItems();
            }
        }
    }
}