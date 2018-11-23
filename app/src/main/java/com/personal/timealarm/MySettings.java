package com.personal.timealarm;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class MySettings extends AppCompatActivity{

    private TextView view_content;
    private SharedPreferences data;
    private SharedPreferences.Editor editor;
    private long curLastTime = 0, curStopTime = 0;

    ServiceConnection connection=new ServiceConnection() {
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            MonitorService.MyMsg msg = (MonitorService.MyMsg) iBinder;
            curLastTime = msg.getCurLastTime();
            curStopTime = msg.getCurStopTime();
            myHandler.sendMessage(new Message());
        }
        public void onServiceDisconnected(ComponentName componentName) {

        }
    };

    @SuppressLint("HandlerLeak")
    Handler myHandler = new Handler(){
        public void handleMessage(Message msg) {
            updateContent();
        }
    };

    @SuppressLint("CommitPrefEdits")
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        setTitle("设置");

        view_content = findViewById(R.id.widget_content);

        data = getSharedPreferences("data", MODE_PRIVATE);
        editor = data.edit();

        Intent service = new Intent(this, MonitorService.class);
        bindService(service, connection, Context.BIND_AUTO_CREATE);
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

    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.widget_lastTime:
                getAndSetData(1);
                break;
            case R.id.widget_stopTime:
                getAndSetData(2);
                break;
            case R.id.widget_type:
                getAndSetData(3);
                break;
            case R.id.widget_ring:
                if (data.getBoolean("isOnAlarm", false)) {
                    Toast.makeText(MySettings.this, "请先关闭提醒", Toast.LENGTH_SHORT).show();
                } else {

                }
                break;
        }
    }

    public void getAndSetData(final int TYPE) {
        if (data.getBoolean("isOnAlarm", false)) {
            Toast.makeText(MySettings.this, "请先关闭提醒", Toast.LENGTH_SHORT).show();
            return;
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        if (TYPE == 1 || TYPE == 2) {
            final View VIEW_EDIT = View.inflate(this, R.layout.activity_edit, null);
            if(TYPE == 1){
                builder.setTitle("设置"+getString(R.string.lastTime)).setIcon(R.drawable.clock);
            }else{
                builder.setTitle("设置"+getString(R.string.stopTime)).setIcon(R.drawable.clock);
            }
            builder.setView(VIEW_EDIT);
            builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                @SuppressLint("SetTextI18n")
                public void onClick(DialogInterface dialogInterface, int i) {
                    EditText view_editText = VIEW_EDIT.findViewById(R.id.widget_minute);
                    String res = view_editText.getText().toString();
                    if ("".equals(res) || res.charAt(0)=='0') {
                        Toast.makeText(MySettings.this, "请输入正确的数字！", Toast.LENGTH_SHORT).show();
                    }else{
                        if(TYPE == 1) editor.putInt("lastTime",Integer.parseInt(res));
                        else editor.putInt("stopTime",Integer.parseInt(res));
                        editor.apply();
                        updateContent();
                    }
                    dialogInterface.dismiss();
                }
            }).setNegativeButton("取消", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.dismiss();
                }
            }).create().show();
        } else if (TYPE == 3) {
            final String[] arrayOfString = {"震动", "响铃(假装能响铃)"};
            builder.setTitle(getString(R.string.type)).setIcon(R.drawable.clock);
            builder.setSingleChoiceItems(arrayOfString, data.getInt("type",0), new DialogInterface.OnClickListener()    {
                public void onClick(DialogInterface dialogInterface, int i){
                    editor.putInt("type", i);
                    editor.apply();
                    updateContent();
                    dialogInterface.dismiss();
                }
            }).setCancelable(true).create().show();
        }
    }

    @SuppressLint("SetTextI18n")
    public void updateContent() {
        String type;
        if (data.getInt("type", 0) == 0) type = "震动";
        else type = "响铃";
        view_content.setText(getString(R.string.lastTime)+"："+data.getInt("lastTime",30)
                +" min\n"+getString(R.string.stopTime)+"："+data.getInt("stopTime",5)
                +" min\n"+getString(R.string.type)+"："+type
                +"\n"+getString(R.string.ring)+"："+data.getString("ring","Null")
                +"\n\n"+getString(R.string.curLastTime)+": "+String.format("%.2f", curLastTime/60000.0)+" min"
                +"\n"+getString(R.string.curStopTime)+": "+String.format("%.2f", curStopTime/60000.0)+" min");
    }

    protected void onDestroy() {
        super.onDestroy();
        unbindService(connection);
    }
}
