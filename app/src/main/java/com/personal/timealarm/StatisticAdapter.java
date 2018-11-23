package com.personal.timealarm;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.List;
import java.util.Map;

public class StatisticAdapter extends BaseAdapter{
    private List<Map<String,Object>> items;
    private Context context;

    public StatisticAdapter(Context c, List l) {
        items = l;
        context = c;
        for(int i=0;i<items.size()-1;i++) {
            for(int j=i+1;j<items.size();j++){
                if((long)items.get(i).get("proLongValue") < (long)items.get(j).get("proLongValue")){
                    Map<String, Object> temp = items.get(i);
                    items.set(i, items.get(j));
                    items.set(j, temp);
                }
            }
        }
        if(!items.isEmpty()) {
            long max = (long) items.get(0).get("proLongValue");
            for (int i = 0; i < items.size(); i++) {
                long pro = (long) items.get(i).get("proLongValue");
                int prog = (int) (pro * 100 / max);
                items.get(i).put("progress", prog);
                items.get(i).put("proValue", getProValue(pro));
            }
        }
    }

    private String getProValue(long pro){
        long h = pro/1000/60/60;
        long m = pro/1000/60 - h*60;
        long s = pro/1000 - h*60*60 - m*60;
        return h+"时"+m+"分"+s+"秒";
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
            v = View.inflate(context, R.layout.activity_statistics_item, null);
            holder = new Holder();
            holder.view_icon = v.findViewById(R.id.widget_icon);
            holder.view_text = v.findViewById(R.id.widget_text);
            holder.view_proValue = v.findViewById(R.id.widget_proValue);
            holder.view_progressBar = v.findViewById(R.id.widget_progressBar);
            v.setTag(holder);
        }
        holder.view_icon.setImageDrawable((Drawable)items.get(i).get("icon"));
        holder.view_text.setText((String)items.get(i).get("text"));
        holder.view_proValue.setText((String)items.get(i).get("proValue"));
        holder.view_progressBar.setProgress((Integer) items.get(i).get("progress"));
        return v;
    }

    public List<Map<String, Object>> getItems() {
        return items;
    }

    private static class Holder{    //static方便内存垃圾回收，此时内部类不持有外部对象引用
        private ImageView view_icon;
        private TextView view_text;
        private TextView view_proValue;
        private ProgressBar view_progressBar;
    }
}
