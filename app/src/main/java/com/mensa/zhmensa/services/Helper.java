package com.mensa.zhmensa.services;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.preference.PreferenceManager;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.mensa.zhmensa.R;
import com.mensa.zhmensa.models.IMenu;
import com.mensa.zhmensa.models.Mensa;
import com.mensa.zhmensa.models.MensaCategory;

import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.lang.reflect.Type;
import java.util.Locale;

@SuppressWarnings("unused")
public class Helper {


    private static Gson gson;

    @SuppressWarnings("HardCodedStringLiteral")
    private static final String[] UNIQUE_MENU_MENSA = {"Tannenbar"};
    @SafeVarargs
    @NonNull
    public static <T> T firstNonNull(T... objects){
        for (T obj: objects) {
            if(obj != null)
                return obj;
        }
        return null;
    }

    /**
     * Gets an id for a given menu. Id will be the same for all similar menus (e.g. Green from Polyterasse)
     * @param mensaName
     * @param menuName
     * @param loadIndex
     * @param mealType
     * @return
     */
    @SuppressWarnings("HardCodedStringLiteral")
    public static String getIdForMenu(String mensaName, String menuName, int loadIndex, Mensa.MenuCategory mealType) {
        if(isDubiousMenuName(mensaName, menuName)) {
            return "'uni:" + mensaName + "' pos: " + loadIndex + " mealtype:" + mealType;
        }
        return "mensa:" + mensaName + ",Menu:" + menuName;
    }


    /**
     * Checks if a mensa has unique menu names.
     * E.g. Tannebar always changes the names (Burritos, Mezze.) and therefore this function will return true
      * @param mensaName the mensa name
     * @param menuName the menu name
     * @return true if name is not unqiue, false otherwise
     */
    private static boolean isDubiousMenuName(String mensaName, String menuName) {
       for (String mensa : UNIQUE_MENU_MENSA) {
           if(mensa.equals(mensaName)) {
               return true;
           }
       }
       return false;
    }


    /**
     *
     * @return the dstart of this week as date time object
     */
    static DateTime getStartOfWeek() {
        DateTime dateTime = new DateTime(System.currentTimeMillis());

        if(dateTime.getDayOfWeek() > 5) {
            dateTime = dateTime.plusWeeks(1);
        }

        return dateTime.withDayOfWeek(1);
    }

    /**
     * Current start of the week day in formet yyyy-MM-dd
     * @param offset
     * @return
     */
    public static String getDay(int offset) {
        return getDay(getStartOfWeek().plusDays(offset), DateTimeFormat.forPattern("yyyy-MM-dd"));
    }

    /**
     * Current start of the week day in formet yyyy-MM-dd
     * @param offset
     * @return
     */
    private static String getDay(DateTime date, DateTimeFormatter dtfOut) {
        return dtfOut.print(date);
    }

    public static String getHumanReadableDay(int selectedDay) {
        return getDay(getStartOfWeek().plusDays(selectedDay), DateTimeFormat.forPattern("dd MMMM"));
    }

    public static String getDayForPattern(int selectedDay, String pattern) {
        return getDay(getStartOfWeek().plusDays(selectedDay), DateTimeFormat.forPattern(pattern));
    }

    public static String getDayForPatternAndStart(DateTime start, String pattern) {
        return getDay(start, DateTimeFormat.forPattern(pattern));
    }


    static String convertMensaToJsonString(Mensa mensa) {
        if(gson == null)
            gson = new GsonBuilder().registerTypeAdapter(IMenu.class, new InterfaceAdapter<IMenu>())
                    .registerTypeAdapter(MensaCategory.class, new InterfaceAdapter<MensaCategory>())
                    .create();

        return gson.toJson(mensa);
    }

    static Mensa getMensaFromJsonString(String jsonString) {
        if(gson == null)
            gson = new GsonBuilder().registerTypeAdapter(IMenu.class, new InterfaceAdapter<IMenu>())
                    .registerTypeAdapter(MensaCategory.class, new InterfaceAdapter<MensaCategory>())
                    .create();


        return gson.fromJson(jsonString, Mensa.class);

    }


    @SuppressWarnings("HardCodedStringLiteral")
    public static String getNameForDay(Mensa.Weekday day, Context ctx) {
        if(ctx != null) {
            switch (day) {
                case MONDAY:
                    return ctx.getString(R.string.monday);
                case TUESDAY:
                    return ctx.getString(R.string.tuesday);
                case WEDNESDAY:
                    return ctx.getString(R.string.wednesday);
                case THURSDAY:
                    return ctx.getString(R.string.thursday);
                case FRIDAY:
                    return ctx.getString(R.string.friday);
            }
        } else {
            switch (day) {
                case MONDAY:
                    return "Mo";
                case TUESDAY:
                    return "Tu";
                case WEDNESDAY:
                    return "We";
                case THURSDAY:
                    return "Th";
                case FRIDAY:
                    return "Fr";
            }
        }
        return String.valueOf(day);
    }

    /**
     *
     * @return 0 for monday, 4 for friday...
     */
    static int getCurrentDay() {
        int currentDay = new DateTime(System.currentTimeMillis()).getDayOfWeek();
        if(currentDay > DateTimeConstants.FRIDAY)
            return 0;

        return currentDay - DateTimeConstants.MONDAY;
    }

    static boolean isDataStillValid(Long lastUpdated) {
            Log.d("lastupdatecheck", "Last updated: " + new DateTime(lastUpdated));
        return getDay(0).equals(getDay(new DateTime(lastUpdated), DateTimeFormat.forPattern("yyyy-MM-dd")));
    }

    public static String removeHtmlTags(@NonNull  String str) {
        return str.replaceAll("<[^>]+>","");
    }

    public static String getFavoriteTitle() {
        // TODO
        return "Favoriten";
    }

    public static String getLabelForMealType(Mensa.MenuCategory category, Context context) {
        switch (category){
            case LUNCH: return context.getString(R.string.lunch);
            case DINNER: return context.getString(R.string.dinner);
        }

        return context.getString(R.string.unknown);
    }


    @SuppressWarnings("SameReturnValue")
    @NonNull
    public static String getLanguageCode(Context ctx) {
        if(ctx!= null)
            return PreferenceManager.getDefaultSharedPreferences(ctx).getString("language_preference", Locale.getDefault().getLanguage());
        else
            return Locale.getDefault().getLanguage();
    }

    // TODO
    public static Boolean isMenuVegi(String name, String description) {
        return false;
    }


    @SuppressWarnings("HardCodedStringLiteral")
    static final class InterfaceAdapter<T> implements JsonSerializer<T>, JsonDeserializer<T> {
        @NonNull
        public JsonElement serialize(T object, Type interfaceType, JsonSerializationContext context) {
            final JsonObject wrapper = new JsonObject();
            wrapper.addProperty("type", object.getClass().getName());
            wrapper.add("data", context.serialize(object));
            return wrapper;
        }

        public T deserialize(JsonElement elem, Type interfaceType, JsonDeserializationContext context) throws JsonParseException {
            final JsonObject wrapper = (JsonObject) elem;
            final JsonElement typeName = get(wrapper, "type");
            final JsonElement data = get(wrapper, "data");
            final Type actualType = typeForName(typeName);
            return context.deserialize(data, actualType);
        }

        private Type typeForName(final JsonElement typeElem) {
            try {
                return Class.forName(typeElem.getAsString());
            } catch (ClassNotFoundException e) {
                throw new JsonParseException(e);
            }
        }

        private JsonElement get(final JsonObject wrapper, String memberName) {
            final JsonElement elem = wrapper.get(memberName);
            if (elem == null) throw new JsonParseException("no '" + memberName + "' member found in what was expected to be an interface wrapper");
            return elem;
        }
    }

}
