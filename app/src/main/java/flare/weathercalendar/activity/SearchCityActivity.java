package flare.weathercalendar.activity;


import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SearchView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import flare.weathercalendar.asyncTask.CityAsyncTask;
import flare.weathercalendar.MyApplication;
import flare.weathercalendar.R;
import flare.weathercalendar.adapter.CitiesAdapter;
import flare.weathercalendar.entity.City;

/**
 * Created by 54333 on 2017/7/20.
 */

public class SearchCityActivity extends Activity {
    private CitiesAdapter citiesAdapter;
    private ListView listView;
    public static final String WAIT_FOR_SEARCH_ANSWER = "正在搜索城市……";
    public static final String AUTOMATIC_LOCATION = "自动定位";
    public static final String SEARCH_NO_RESULT = "未找到结果";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {   //状态栏字体颜色为黑色
            getWindow().getDecorView().setSystemUiVisibility( View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN|View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }



        setContentView(R.layout.activity_search_city);


        SearchView searchView = (SearchView) findViewById(R.id.search_view);
        searchView.setIconifiedByDefault(false);
        searchView.setOnQueryTextListener(new MyOnQueryTextListener());


        List<City> listItems = new ArrayList<City>();
        City listItem = new City();
        listItem.setPath(AUTOMATIC_LOCATION);
        listItem.setId("GPS");
        listItems.add(listItem);

        citiesAdapter = new CitiesAdapter(this, listItems);
        listView = (ListView) findViewById(R.id.city_list);
        listView.setAdapter(citiesAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (!citiesAdapter.getData().get(position).getPath().
                        equals(WAIT_FOR_SEARCH_ANSWER) && !citiesAdapter.getData().
                        get(position).getPath().equals(SEARCH_NO_RESULT)) {
                    Intent intent = getIntent();
                    intent.putExtra("cityId", citiesAdapter.getData().get(position).getId());
                    setResult(1, intent);
                    finish();
                }

            }
        });
    }

    class MyOnQueryTextListener implements SearchView.OnQueryTextListener {

        @Override
        public boolean onQueryTextSubmit(String query) {
            return false;
        }

        @Override
        public boolean onQueryTextChange(String newText) {
            if (!MyApplication.isNetworkAvailable(SearchCityActivity.this)) {
                MyApplication.toastForNoNetwork(SearchCityActivity.this);
                return false;
            }
            if (newText.equals("")) {
                List<City> listItems = citiesAdapter.getData();
                listItems.clear();
                City listItem = new City();
                listItem.setPath(AUTOMATIC_LOCATION);
                listItem.setId("GPS");
                listItems.add(listItem);
                citiesAdapter.notifyDataSetChanged();

                MyApplication application = (MyApplication) getApplication();
                application.setNowSearch(newText);
            } else {
                MyApplication application = (MyApplication) getApplication();
                application.setNowSearch(newText);
                List<City> listItems = citiesAdapter.getData();
                listItems.clear();
                City listItem = new City();
                listItem.setPath(WAIT_FOR_SEARCH_ANSWER);
                listItems.add(listItem);
                citiesAdapter.notifyDataSetChanged();
                CityAsyncTask cityAsyncTask = new CityAsyncTask(citiesAdapter, application);
                cityAsyncTask.execute(newText);
            }
            return false;
        }
    }
}
