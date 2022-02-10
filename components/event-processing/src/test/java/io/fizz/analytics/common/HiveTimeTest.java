package io.fizz.analytics.common;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Date;

class HiveTimeTest {
    private final long timestamp = 1524983609000L;

    @Test
    @DisplayName("it should create a time object from milliseconds")
    void createFromMillisecondsTest() {
        final HiveTime time = new HiveTime(timestamp);
        assert (time.year.getValue() == 2018);
        assert (time.month.getValue() == 4);
        assert (time.dayOfMonth.getValue() == 29);
        assert (time.yyyymmmdd().equals("2018-04-29"));
        assert (time.yyyymmm().equals("2018-04"));
    }

    @Test
    @DisplayName("it should create a time object from a value Date")
    void createFromDateTest() {
        final HiveTime time = new HiveTime(new Date(timestamp));
        assert (time.year.getValue() == 2018);
        assert (time.month.getValue() == 4);
        assert (time.dayOfMonth.getValue() == 29);
        assert (time.yyyymmmdd().equals("2018-04-29"));
        assert (time.yyyymmm().equals("2018-04"));
    }

    @Test
    @DisplayName("it should not create a time object from missing date")
    void missingDateTest() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            new HiveTime(null);
        });
    }

    @Test
    @DisplayName("it should create a time object from invalid date")
    void invalidDateTest() {
        // april does not have 31 days
        final HiveTime time = new HiveTime(2018, 4, 31);
        assert (time.yyyymmmdd().equals("2018-05-01"));
    }

    @Test
    @DisplayName("it should only equate to similar dates")
    void equalityTest() {
        final HiveTime time1 = new HiveTime(2018, 4, 12);
        final HiveTime time2 = new HiveTime(2018, 4, 12);
        assert (time1.equals(time2));

        final HiveTime time3 = new HiveTime(2018, 4, 11);
        assert (!time1.equals(time3));

        final HiveTime time4 = new HiveTime(2018, 3, 12);
        assert (!time1.equals(time4));

        final HiveTime time5 = new HiveTime(2017, 4, 12);
        assert (!time1.equals(time5));
    }

    @Test
    @DisplayName("it should add days to a Hive Time")
    void addDays() {
        final HiveTime time1 = new HiveTime(2018, 4, 12);
        final HiveTime time2 = time1.addDays(12);
        assert (time2.dayOfMonth.getValue() == 24);

        final HiveTime time3 = time1.addDays(20);
        assert (time3.dayOfMonth.getValue() == 2);
        assert (time3.month.getValue() == 5);

        final HiveTime time4 = new HiveTime(2018, 12, 12);
        final HiveTime time5 = time4.addDays(20);
        assert (time5.dayOfMonth.getValue() == 1);
        assert (time5.month.getValue() == 1);
        assert (time5.year.getValue() == 2019);
    }

    @Test
    @DisplayName("it should subtract days to a Hive Time")
    void subtractDays() {
        final HiveTime time1 = new HiveTime(2018, 3, 12);
        final HiveTime time2 = time1.addDays(-2);
        assert (time2.dayOfMonth.getValue() == 10);

        final HiveTime time3 = time1.addDays(-20);
        assert (time3.dayOfMonth.getValue() == 20);
        assert (time3.month.getValue() == 2);

        final HiveTime time4 = new HiveTime(2019, 1, 12);
        final HiveTime time5 = time4.addDays(-20);
        assert (time5.dayOfMonth.getValue() == 23);
        assert (time5.month.getValue() == 12);
        assert (time5.year.getValue() == 2018);
    }

    @Test
    @DisplayName("it should return previous month")
    void previousMonth() {
        final HiveTime time1 = new HiveTime(2018, 3, 12);
        final HiveTime time2 = time1.previousMonth(16);
        assert (time2.dayOfMonth.getValue() == 16);
        assert (time2.month.getValue() == 2);
        assert (time2.year.getValue() == 2018);

        final HiveTime time3 = new HiveTime(2019, 1, 12);
        final HiveTime time4 = time3.previousMonth(16);
        assert (time4.dayOfMonth.getValue() == 16);
        assert (time4.month.getValue() == 12);
        assert (time4.year.getValue() == 2018);
    }

    @Test
    @DisplayName("it should hash equal times to sames codes")
    void hashEqualityTest() {
        final HiveTime time1 = new HiveTime(2018, 4, 12);
        final HiveTime time2 = new HiveTime(2018, 4, 12);
        assert (time1.hashCode() == time2.hashCode());
    }
}
