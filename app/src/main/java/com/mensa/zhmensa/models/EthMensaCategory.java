package com.mensa.zhmensa.models;

import android.util.Log;

import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.mensa.zhmensa.R;
import com.mensa.zhmensa.services.Helper;
import com.mensa.zhmensa.services.HttpUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import cz.msebera.android.httpclient.Header;

import java.util.ArrayList;
import java.util.List;


public class EthMensaCategory extends MensaCategory {
     // e.g. https://www.webservices.ethz.ch/gastro/v1/RVRI/Q1E1/meals/de/2019-07-05/lunch
    private String apiRoute = "https://www.webservices.ethz.ch/gastro/v1/RVRI/Q1E1/meals/de/";

    public EthMensaCategory(String displayName) {
        super(displayName);
    }

    public EthMensaCategory(String displayName, List<String> knownMensaIds) {
        super(displayName, knownMensaIds);
    }



    public static List<IMenu> getMenusFromJsonArray(String mensa, Mensa.MenuCategory mealType, JSONArray array) throws JSONException{
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
            String allergeneStr = "";
            for (int j = 0; j < allergene.length(); j++) {
                //String[] split = allergene.getString(j).split(":");
                //allergeneStr += split[split.length - 1];
                allergeneStr += allergene.getJSONObject(j).getString("label").replace("\\/", "/");
                if(j < allergene.length() - 1)
                    allergeneStr += ", ";
            }

            menus.add(
                    new Menu(Helper.getIdForMenu(mensa, meal.getString("label"), i, mealType),
                            meal.getString("label"),
                            descriptionStr,
                            pricesStr,
                            allergeneStr
                    )
            );
        }
        return menus;
    }

    public static List<Mensa> convertJsonResponseToList(JSONArray array, Mensa.Weekday day, Mensa.MenuCategory menuCategory) {
        List<Mensa> mensaList = new ArrayList<>();
        for (int i = 0; i < array.length(); i++) {
            try {
                JSONObject obj = (JSONObject) array.get(i);
                Log.d("JSON ARR:",obj.toString());
                String name = obj.getString("mensa");
                Mensa mensa = new Mensa(name, name);
                mensa.setMenuForDayAndCategory(day, menuCategory, getMenusFromJsonArray(name, menuCategory,  obj.getJSONArray("meals")));
                mensaList.add(mensa);
            } catch (JSONException e) {
                Log.e("Error", e.getMessage());
            }
        }
        return mensaList;
    }



    MensaListObservable getMensaUpdateForDayAndMeal(final Mensa.Weekday day, final Mensa.MenuCategory menuCategory) {
        final MensaListObservable obs = new MensaListObservable(day, menuCategory);
        int offset = 0;
        String mealType = menuCategory == Mensa.MenuCategory.LUNCH ? "lunch" : "dinner";
        Log.d("Mensa api request", "Api rquest with menucat " + String.valueOf(menuCategory));

        HttpUtils.getByUrl("https://www.webservices.ethz.ch/gastro/v1/RVRI/Q1E1/meals/de/"+ Helper.getDay(day.day) +"/" + mealType, new RequestParams(), new JsonHttpResponseHandler() {

            public void onSuccess(int statusCode, Header[] headers, JSONArray timeline) {
                Log.e("Got menucat", String.valueOf(menuCategory));
                Log.d("ETH Mensa API Response", timeline.toString());
                obs.addNewMensaList(convertJsonResponseToList(timeline, day, menuCategory));
            }

            public void onFailure(int statusCode, cz.msebera.android.httpclient.Header[] headers, String responseString, Throwable throwable) {
                Log.e("error in get request", throwable.getMessage());
            }
        });
        return obs;
    }

    @Override
    public List<MensaListObservable> loadMensasFromAPI() {
        List<MensaListObservable> list = new ArrayList<>();
        for (Mensa.Weekday day: Mensa.Weekday.values()) {
            for (Mensa.MenuCategory cat: Mensa.MenuCategory.values()) {
                list.add(getMensaUpdateForDayAndMeal(day, cat));
            }
        }
        return list;
    }



    @Override
    public Integer getCategoryIconId() {
        return R.drawable.ic_eth_2;
    }
}
