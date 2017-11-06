package flare.weathercalendar.asyncTask;

import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

import flare.weathercalendar.MyApplication;
import flare.weathercalendar.activity.SearchCityActivity;
import flare.weathercalendar.adapter.CitiesAdapter;
import flare.weathercalendar.entity.City;

/**
 * Created by 54333 on 2017/7/18.
 */

public class CityAsyncTask extends AsyncTask<String, Integer, String> {
    private String s;
    private ArrayList<City> cities;
    private CitiesAdapter citiesAdapter;
    private String searchText;
    private MyApplication application;

    public CityAsyncTask(CitiesAdapter citiesAdapter, MyApplication application) {
        this.citiesAdapter = citiesAdapter;
        this.application = application;
    }

    @Override
    protected String doInBackground(String... params) {
        try {
            searchText = params[0];
            URL url = new URL("https://api.seniverse.com/v3/location/search.json?" +
                    "key=pzcqocgaqtwegswj&q=" + searchText);
            HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
            conn.connect();
            int code = conn.getResponseCode();
            if (code == 200) {
                InputStream inputStream = conn.getInputStream();
                s = convertStreamToString(inputStream);
            } else {
                Log.d("Download", "failed code=" + code);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return s;
    }

    @Override
    protected void onPostExecute(String s) {
        cities = new ArrayList<>();
        if (s != null && searchText.equals(application.getNowSearch())) {
            analysisJSON(s);
            showCities();
        }
    }

    public void analysisJSON(String s) {
        try {
            JSONObject jsonObject = new JSONObject(s);
            JSONArray results = jsonObject.getJSONArray("results");
            for (int i = 0; i < results.length(); i++) {
                JSONObject cityObject = results.getJSONObject(i);
                City city = new City();
                city.setName(cityObject.getString("name"));
                city.setId(cityObject.getString("id"));
                city.setPath(cityObject.getString("path"));
                cities.add(city);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void showCities() {
        List<City> listItems = citiesAdapter.getData();
        listItems.clear();
        for (int i = 0; i < cities.size(); i++) {
            City listItem = new City();
            listItem.setPath(cities.get(i).getPath());
            listItem.setId(cities.get(i).getId());
            listItem.setName(cities.get(i).getName());
            listItems.add(listItem);
        }
        if (listItems.size() == 0) {
            City listItem = new City();
            listItem.setPath(SearchCityActivity.SEARCH_NO_RESULT);
            listItems.add(listItem);
            listItem = new City();
            listItem.setPath(SearchCityActivity.AUTOMATIC_LOCATION);
            listItem.setId("GPS");
            listItems.add(listItem);
        }
        citiesAdapter.notifyDataSetChanged();
    }

    public static String convertStreamToString(InputStream is) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();
        String line = null;
        try {
            while ((line = reader.readLine()) != null) {
                sb.append(line + "/n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return sb.toString();
    }

}
