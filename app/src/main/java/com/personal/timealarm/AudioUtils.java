package com.personal.timealarm;


import android.content.Context;
import android.database.Cursor;
import android.provider.MediaStore;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @ClassName: com.example.mediastore.AudioUtils
 * @Description: 音频文件帮助类
 *
 */
public class AudioUtils {
    /**
     * 获取sd卡所有的音乐文件
     *
     * @return
     * @throws Exception
     */
    public static ArrayList<Map<String,String>> getAllSongs(Context context) {

        ArrayList<Map<String,String>> songs = null;

        Cursor cursor = context.getContentResolver().query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                new String[] { MediaStore.Audio.Media._ID,
                        MediaStore.Audio.Media.DISPLAY_NAME,
                        MediaStore.Audio.Media.TITLE,
                        MediaStore.Audio.Media.DURATION,
                        MediaStore.Audio.Media.ARTIST,
                        MediaStore.Audio.Media.ALBUM,
                        MediaStore.Audio.Media.YEAR,
                        MediaStore.Audio.Media.MIME_TYPE,
                        MediaStore.Audio.Media.SIZE,
                        MediaStore.Audio.Media.DATA },
                MediaStore.Audio.Media.MIME_TYPE + "=? or "
                        + MediaStore.Audio.Media.MIME_TYPE + "=?",
                new String[] { "audio/mpeg", "audio/x-ms-wma" }, null);

        songs = new ArrayList<Map<String,String>>();

        if (cursor.moveToFirst()) {
            do {
                Map<String,String> temp_song = new HashMap<>();
                if (cursor.getString(9) == null) {
                    continue;
                    //没文件路径就算了
                }
                //文件路径
                temp_song.put("songFileUrl",cursor.getString(9));
                // 文件名
                temp_song.put("name",cursor.getString(2));
                // 时长
                temp_song.put("length",String.format("%.2f",cursor.getInt(3)/1000.0/60.0)+"min");
                // 歌手名
                temp_song.put("singer",cursor.getString(4));
                // 歌曲格式
//                if ("audio/mpeg".equals(cursor.getString(7).trim())) {
//                    song.setType("mp3");
//                } else if ("audio/x-ms-wma".equals(cursor.getString(7).trim())) {
//                    song.setType("wma");
//                }
                songs.add(temp_song);
            } while (cursor.moveToNext());
            cursor.close();
        }
        return songs;
    }
}
