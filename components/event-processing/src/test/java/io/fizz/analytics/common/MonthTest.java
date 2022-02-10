package io.fizz.analytics.common;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class MonthTest {
    @Test
    @DisplayName("it should create a valid object")
    void basicValidityTest() {
        for (int month = 1; month <= 12; month++) {
            final Month obj = new Month(month);
            assert (obj.getValue() == month);
        }
    }

    @Test
    @DisplayName("it should not create a month object for value less than 1")
    void invalidMinTest() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> new Month(0));
    }

    @Test
    @DisplayName("it should not create a month object for value greater than 12")
    void invalidMaxTest() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> new Month(13));
    }

    @Test
    @DisplayName("it should only equate two months based on value")
    void equalsTest() {
        final Month month1 = new Month(6);
        final Month month2 = new Month(6);

        assert (month1.equals(month2));

        final Month month3 = new Month(10);

        assert (!month1.equals(month3));
    }

    @Test
    @DisplayName("similar objects should produce the same hash code")
    void equalHashCodeTest() {
        final Month month1 = new Month(4);
        final Month month2 = new Month(4);

        assert (month1.hashCode() == month2.hashCode());
    }
}
