package flare.weathercalendar.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;
import java.util.Map;

import flare.weathercalendar.R;
import flare.weathercalendar.activity.SearchCityActivity;
import flare.weathercalendar.entity.City;

/**
 * Created by 54333 on 2017/7/20.
 */

public class CitiesAdapter extends BaseAdapter {
    private List<City> data;
    private LayoutInflater inflater;

    public List<City> getData() {
        return data;
    }

    public CitiesAdapter(Context context, List<City> data) {
        this.data = data;
        inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public Object getItem(int position) {
        return data.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    static class ViewHolder {
        TextView city;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = inflater.inflate(R.layout.city, null);
            holder.city = (TextView) convertView.findViewById(R.id.city);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        City city = data.get(position);
        if (city.getPath().equals(SearchCityActivity.WAIT_FOR_SEARCH_ANSWER) ||
                city.getPath().equals(SearchCityActivity.AUTOMATIC_LOCATION) ||
                city.getPath().equals(SearchCityActivity.SEARCH_NO_RESULT)) {
            holder.city.setText(city.getPath());
        } else {
            String[] path = city.getPath().split(",", 2);
            holder.city.setText(path[0] + " - " + path[1]);
        }
        return convertView;
    }
}
