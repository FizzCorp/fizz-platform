package io.fizz.gateway.services;

import io.fizz.common.domain.CountryCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;


class GeoLocationTest {
    @Test
    @DisplayName("it should convert ip address to country code.")
    void ipAddressConversionUSTest() {
        CountryCode countryCode = GeoLocationService.instance().getCountryCode("72.229.28.185");
        assert (countryCode.value().equals("US"));
    }

    @Test
    @DisplayName("it should fail to convert ip address into country code.")
    void ipAddressConversionUnknownTest() {
        CountryCode countryCode = GeoLocationService.instance().getCountryCode("192.168.1.1");
        assert (countryCode.value().equals("??"));
    }
}
