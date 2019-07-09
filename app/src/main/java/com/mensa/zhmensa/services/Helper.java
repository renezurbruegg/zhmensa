package com.mensa.zhmensa.services;

import android.util.Log;

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
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.lang.reflect.Type;

public class Helper {


    private static Gson gson;

    private static final String[] UNIQUE_MENU_MENSA = {"Tannebar"};
    public static <T> T firstNonNull(T... objects){
        for (T obj: objects) {
            if(obj != null)
                return obj;
        }
        return null;
    }

    public static String getIdForMenu(String mensaName, String menuName, int loadIndex, Mensa.MenuCategory mealType) {
        if(isUniqueMenueName(mensaName, menuName)) {
            return "'uni:" + mensaName + "' pos: " + loadIndex + " mealtype:" + mealType;
        }
        return "mensa:" + mensaName + ",Menu:" + menuName;
    }



    private static boolean isUniqueMenueName(String mensaName, String menuName) {
       for (String mensa : UNIQUE_MENU_MENSA) {
           if(mensa.equals(mensaName)) {
               return true;
           }
       }
       return false;
    };


    public static DateTime getStartOfWeek() {
        DateTime dateTime = new DateTime(System.currentTimeMillis());

        if(dateTime.getDayOfWeek() > 5) {
            dateTime = dateTime.plusWeeks(1);
        }

        return dateTime.withDayOfWeek(1);

   /*     long firstDayOfWeekTimestamp = dateTime.withDayOfWeek(1).getMillis();

        DateTimeFormatter dtfOut = DateTimeFormat.forPattern("MM/dd/yyyy");
        Log.d("current dt", dtfOut.print(dateTime));
        if(dateTime.getDayOfWeek() > 5) {
            Log.d("datetime", "Found saturda or Sunday");
            dateTime.plusWeeks(1);
            Log.d("current dt", dtfOut.print(dateTime.plusWeeks(1)));
        }

        return "";*/
    }

    public static String getDay(int offset) {
        DateTime date = getStartOfWeek().plusDays(offset);
        DateTimeFormatter dtfOut = DateTimeFormat.forPattern("yyyy-MM-dd");
        return dtfOut.print(date);
/*        Date date = new Date(System.currentTimeMillis());

        // Conversion
        SimpleDateFormat sdf;
        sdf = new SimpleDateFormat("yyyy-MM-dd");
        sdf.setTimeZone(TimeZone.getTimeZone("MESZ"));
        return sdf.format(date);*/
    }



    public static String convertMensaToJsonString(Mensa mensa) {
        if(gson == null)
            gson = new GsonBuilder().registerTypeAdapter(IMenu.class, new InterfaceAdapter<IMenu>())
                    .registerTypeAdapter(MensaCategory.class, new InterfaceAdapter<MensaCategory>())
                    .create();

        return gson.toJson(mensa);
    }

    public static Mensa getMensaFromJsonString(String jsonString) {
        if(gson == null)
            gson = new GsonBuilder().registerTypeAdapter(IMenu.class, new InterfaceAdapter<IMenu>())
                    .registerTypeAdapter(MensaCategory.class, new InterfaceAdapter<MensaCategory>())
                    .create();


        Mensa mensa =  gson.fromJson(jsonString, Mensa.class);
        /*Log.d("gmfjs","Loaded mensa " + mensa.getDisplayName());
        for (Mensa.Weekday day: Mensa.Weekday.values()) {
            for (Mensa.MenuCategory manuCat : Mensa.MenuCategory.values()){
                Log.d(day + ": " + manuCat, mensa.getMenusForDayAndCategory(day, manuCat).toString());
            }

        }*/
        return mensa;
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

    public String getLanguageCode() {
        return "de";
    }



    static final class InterfaceAdapter<T> implements JsonSerializer<T>, JsonDeserializer<T> {
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
