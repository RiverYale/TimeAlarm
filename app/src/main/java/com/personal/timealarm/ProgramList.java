package com.personal.timealarm;

import android.app.ActionBar;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProgramList extends AppCompatActivity{

    private ListView view_programList;
    private List<String> onListPackageNames;
    private List<String> packageNames;
    private SharedPreferences data;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_program_list);
        setTitle("添加程序");

        data = getSharedPreferences("data", MODE_PRIVATE);
        onListPackageNames = getOnListPackageNames();
        packageNames = getAppList();

        initList();

        view_programList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Map<String, Object> tempMap = (Map<String, Object>)adapterView.getAdapter().getItem(i);
                String packageName = (String)tempMap.get("id");

                Intent intent = new Intent();
                intent.putExtra("packageName", packageName);
                setResult(1, intent);
                finish();
            }
        });
    }

    protected void onStart() {
        super.onStart();
        ActionBar actionBar = this.getActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     *初始化列表
     */
    private void initList() {
        view_programList = findViewById(R.id.widget_programlist);
        List<Map<String, Object>> appInfo = new ArrayList<>();
        for(int i=0;i<packageNames.size();i++) {
            Map<String, Object> map = getAppInfo(packageNames.get(i));
            if(map.size()>0){
                appInfo.add(map);
            }
        }
        ProgramListAdapter adapter = new ProgramListAdapter(this, appInfo);
        view_programList.setAdapter(adapter);
    }

    /**
     * 获取可备选app包名
     */
    private List<String> getAppList() {
        PackageManager pm = getPackageManager();
        List<PackageInfo> packages = pm.getInstalledPackages(0);
        List<String> temp = new ArrayList<>();
        for(int i=0;i<packages.size();i++) {
            PackageInfo packageInfo = packages.get(i);
            String str = packageInfo.packageName;
            if (!isOnList(str) && (packageInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0){ // 非系统应用
                temp.add(str);
            }
        }
        if(!isOnList("com.android.browser")){
            temp.add("com.android.browser");
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
     * 获取被监控的app包名
     */
    private List<String> getOnListPackageNames() {
        String strs[] = data.getString("packageNames", " ").split(" ");
        return new ArrayList<>(Arrays.asList(strs));
    }

    /**
     * 判断包名是否在被监控列表
     */
    private boolean isOnList(String packageName) {
        for(int i = 0; i< onListPackageNames.size(); i++) {
            if (packageName.equals(onListPackageNames.get(i))) {
                return true;
            }
        }
        return false;
    }
}
