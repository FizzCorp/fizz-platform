package io.fizz.chatcommon.domain;

import io.fizz.common.domain.DomainErrorException;

public enum LanguageCode {
    AFRIKAANS("af"),
    ARABIC("ar"),
    BULGARIAN("bg"),
    BANGLA("bn"),
    BOSNIAN_LATIN("bs"),
    CATALAN("ca"),
    CZECH("cs"),
    CANTONESE("yue"),
    CHINESE_SIMPLIFIED("zh-Hans"),
    CHINESE_TRADITIONAL("zh-Hant"),
    WELSH("cy"),
    DANISH("da"),
    GERMAN("de"),
    GREEK("el"),
    ENGLISH("en"),
    SPANISH("es"),
    ESTONIAN("et"),
    PERSIAN("fa"),
    FINNISH("fi"),
    FILIPINO("fil"),
    FIJIAN("fj"),
    FRENCH("fr"),
    HEBREW("he"),
    HINDI("hi"),
    CROATIAN("hr"),
    HAITIAN_CREOLE("ht"),
    HUNGARIAN("hu"),
    INDONESIAN("id"),
    ICELANDIC("is"),
    ITALIAN("it"),
    JAPANESE("ja"),
    KOREAN("ko"),
    LITHUANIAN("lt"),
    LATVIAN("lv"),
    MALAGASY("mg"),
    MALAY("ms"),
    MALTESE("mt"),
    HMONGDAW("mww"),
    NORWEGIAN("nb"),
    DUTCH("nl"),
    QUERETARO_OTOMI("otq"),
    POLISH("pl"),
    PORTUGUESE("pt"),
    ROMANIAN("ro"),
    RUSSIAN("ru"),
    SLOVAK("sk"),
    SLOVENIAN("sl"),
    SAMOAN("sm"),
    SERBIAN_CYRILLIC("sr-Cyrl"),
    SERBIAN_LATIN("sr-Latn"),
    SWEDISH("sv"),
    KISWAHILI("sw"),
    TAMIL("ta"),
    TELUGU("te"),
    THAI("th"),
    KLINGON("tlh"),
    KLINGON_PLQAD("tlh-Qaak"),
    TOGAN("to"),
    TURKISH("tr"),
    TAHITIAN("ty"),
    UKRAINIAN("uk"),
    URDU("ur"),
    VIETNAMESE("vi"),
    YUCATECMAYA("yua");

    private String value;
    LanguageCode(final String aValue) {
        value = aValue;
    }

    public String value() {
        return value;
    }

    public static LanguageCode fromValue(final String aValue) throws DomainErrorException {
        for (LanguageCode lang: LanguageCode.values()) {
            if (lang.value.equals(aValue)) {
                return lang;
            }
        }

        throw new DomainErrorException(String.format("invalid_language_code_%s", aValue));
    }
}
