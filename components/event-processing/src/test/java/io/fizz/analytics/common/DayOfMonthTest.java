package io.fizz.analytics.common;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class DayOfMonthTest {
    @Test
    @DisplayName("it should create a valid object")
    void basicValidityTest() {
        for (int day = 1; day <= 31; day++) {
            final DayOfMonth dom = new DayOfMonth(day);
            assert (dom.getValue() == day);
        }
    }

    @Test
    @DisplayName("it should not create a day object for value less than 1")
    void invalidMinTest() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> new DayOfMonth(0));
    }

    @Test
    @DisplayName("it should not create a day object for value greater than 31")
    void invalidMaxTest() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> new DayOfMonth(32));
    }

    @Test
    @DisplayName("it should only equate two days based on value")
    void equalsTest() {
        final DayOfMonth day1 = new DayOfMonth(16);
        final DayOfMonth day2 = new DayOfMonth(16);

        assert (day1.equals(day2));

        final DayOfMonth day3 = new DayOfMonth(23);

        assert (!day1.equals(day3));
    }

    @Test
    @DisplayName("similar objects should produce the same hash code")
    void equalHashCodeTest() {
        final DayOfMonth day1 = new DayOfMonth(4);
        final DayOfMonth day2 = new DayOfMonth(4);

        assert (day1.hashCode() == day2.hashCode());
    }
}
