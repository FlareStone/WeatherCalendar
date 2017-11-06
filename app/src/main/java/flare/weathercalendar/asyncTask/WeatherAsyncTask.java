package flare.weathercalendar.asyncTask;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.telephony.CellLocation;
import android.telephony.TelephonyManager;
import android.telephony.cdma.CdmaCellLocation;
import android.telephony.gsm.GsmCellLocation;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.net.ssl.HttpsURLConnection;

import flare.weathercalendar.MyApplication;
import flare.weathercalendar.entity.City;
import flare.weathercalendar.entity.Weather;
import flare.weathercalendar.fragment.WeatherFragment;

/**
 * Created by 54333 on 2017/7/21.
 */

public class WeatherAsyncTask extends AsyncTask<String, Integer, Integer> {
    private View view;
    private String cityId;
    private String nowWeather;
    private String dailyWeather;
    private String suggestion;
    private Weather weather;
    private Activity activity;
    private final int ERROR = 0;
    private final int OK = 1;
    private final int OPERATOR_NOT_SUPPORT = 2;
    private final int GPS_FAIL = 3;

    public WeatherAsyncTask(View view, Activity activity) {
        this.view = view;
        this.activity = activity;
    }

    @Override
    protected Integer doInBackground(String... params) {
        String searchText = null;
        cityId = params[0];
        if (cityId.equals("GPS")) {
            if (isWifi(activity)) { //ip定位
                searchText = "ip";
            } else {    //基站定位
                try {
                    searchText = cellTowerLocate();
                } catch (IOException | JSONException e) {
                    e.printStackTrace();
                }
                if (searchText == null) {
                    return OPERATOR_NOT_SUPPORT;
                } else if (searchText.equals("0")) {
                    return GPS_FAIL;
                }
            }
        } else {
            searchText = cityId;
        }
        try {
            URL url = new URL("https://api.seniverse.com/v3/weather/now.json?" +
                    "key=pzcqocgaqtwegswj&location=" + searchText);
            HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
            conn.connect();
            int code = conn.getResponseCode();
            if (code == 200) {
                InputStream inputStream = conn.getInputStream();
                nowWeather = CityAsyncTask.convertStreamToString(inputStream);
            } else {
                Log.d("Download", "failed code=" + code);
                return ERROR;
            }

            url = new URL("https://api.seniverse.com/v3/weather/daily.json?" +
                    "key=pzcqocgaqtwegswj&location=" + searchText + "&start=0&days=3");
            conn = (HttpsURLConnection) url.openConnection();
            conn.connect();
            code = conn.getResponseCode();
            if (code == 200) {
                InputStream inputStream = conn.getInputStream();
                dailyWeather = CityAsyncTask.convertStreamToString(inputStream);
            } else {
                Log.d("Download", "failed code=" + code);
                return ERROR;
            }

            url = new URL("https://api.seniverse.com/v3/life/suggestion.json?" +
                    "key=pzcqocgaqtwegswj&location=" + searchText);
            conn = (HttpsURLConnection) url.openConnection();
            conn.connect();
            code = conn.getResponseCode();
            if (code == 200) {
                InputStream inputStream = conn.getInputStream();
                suggestion = CityAsyncTask.convertStreamToString(inputStream);
            } else {
                Log.d("Download", "failed code=" + code);
                return ERROR;
            }
            return OK;
        } catch (IOException e) {
            Log.d("Download", "超时");
        }
        return ERROR;
    }

    @Override
    protected void onPostExecute(Integer s) {
        MyApplication application = (MyApplication) activity.getApplication();
        if (s == OK) {
            analysisWeatherJSON();
            saveWeather();
            WeatherFragment.putWeatherOnUI(view, weather);
            if (application.getSwipeRefreshLayout() != null) {
                application.getSwipeRefreshLayout().setRefreshing(false);
                application.setSwipeRefreshLayout(null);
                Toast.makeText(activity, "刷新成功", Toast.LENGTH_SHORT).show();
            }
        } else if (s == ERROR) {
            Toast.makeText(activity, "出现未知错误", Toast.LENGTH_SHORT).show();
        } else if (s == OPERATOR_NOT_SUPPORT) {
            Toast.makeText(activity, "您的运营商不支持自动定位", Toast.LENGTH_SHORT).show();
        } else if (s == GPS_FAIL) {
            Toast.makeText(activity, "自动定位失败，请稍后或移动位置后再试", Toast.LENGTH_SHORT).show();
        }
        if (application.getSwipeRefreshLayout() != null) {
            application.getSwipeRefreshLayout().setRefreshing(false);
            application.setSwipeRefreshLayout(null);
        }
    }

    private void saveWeather() {
        SharedPreferences sharedPreferences = view.getContext().getSharedPreferences("city",
                Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("cityId", weather.getCity().getId());
        editor.putString("cityName", weather.getCity().getName());
        editor.putString("cityPath", weather.getCity().getPath());
        editor.putString("weatherText", weather.getText());
        editor.putString("weatherCode", weather.getCode());
        editor.putString("date", weather.getDate());
        editor.putString("temperature", weather.getTemperature());
        editor.putString("highTemperature", weather.getHighTemperature());
        editor.putString("lowTemperature", weather.getLowTemperature());
        editor.putString("windDirection", weather.getWindDirection());
        editor.putString("windScale", weather.getWindScale());
        editor.putString("uv", weather.getUv());
        editor.putString("comfort", weather.getComfort());
        editor.putString("carWashing", weather.getCarWashing());
        editor.putString("dressing", weather.getDressing());
        editor.putString("traffic", weather.getTraffic());
        editor.putString("morningSport", weather.getMorningSport());
        editor.putString("sport", weather.getSport());
        editor.putString("umbrella", weather.getUmbrella());
        editor.putString("shopping", weather.getShopping());
        editor.putString("tomorrowText", weather.getTomorrowText());
        editor.putString("tomorrowCode", weather.getTomorrowCode());
        editor.putString("tomorrowHighTemperature", weather.getTomorrowHighTemperature());
        editor.putString("tomorrowLowTemperature", weather.getTomorrowLowTemperature());
        editor.putString("tomorrowWindDirection", weather.getTomorrowWindDirection());
        editor.putString("tomorrowWindScale", weather.getTomorrowWindScale());
        editor.putString("afterTomorrowText", weather.getAfterTomorrowText());
        editor.putString("afterTomorrowCode", weather.getAfterTomorrowCode());
        editor.putString("afterTomorrowHighTemperature", weather.getAfterTomorrowHighTemperature());
        editor.putString("afterTomorrowLowTemperature", weather.getAfterTomorrowLowTemperature());
        editor.putString("afterTomorrowWindDirection", weather.getAfterTomorrowWindDirection());
        editor.putString("afterTomorrowWindScale", weather.getAfterTomorrowWindScale());
        editor.apply();
    }

    private void analysisWeatherJSON() {
        weather = new Weather();
        JSONObject jsonObject;
        JSONObject results;
        try {
            jsonObject = new JSONObject(nowWeather);
            results = jsonObject.getJSONArray("results").getJSONObject(0);
            JSONObject location = results.getJSONObject("location");
            City city = new City();
            city.setId(cityId); //自动定位时为字符串"GPS"
            city.setName(location.getString("name"));
            city.setPath(location.getString("path"));
            weather.setCity(city);
            JSONObject now = results.getJSONObject("now");
            weather.setText(now.getString("text"));
            weather.setCode(now.getString("code"));
            weather.setTemperature(now.getString("temperature"));
            weather.setWindDirection(now.getString("wind_direction"));
            weather.setWindScale(now.getString("wind_scale"));
        } catch (JSONException e) {
            Log.d("Weather", "获取实时天气失败");
        }

        try {
            jsonObject = new JSONObject(dailyWeather);
            results = jsonObject.getJSONArray("results").getJSONObject(0);
            JSONArray daily = results.getJSONArray("daily");
            JSONObject day = daily.getJSONObject(0);
            weather.setLowTemperature(day.getString("low"));
            weather.setHighTemperature(day.getString("high"));
            day = daily.getJSONObject(1);
            weather.setTomorrowCode(day.getString("code_day"));
            weather.setTomorrowHighTemperature(day.getString("high"));
            weather.setTomorrowLowTemperature(day.getString("low"));
            weather.setTomorrowText(day.getString("text_day"));
            weather.setTomorrowWindDirection(day.getString("wind_direction"));
            weather.setTomorrowWindScale(day.getString("wind_scale"));
            day = daily.getJSONObject(2);
            weather.setAfterTomorrowCode(day.getString("code_day"));
            weather.setAfterTomorrowHighTemperature(day.getString("high"));
            weather.setAfterTomorrowLowTemperature(day.getString("low"));
            weather.setAfterTomorrowText(day.getString("text_day"));
            weather.setAfterTomorrowWindDirection(day.getString("wind_direction"));
            weather.setAfterTomorrowWindScale(day.getString("wind_scale"));
        } catch (JSONException e) {
            Log.d("Weather", "获取逐日天气失败");
        }

        try {
            jsonObject = new JSONObject(suggestion);
            results = jsonObject.getJSONArray("results").
                    getJSONObject(0).getJSONObject("suggestion");
            weather.setUv(results.getJSONObject("uv").getString("brief"));
            weather.setComfort(results.getJSONObject("comfort").getString("brief"));
            weather.setCarWashing(results.getJSONObject("car_washing").getString("brief"));
            weather.setDressing(results.getJSONObject("dressing").getString("brief"));
            weather.setTraffic(results.getJSONObject("traffic").getString("brief"));
            weather.setMorningSport(results.getJSONObject("morning_sport").getString("brief"));
            weather.setSport(results.getJSONObject("sport").getString("brief"));
            weather.setUmbrella(results.getJSONObject("umbrella").getString("brief"));
            weather.setShopping(results.getJSONObject("shopping").getString("brief"));
        } catch (JSONException e) {
            Log.d("Weather", "获取生活指数失败");
        }

        SimpleDateFormat dateFormat = new SimpleDateFormat("MM月dd日 E");
        weather.setDate(dateFormat.format(new Date()));
    }

    private static boolean isWifi(Context mContext) {
        ConnectivityManager connectivityManager = (ConnectivityManager) mContext
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetInfo = connectivityManager.getActiveNetworkInfo();
        if (activeNetInfo != null
                && activeNetInfo.getType() == ConnectivityManager.TYPE_WIFI) {
            return true;
        }
        return false;
    }

    private String cellTowerLocate() throws IOException, JSONException {
        String locationCode = null;
        String receivedJSON;
        TelephonyManager telephonyManager = (TelephonyManager) activity.getSystemService(Context.TELEPHONY_SERVICE);
        String imsi = telephonyManager.getSubscriberId();
        if (imsi != null) {
            if (imsi.startsWith("46000") || imsi.startsWith("46002") || imsi.startsWith("46001")) { //移动联通
                TelephonyManager mTelephonyManager =
                        (TelephonyManager) activity.getSystemService(Context.TELEPHONY_SERVICE);

                // 中国移动和中国联通获取LAC、CID的方式
                GsmCellLocation location = (GsmCellLocation) mTelephonyManager.getCellLocation();
                int lac = location.getLac();
                int cell = location.getCid();
                int mnc = 0;    //默认移动
                if (imsi.startsWith("46001")) { //联通
                    mnc = 1;
                }

                URL url = new URL("http://v.juhe.cn/cell/get?mnc=" + mnc + "&cell=" + cell +
                        "&lac=" + lac + "&key=e50164075d8913dee53e236d6dcbe0bb");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.connect();
                int code = conn.getResponseCode();
                if (code == 200) {
                    InputStream inputStream = conn.getInputStream();
                    receivedJSON = CityAsyncTask.convertStreamToString(inputStream);
                    locationCode = analysisLocationJSON1(receivedJSON);
                }
            } else if (imsi.startsWith("46003")) {  //电信
                TelephonyManager tel = (TelephonyManager)activity.getSystemService(Context.TELEPHONY_SERVICE);
                CellLocation cel = tel.getCellLocation();
                int nPhoneType = tel.getPhoneType();
                //电信   CdmaCellLocation
                if (nPhoneType == 2 && cel instanceof CdmaCellLocation) {
                    CdmaCellLocation cdmaCellLocation = (CdmaCellLocation) cel;
                    int sid=cdmaCellLocation.getSystemId();
                    int nid=cdmaCellLocation.getNetworkId();
                    int bid=cdmaCellLocation.getBaseStationId();
                    URL url = new URL("http://v.juhe.cn/cdma/?sid=" + sid + "&cellid=" + bid +
                            "&nid=" + nid + "&key=e50164075d8913dee53e236d6dcbe0bb");
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.connect();
                    int code = conn.getResponseCode();
                    if (code == 200) {
                        InputStream inputStream = conn.getInputStream();
                        receivedJSON = CityAsyncTask.convertStreamToString(inputStream);
                        analysisLocationJSON2(receivedJSON);
                    }
                }
            }
        }
        return locationCode;
    }

    private String analysisLocationJSON1(String receivedJSON) throws JSONException {    //移动联通JSON
        if (receivedJSON == null) {
            return null;
        }
        JSONObject result = new JSONObject(receivedJSON);
        if (result.get("resultcode").equals("200")) {
            JSONObject dataObject = result.getJSONObject("result").
                    getJSONArray("data").getJSONObject(0);
            String lng = dataObject.getString("LNG");
            String lat = dataObject.getString("LAT");
            return lat + ":" + lng;
        } else {
            return "0";
        }
    }

    private String analysisLocationJSON2(String receivedJSON) throws JSONException {    //移动联通JSON
        if (receivedJSON == null) {
            return null;
        }
        JSONObject result = new JSONObject(receivedJSON);
        if (result.get("resultcode").equals("200")) {
            JSONObject dataObject = result.getJSONObject("result");
            String lng = dataObject.getString("lon");
            String lat = dataObject.getString("lat");
            return lat + ":" + lng;
        } else {
            return "0";
        }
    }


}
