package io.fizz.gateway.services;

import com.maxmind.geoip2.DatabaseReader;
import com.maxmind.geoip2.exception.GeoIp2Exception;
import com.maxmind.geoip2.record.Country;
import io.fizz.common.LoggingService;
import io.fizz.common.domain.CountryCode;

import java.io.*;
import java.net.InetAddress;

public class GeoLocationService {
    private static final LoggingService.Log logger = LoggingService.getLogger(GeoLocationService.class);
    private static final GeoLocationService instance = new GeoLocationService();

    private DatabaseReader dbReader;

    public static GeoLocationService instance() {
        return instance;
    }

    private GeoLocationService() {
        final ClassLoader loader = getClass().getClassLoader();
        try(InputStream in = loader.getResourceAsStream("GeoLite2-Country.mmdb")) {
            dbReader = new DatabaseReader.Builder(in).build();
        }
        catch (IOException ex) {
            logger.fatal("missing GeoLite2-Country.mmdb file in resources folder.");
        }
    }

    public CountryCode getCountryCode(String ipAddress) {
        try {
            InetAddress inetAddress = InetAddress.getByName(ipAddress);
            String countryCode = getCountryCode(inetAddress);
            return new CountryCode(countryCode);
        } catch (Exception e) {
            logger.warn("Unable to convert ip address (" + ipAddress + ") to country code");
        }
        return CountryCode.unknown();
    }

    private String getCountryCode(InetAddress inetAddress) throws IOException, GeoIp2Exception {
        return getCountry(inetAddress).getIsoCode();
    }

    private Country getCountry(InetAddress inetAddress) throws IOException, GeoIp2Exception {
        return dbReader.country(inetAddress).getCountry();
    }
}
