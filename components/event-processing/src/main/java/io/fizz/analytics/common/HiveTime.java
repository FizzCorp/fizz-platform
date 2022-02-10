package io.fizz.analytics.common;

import java.io.Serializable;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.Objects;
import java.util.TimeZone;

public class HiveTime implements Serializable {
    public final Year year;
    public final Month month;
    public final DayOfMonth dayOfMonth;

    public HiveTime (long aMilliseconds) {
        this(new Date(aMilliseconds));
    }

    public HiveTime(Date aTime) {
        if (Objects.isNull(aTime)) {
            throw new IllegalArgumentException("invalid date specified");
        }

        final Calendar cal = Calendar.getInstance();
        cal.setTime(aTime);

        year = new Year(cal.get(Calendar.YEAR));
        month = new Month(cal.get(Calendar.MONTH) + 1);
        dayOfMonth = new DayOfMonth(cal.get(Calendar.DAY_OF_MONTH));
    }

    public HiveTime(int aYear, int aMonth, int aDayOfMonth) {
        this(Utils.fromYMD(aYear, aMonth, aDayOfMonth));
    }

    public String yyyymmmdd() {
        final String dayStr = dayOfMonth.getValue() < 10 ? "0" + dayOfMonth.getValue() : Integer.toString(dayOfMonth.getValue());
        return yyyymmm() + "-" + dayStr;

    }

    public String yyyymmm() {
        final String yearStr = Integer.toString(year.getValue());
        final String monthStr = month.getValue() < 10 ? "0" + month.getValue() : Integer.toString(month.getValue());

        return yearStr + "-" + monthStr;
    }

    public HiveTime addDays(final int aOffset) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(Utils.fromYMD(year.getValue(), month.getValue(), dayOfMonth.getValue()));
        cal.setTimeZone(TimeZone.getTimeZone("UTC"));
        cal.add(Calendar.DATE, aOffset);

        return new HiveTime(cal.getTime());
    }

    public HiveTime previousMonth(final int aDate) {
        final int month = this.month.getValue() > 1 ? this.month.getValue() - 1 : 12;
        final int year = this.month.getValue() > 1 ? this.year.getValue() : this.year.getValue() - 1;

        return new HiveTime(year, month, aDate);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        HiveTime hiveTime = (HiveTime) o;

        if (!year.equals(hiveTime.year)) return false;
        if (!month.equals(hiveTime.month)) return false;
        return dayOfMonth.equals(hiveTime.dayOfMonth);
    }

    @Override
    public int hashCode() {
        int result = year.hashCode();
        result = 31 * result + month.hashCode();
        result = 31 * result + dayOfMonth.hashCode();
        return result;
    }
}
