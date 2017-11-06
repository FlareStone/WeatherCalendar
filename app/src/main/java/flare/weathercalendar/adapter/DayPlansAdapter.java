package flare.weathercalendar.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import flare.weathercalendar.R;
import flare.weathercalendar.activity.AddPlanActivity;
import flare.weathercalendar.entity.Plan;

/**
 * Created by 54333 on 2017/7/27.
 */

public class DayPlansAdapter extends BaseAdapter {
    private List<Plan> data;
    private LayoutInflater inflater;

    public DayPlansAdapter(Context context, List<Plan> data) {
        this.data = data;
        inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public Plan getItem(int position) {
        return data.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public void deleteItem(int position) {
        data.remove(position);
    }

    public void deleteAllItem() {
        data.clear();
    }

    public void addItem(Plan plan) {
        data.add(plan);
    }

    public void refresh(List<Plan> list) {
        data.clear();
        data.addAll(list);
    }

    static class ViewHolder {
        TextView time;
        TextView title;
        ImageView remind;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = inflater.inflate(R.layout.day_plan, null);
            holder.time = (TextView) convertView.findViewById(R.id.time);
            holder.title = (TextView) convertView.findViewById(R.id.title);
            holder.remind = (ImageView) convertView.findViewById(R.id.remind);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        Plan plan = data.get(position);
        holder.time.setText(AddPlanActivity.getTime(plan.getHour(), plan.getMinute()));
        holder.title.setText(plan.getTitle());
        if (plan.isNeedRemind()) {
            holder.remind.setVisibility(View.VISIBLE);
        } else {
            holder.remind.setVisibility(View.GONE);
        }
        return convertView;
    }
}
