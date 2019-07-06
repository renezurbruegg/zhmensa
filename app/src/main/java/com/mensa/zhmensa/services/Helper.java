package com.mensa.zhmensa.services;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

public class Helper {

    public static <T> T firstNonNull(T... objects){
        for (T obj: objects) {
            if(obj != null)
                return obj;
        }
        return null;
    }



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

}
