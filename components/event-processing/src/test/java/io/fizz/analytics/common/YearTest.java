package io.fizz.analytics.common;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class YearTest {
    @Test
    @DisplayName("it should create a valid object")
    void basicValidityTest() {
        final Year obj = new Year(1970);
        assert (obj.getValue() == 1970);
    }

    @Test
    @DisplayName("it should not create a year object for value less than 1970")
    void invalidMinTest() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> new Year(1969));
    }

    @Test
    @DisplayName("it should only equate two months based on value")
    void equalsTest() {
        final Year year1 = new Year(2012);
        final Year year2 = new Year(2012);

        assert (year1.equals(year2));

        final Year year3 = new Year(2018);

        assert (!year1.equals(year3));
    }

    @Test
    @DisplayName("similar objects should produce the same hash code")
    void equalHashCodeTest() {
        final Year year1 = new Year(2012);
        final Year year2 = new Year(2012);

        assert (year1.hashCode() == year2.hashCode());
    }
}
