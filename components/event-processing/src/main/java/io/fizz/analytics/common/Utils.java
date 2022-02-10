package io.fizz.analytics.common;

import io.fizz.common.ConfigService;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Objects;
import java.util.TimeZone;

public class Utils {

    public static long dateToTimestamp(int year, int month, int day) {
        return fromYMD(year, month, day).getTime();
    }

    public static Date fromYMD(int year, int month, int day) {
        final String timeStr = "" +
                (year) +
                ("-") +
                (month < 10 ? "0" + month : month) +
                ("-") +
                (day < 10 ? "0" + day : day);

        return fromYYYYMMDD(timeStr);
    }

    public static Date fromYYYYMMDD(final String aTimeStr) {
        if (Objects.isNull(aTimeStr)) {
            throw new IllegalArgumentException("invalid time string");
        }

        final SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        format.setTimeZone(TimeZone.getTimeZone("UTC"));

        try {
            return format.parse(aTimeStr);
        }
        catch (ParseException ex) {
            throw new IllegalArgumentException("invalid time specified.");
        }
    }

    public static HiveTime previousDay() {
        final int day = ConfigService.instance().getNumber("job.event.processing.day").intValue();
        final Calendar calendar = Calendar.getInstance();
        calendar.setTimeZone(TimeZone.getTimeZone("UTC"));
        calendar.add(Calendar.DATE, day);
        return new HiveTime(calendar.getTime());
    }
}
