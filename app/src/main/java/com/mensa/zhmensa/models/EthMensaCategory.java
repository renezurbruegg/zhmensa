package com.mensa.zhmensa.models;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.mensa.zhmensa.R;
import com.mensa.zhmensa.services.Helper;
import com.mensa.zhmensa.services.HttpUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import cz.msebera.android.httpclient.Header;


@SuppressWarnings("HardCodedStringLiteral")
public class EthMensaCategory extends MensaCategory {
     // e.g. https://www.webservices.ethz.ch/gastro/v1/RVRI/Q1E1/meals/de/2019-07-05/lunch
    private static final String apiRoute = "https://www.webservices.ethz.ch/gastro/v1/RVRI/Q1E1/meals/";

    private static final String[] EXPECTED_MENSA_NAMES = {"food&lab", "Foodtrailer ETZ", "food market - pizza pasta", "food market - grill bbQ", "BELLAVISTA", "Polysnack", "Clausiusbar", "Polysnack", "FUSION coffee", "Rice Up!", "Fusion meal", "G-ESSbar", "Tannenbar", "food market - green day", "Mensa Polyterrasse", "Dozentenfoyer"}; //NON-NLS

    public EthMensaCategory(@NonNull String displayName, int pos) {
        super(displayName, pos);
    }

    protected EthMensaCategory(@SuppressWarnings("SameParameterValue") @NonNull String displayName, @NonNull List<String> knownMensaIds, int pos) {
        super(displayName, knownMensaIds, pos);
    }


    /**
     * Converts a JSON response for a given mensa to a list with menus
     * @param mensa The mensa that contain these menus
     * @param mealType The mealtype (Dinner / Lunch)
     * @param array The Json respone
     * @return List with all menu items stored in the json array
     * @throws JSONException
     */
    @NonNull
    private static List<IMenu> getMenusFromJsonArray(String mensa, Mensa.MenuCategory mealType, JSONArray array) throws JSONException {

        List<IMenu> menus = new ArrayList<>();

        for (int i = 0; i < array.length(); i++) {
            JSONObject meal = array.getJSONObject(i);
            JSONArray description = meal.getJSONArray("description");
            StringBuilder descriptionStr = new StringBuilder();
            for (int j = 0; j < description.length(); j++) {
                descriptionStr.append(description.getString(j)).append("\n");
            }

            JSONObject prices = meal.getJSONObject("prices");

            String studentPrice = prices.getString("student");
            String staffPrice = prices.getString("staff");
            String externPrice = prices.getString("extern");

            String typeString = meal.getString("type").toLowerCase();
            StringBuilder pricesStr = new StringBuilder();

            if(!studentPrice.equals("0.00") && !studentPrice.equals("null"))
                pricesStr.append(studentPrice);

            if(!staffPrice.equals("0.00") && !staffPrice.equals("null"))
                pricesStr.append(( pricesStr.length() == 0 ? "" : " / ") + staffPrice);

            if(!externPrice.equals("0.00") && !externPrice.equals("null"))
                pricesStr.append(( pricesStr.length() == 0 ? "" : " / ") + externPrice);

            JSONArray allergene = meal.getJSONArray("allergens");
            StringBuilder allergeneStr = new StringBuilder();

            for (int j = 0; j < allergene.length(); j++) {
                allergeneStr.append(allergene.getJSONObject(j).getString("label").replace("\\/", "/"));
                if(j < allergene.length() - 1)
                    allergeneStr.append(", ");
            }

            Menu newMenu = new Menu(Helper.getIdForMenu(mensa, meal.getString("label"), i, mealType),
                    meal.getString("label"),
                    Helper.removeHtmlTags(descriptionStr.toString()),
                    pricesStr.toString(),
                    allergeneStr.toString()
            );

            if(newMenu.getName().contains("grill")){
                Log.d("got", "it");
            }
            if(typeString.equals("vegetarisch") || typeString.equals("vegetarian")) {
                newMenu.setVegi(true);
            } else if( typeString.equals("fish") || typeString.equals("fisch") || typeString.equals("meat") || typeString.equals("fleisch")) {
                newMenu.setVegi(false);
            } else if(mensa.equals("Dozentenfoyer") && newMenu.getName().equalsIgnoreCase("favorite")) {
                newMenu.setVegi(false);
            } else {
                newMenu.setVegi(meal.getJSONArray("origins").length() == 0);
            }

            menus.add(newMenu);
        }
        return menus;
    }


    @NonNull
    private static List<Mensa> convertJsonResponseToList(JSONArray array, Mensa.Weekday day, Mensa.MenuCategory menuCategory) {
        List<Mensa> mensaList = new ArrayList<>();
        List<String> mensaNames = new ArrayList<>();

        for (int i = 0; i < array.length(); i++) {
            try {
                JSONObject obj = (JSONObject) array.get(i);
                String name = obj.getString("mensa");
                mensaNames.add(name);
                Mensa mensa = new Mensa(name, name);
                mensa.setMenuForDayAndCategory(day, menuCategory, getMenusFromJsonArray(name, menuCategory,  obj.getJSONArray("meals")));
                mensaList.add(mensa);
            } catch (JSONException e) {
                Log.e("Error", (e == null ? "null" : e.getMessage() ));
            }
        }

        for(String expectedName : EXPECTED_MENSA_NAMES) {
            if(!mensaNames.contains(expectedName)) {
                mensaList.add(new Mensa(expectedName, expectedName, true));
            }
        }
        return mensaList;
    }


    private String getApiRoute(Mensa.Weekday day, Mensa.MenuCategory mealType, String languageCode) {
        String mealStr = mealType == Mensa.MenuCategory.LUNCH ? "lunch" : "dinner";

        return apiRoute + languageCode + "/" + Helper.getDay(day.day) + "/" + mealStr;
    }

    @NonNull
    private MensaListObservable getMensaUpdateForDayAndMeal(@NonNull final Mensa.Weekday day, final Mensa.MenuCategory menuCategory, String languageCode) {
        final MensaListObservable obs = new MensaListObservable(day, menuCategory);

        // Log.d("Mensa api request", "Api rquest with menucat " + String.valueOf(menuCategory));

       final String url =  getApiRoute(day, menuCategory, languageCode);

        Log.d("Mensa api request", "url: " + url);

        HttpUtils.getByUrl(url, new RequestParams(), new JsonHttpResponseHandler() {

            public void onSuccess(int statusCode, Header[] headers, @NonNull JSONArray timeline) {
       //         Log.d("ETH Mensa API Response", timeline.toString());
                obs.addNewMensaList(convertJsonResponseToList(timeline, day, menuCategory));
            }

            public void onFailure(int statusCode, cz.msebera.android.httpclient.Header[] headers, String responseString, @Nullable Throwable throwable) {
                Log.e("error in get request", (throwable == null) ? "null" : throwable.getMessage());
            }

            public void onFailure(int statusCode, Header[] header, @Nullable Throwable t, JSONObject obj){
                Log.e("URL: ", url);
                Log.e("error in get request", (t == null) ? "null" : t.getMessage());
                Log.e("Response " + statusCode, String.valueOf(obj));
            }
        });
        return obs;
    }

    @NonNull
    @Override
    public List<MensaListObservable> loadMensasFromAPI(String languageCode) {
        List<MensaListObservable> list = new ArrayList<>();
        for (Mensa.Weekday day: Mensa.Weekday.values()) {
            for (Mensa.MenuCategory cat: Mensa.MenuCategory.values()) {
                list.add(getMensaUpdateForDayAndMeal(day, cat, languageCode));
            }
        }
        return list;
    }

    @Override
    public Integer getCategoryIconId() {
        return R.drawable.ic_eth_2;
    }
}
