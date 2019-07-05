package com.mensa.zhmensa.models;

import android.util.Log;
import android.widget.Toast;

import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.mensa.zhmensa.services.HttpUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import cz.msebera.android.httpclient.Header;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Observable;
import java.util.TimeZone;


public class EthMensaCategory extends MensaCategory {
     // e.g. https://www.webservices.ethz.ch/gastro/v1/RVRI/Q1E1/meals/de/2019-07-05/lunch
    private String apiRoute = "https://www.webservices.ethz.ch/gastro/v1/RVRI/Q1E1/meals/de/";

    public EthMensaCategory(String displayName) {
        super(displayName);
    }


    public static List<IMenu> getMenusFromJsonArray(JSONArray array) throws JSONException{
        List<IMenu> menus = new ArrayList<>();
        for (int i = 0; i < array.length(); i++) {
            JSONObject meal = array.getJSONObject(i);
            JSONArray description = meal.getJSONArray("description");
            String descriptionStr = "";
            for (int j = 0; j < description.length(); j++) {

                descriptionStr += description.getString(j) + "\n";
            }
            JSONObject prices = meal.getJSONObject("prices");
            String pricesStr = prices.getString("student") + " / " + prices.getString("staff") + " / " + prices.getString("extern");

            JSONArray allergene = meal.getJSONArray("allergens");
            String allergeneStr = "Allergene: ";
            for (int j = 0; j < allergene.length(); j++) {
                //String[] split = allergene.getString(j).split(":");
                //allergeneStr += split[split.length - 1];
                allergeneStr += allergene.getJSONObject(j).getString("label").replace("\\/", "/");
                if(j < allergene.length() - 1)
                    allergeneStr += ", ";
            }

            menus.add(
                    new Menu(
                        meal.getString("label"),
                        descriptionStr,
                        pricesStr,
                        allergeneStr
                    )
            );
        }
        return menus;
    }

    public static List<Mensa> convertJsonResponseToList(JSONArray array) {
        List<Mensa> mensaList = new ArrayList<>();
        for (int i = 0; i < array.length(); i++) {
            try {
                JSONObject obj = (JSONObject) array.get(i);
                Log.d("JSON ARR:",obj.toString());
                String name = obj.getString("mensa");
                Mensa mensa = new Mensa(name, getMenusFromJsonArray(obj.getJSONArray("meals")));
                mensaList.add(mensa);
            } catch (JSONException e) {
                Log.e("Error", e.getMessage());
            }
        }
        return mensaList;
    }

    public static String getCurrentDay() {
        Date date = new Date(System.currentTimeMillis());

        // Conversion
        SimpleDateFormat sdf;
        sdf = new SimpleDateFormat("yyyy-MM-dd");
        sdf.setTimeZone(TimeZone.getTimeZone("MESZ"));
        return sdf.format(date);
    }

    @Override
    public Observable loadMensasFromAPI() {
        final MensaListObservable obs = new MensaListObservable();

        HttpUtils.getByUrl("https://www.webservices.ethz.ch/gastro/v1/RVRI/Q1E1/meals/de/"+ getCurrentDay() +"/lunch", new RequestParams(), new JsonHttpResponseHandler() {

            public void onSuccess(int statusCode, Header[] headers, JSONArray timeline) {
                Log.d("ETH Mensa API Response", timeline.toString());
                obs.addNewMensaList(convertJsonResponseToList(timeline));
            }

            public void onFailure(int statusCode, cz.msebera.android.httpclient.Header[] headers, String responseString, Throwable throwable) {
                Log.e("error in get request", throwable.getMessage());
            }
        });
        return obs;
    }
}
