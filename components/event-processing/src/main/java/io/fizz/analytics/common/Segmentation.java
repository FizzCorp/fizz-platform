package io.fizz.analytics.common;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public interface Segmentation {
    abstract class AbstractSegment<ValueType> {
        private final String title;

        AbstractSegment(final String aTitle) {
            if (Objects.isNull(aTitle)) {
                throw new IllegalArgumentException("invalid segment title specified.");
            }
            title = aTitle;
        }

        public String getTitle() {
            return title;
        }

        abstract public boolean isIncluded(ValueType value);
    }

    class SegmentRangeInteger extends AbstractSegment<Integer> {
        private final int lowerBound;
        private final int upperBound;

        public SegmentRangeInteger(final String aTitle, int aLowerBound, int aUpperBound) {
            super(aTitle);

            lowerBound = aLowerBound;
            upperBound = aUpperBound;
        }

        @Override
        public boolean isIncluded(Integer value) {
            return value >= lowerBound && value <= upperBound;
        }
    }

    class SegmentRangeLong extends AbstractSegment<Long> {
        private final long lowerBound;
        private final long upperBound;

        public SegmentRangeLong(final String aTitle, long aLowerBound, long aUpperBound) {
            super(aTitle);

            lowerBound = aLowerBound;
            upperBound = aUpperBound;
        }

        @Override
        public boolean isIncluded(Long value) {
            return value >= lowerBound && value <= upperBound;
        }
    }

    class SegmentGroup<ValueType> {
        private final List<AbstractSegment<ValueType>> segments;
        public SegmentGroup(final List<AbstractSegment<ValueType>> aSegments) {
            if (Objects.isNull(aSegments)) {
                throw new IllegalArgumentException("invalid segment specified.");
            }
            segments = aSegments;
        }

        public String fetchSegment(ValueType value) {
            for (final AbstractSegment<ValueType> segment: segments) {
                if (segment.isIncluded(value)) {
                    return segment.getTitle();
                }
            }

            return null;
        }
    }

    SegmentRangeInteger SEGMENT_DAY_1_3 = new SegmentRangeInteger("days_1_3", 0, 2);
    SegmentRangeInteger SEGMENT_DAY_4_7 = new SegmentRangeInteger("days_4_7", 3, 6);
    SegmentRangeInteger SEGMENT_DAY_8_14 = new SegmentRangeInteger("days_8_14", 7, 13);
    SegmentRangeInteger SEGMENT_DAY_15_30 = new SegmentRangeInteger("days_15_30", 14, 29);
    SegmentRangeInteger SEGMENT_DAY_31_ = new SegmentRangeInteger("days_31_", 30, Integer.MAX_VALUE);
    SegmentGroup<Integer> lifecycle = new SegmentGroup<>(new ArrayList<AbstractSegment<Integer>>(){
        {
            add(SEGMENT_DAY_1_3);
            add(SEGMENT_DAY_4_7);
            add(SEGMENT_DAY_8_14);
            add(SEGMENT_DAY_15_30);
            add(SEGMENT_DAY_31_);
        }
    });

    SegmentRangeLong SEGMENT_NONE = new SegmentRangeLong("none", 0L, 0L);
    SegmentRangeLong SEGMENT_MINNOW = new SegmentRangeLong("minnow", 1L, 999L);
    SegmentRangeLong SEGMENT_DOLPHIN = new SegmentRangeLong("dolphin", 1000L, 9999L);
    SegmentRangeLong SEGMENT_WHALE = new SegmentRangeLong("whale", 10000L, Long.MAX_VALUE);
    SegmentGroup<Long> spender = new SegmentGroup<>(new ArrayList<AbstractSegment<Long>>(){
        {
            add(SEGMENT_NONE);
            add(SEGMENT_MINNOW);
            add(SEGMENT_DOLPHIN);
            add(SEGMENT_WHALE);
        }
    });
}
