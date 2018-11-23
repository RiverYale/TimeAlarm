package com.personal.timealarm;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;
import java.util.Map;

public class ProgramListAdapter extends BaseAdapter{
    private List<Map<String,Object>> items;
    private Context context;

    public ProgramListAdapter(Context c, List l) {
        items=l;
        context=c;
    }

    //获取item个数
    public int getCount() {
        return items.size();
    }

    //获取item
    public Object getItem(int i) {
        return items.get(i);
    }

    //获取item的ID
    public long getItemId(int i) {
        return i;
    }

    //获取item的视图
    public View getView(int i, View view, ViewGroup viewGroup) {
        View v;
        Holder holder;
        if (view != null) {     //优化
            v = view;
            holder = (Holder) v.getTag();
        }else{
            v = View.inflate(context, R.layout.activity_program_item, null);
            holder = new Holder();
            holder.iv = v.findViewById(R.id.widget_icon);
            holder.tv = v.findViewById(R.id.widget_text);
            v.setTag(holder);
        }
        holder.iv.setImageDrawable((Drawable)items.get(i).get("icon"));
        holder.tv.setText((String)items.get(i).get("text"));
        return v;
    }

    public List<Map<String, Object>> getItems() {
        return items;
    }

    private static class Holder{    //static方便内存垃圾回收，此时内部类不持有外部对象引用
        private ImageView iv;
        private TextView tv;
    }
}
