package io.fizz.common.domain;

import java.util.Objects;

public class QueryRange {
    private static final DomainErrorException ERROR_INVALID_QUERY_START = new DomainErrorException(new DomainError("invalid_query_start"));
    private static final DomainErrorException ERROR_INVALID_QUERY_END = new DomainErrorException(new DomainError("invalid_query_end"));
    private static final DomainErrorException ERROR_INVALID_QUERY_RANGE = new DomainErrorException(new DomainError("invalid_query_range"));
    public static final long MAX_TIMESTAMP = 9999999999L;

    private final long start;
    private final long end;
    public QueryRange(Long aStart, Long aEnd) throws DomainErrorException {
        if (Objects.isNull(aStart) || aStart < 0 || aStart > MAX_TIMESTAMP) {
            throw ERROR_INVALID_QUERY_START;
        }
        if (Objects.isNull(aEnd) || aEnd < 0 || aEnd > MAX_TIMESTAMP) {
            throw ERROR_INVALID_QUERY_END;
        }
        if (aStart > aEnd) {
            throw ERROR_INVALID_QUERY_RANGE;
        }

        start = aStart;
        end = aEnd;
    }

    public long getStart() {
        return start;
    }

    public long getEnd() {
        return end;
    }
}
