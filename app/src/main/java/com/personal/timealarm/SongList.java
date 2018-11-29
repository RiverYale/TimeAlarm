package com.personal.timealarm;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class SongList extends AppCompatActivity {


    private SharedPreferences data;
    AudioUtils audioUtils;
    ArrayList<Map<String,String>> songList;
    ListView listView;
    private SharedPreferences.Editor editor;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_program_list);
        setTitle("选择音乐");
        songList = audioUtils.getAllSongs(this);

        Map<String,String> random  = new HashMap<>();
        random.put("name","随机播放系统提示音");
        random.put("singer","系统");
        random.put("length","任意");
        random.put("songFileUrl",null);
//        Log.i("song","load success");
        songList.add(0,random);

        listView = findViewById(R.id.widget_programlist);

        String[] from = {"name","singer","length"};
        int[] to = {R.id.widget_song_name,R.id.widget_singer,R.id.widget_song_length};
        SimpleAdapter adapter = new SimpleAdapter(this,songList,R.layout.activity_music_list_item,from,to);
        listView.setAdapter(adapter);

        data = getSharedPreferences("data", MODE_PRIVATE);
        editor = data.edit();
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if(i!=0)
                {
                    editor.putString("songName", songList.get(i).get("name"));
                    editor.putString("songFileUrl", songList.get(i).get("songFileUrl"));
//                    Log.i("song", "data get " + songList.get(i).get("name") + " with URL: " + songList.get(i).get("songFileUrl"));
                    editor.apply();
                }
                else
                {
                    editor.putString("songName",null);
                    editor.putString("songFileUrl", null);
//                    Log.i("song", "data get " + songList.get(i).get("name") + " with URL: " + songList.get(i).get("songFileUrl"));
                    editor.apply();
                }
                setResult(1);
                finish();
            }
        });
    }

    protected void onStart() {
        super.onStart();
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
