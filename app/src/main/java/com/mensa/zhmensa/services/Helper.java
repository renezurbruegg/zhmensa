package com.mensa.zhmensa.services;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.mensa.zhmensa.models.IMenu;
import com.mensa.zhmensa.models.Mensa;
import com.mensa.zhmensa.models.MensaCategory;

import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.lang.reflect.Type;

@SuppressWarnings("unused")
public class Helper {


    private static Gson gson;

    private static final String[] UNIQUE_MENU_MENSA = {"Tannebar"};
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
    public static DateTime getStartOfWeek() {
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


    public static String getNameForDay(Mensa.Weekday day) {
        switch (day) {
            case MONDAY:
                return "Mo";
            case TUESDAY:
                return "Di";
            case WEDNESDAY:
                return "Mi";
            case THURSDAY:
                return "Do";
            case FRIDAY:
                return "Fr";
        }
        return String.valueOf(day);
    }

    /**
     *
     * @return 0 for monday, 4 for friday...
     */
    static int getCurrentDay() {
        return Math.min(new DateTime(System.currentTimeMillis()).getDayOfWeek() , DateTimeConstants.FRIDAY) - DateTimeConstants.MONDAY;
    }

    public static boolean isDataStillValid(Long lastUpdated) {
            Log.d("lastupdatecheck", "Last updated: " + new DateTime(lastUpdated));
        return getDay(0).equals(getDay(new DateTime(lastUpdated), DateTimeFormat.forPattern("yyyy-MM-dd")));
    }

    public static String removeHtmlTags(@NonNull  String str) {
        return str.replaceAll("<[^>]+>","");
    }


    @SuppressWarnings("SameReturnValue")
    @NonNull
    public String getLanguageCode() {
        return "de";
    }



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
