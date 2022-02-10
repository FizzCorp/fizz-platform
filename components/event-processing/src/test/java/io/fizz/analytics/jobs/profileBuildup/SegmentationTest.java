package io.fizz.analytics.jobs.profileBuildup;

import io.fizz.analytics.common.Segmentation;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Objects;

class SegmentationTest {
    private final String SEGMENT_TITLE_1 = "test_segment";
    private final int SEGMENT_BOUND_LOWER_1 = 10;
    private final int SEGMENT_BOUND_UPPER_1 = 20;
    private final String SEGMENT_TITLE_2 = "test_segment_2";
    private final int SEGMENT_BOUND_LOWER_2 = 30;
    private final int SEGMENT_BOUND_UPPER_2 = 40;

    @Nested
    class SegmentRangeIntegerTest {
        @Test
        @DisplayName("it should create valid range segmentValue")
        void validRangeTest() {
            final Segmentation.SegmentRangeInteger segment = new Segmentation.SegmentRangeInteger(
                    SEGMENT_TITLE_1,
                    SEGMENT_BOUND_LOWER_1,
                    SEGMENT_BOUND_UPPER_1
            );

            assert (segment.getTitle().equals(SEGMENT_TITLE_1));
            assert (segment.isIncluded(SEGMENT_BOUND_LOWER_1));
            assert (segment.isIncluded(SEGMENT_BOUND_UPPER_1));
            assert (segment.isIncluded((SEGMENT_BOUND_UPPER_1 + SEGMENT_BOUND_LOWER_1)/2));
        }

        @Test
        @DisplayName("it should specify correct age segmentation")
        void ageSegmentTest() {
            assert (Segmentation.SEGMENT_DAY_1_3.isIncluded(0));
            assert (Segmentation.SEGMENT_DAY_1_3.isIncluded(1));
            assert (Segmentation.SEGMENT_DAY_1_3.isIncluded(2));

            assert (Segmentation.SEGMENT_DAY_4_7.isIncluded(3));
            assert (Segmentation.SEGMENT_DAY_4_7.isIncluded(5));
            assert (Segmentation.SEGMENT_DAY_4_7.isIncluded(6));

            assert (Segmentation.SEGMENT_DAY_8_14.isIncluded(7));
            assert (Segmentation.SEGMENT_DAY_8_14.isIncluded(10));
            assert (Segmentation.SEGMENT_DAY_8_14.isIncluded(13));

            assert (Segmentation.SEGMENT_DAY_15_30.isIncluded(14));
            assert (Segmentation.SEGMENT_DAY_15_30.isIncluded(22));
            assert (Segmentation.SEGMENT_DAY_15_30.isIncluded(29));

            assert (Segmentation.SEGMENT_DAY_31_.isIncluded(30));
            assert (Segmentation.SEGMENT_DAY_31_.isIncluded(350));
            assert (Segmentation.SEGMENT_DAY_31_.isIncluded(Integer.MAX_VALUE));
        }

        @Test
        @DisplayName("it should not create range segmentValue with invalid title")
        void invalidTitleTest() {
            Assertions.assertThrows(IllegalArgumentException.class, () -> {
                new Segmentation.SegmentRangeInteger(null, SEGMENT_BOUND_LOWER_1, SEGMENT_BOUND_UPPER_1);
            });
        }
    }

    @Nested
    class SegmentRangeLongTest {
        @Test
        @DisplayName("it should create valid range segmentValue")
        void validRangeTest() {
            final Segmentation.SegmentRangeLong segment = new Segmentation.SegmentRangeLong(
                    SEGMENT_TITLE_1,
                    SEGMENT_BOUND_LOWER_1,
                    SEGMENT_BOUND_UPPER_1
            );

            assert (segment.getTitle().equals(SEGMENT_TITLE_1));
            assert (segment.isIncluded((long)SEGMENT_BOUND_LOWER_1));
            assert (segment.isIncluded((long)SEGMENT_BOUND_UPPER_1));
            assert (segment.isIncluded((long)(SEGMENT_BOUND_UPPER_1 + SEGMENT_BOUND_LOWER_1)/2));
        }

        @Test
        @DisplayName("it should specify correct spender segmentValue")
        void spenderSegmentTest() {
            assert (Segmentation.SEGMENT_NONE.isIncluded(0L));
            assert (!Segmentation.SEGMENT_NONE.isIncluded(1L));

            assert (Segmentation.SEGMENT_MINNOW.isIncluded(1L));
            assert (Segmentation.SEGMENT_MINNOW.isIncluded(100L));
            assert (Segmentation.SEGMENT_MINNOW.isIncluded(999L));

            assert (Segmentation.SEGMENT_DOLPHIN.isIncluded(1000L));
            assert (Segmentation.SEGMENT_DOLPHIN.isIncluded(5000L));
            assert (Segmentation.SEGMENT_DOLPHIN.isIncluded(9999L));

            assert (Segmentation.SEGMENT_WHALE.isIncluded(10000L));
            assert (Segmentation.SEGMENT_WHALE.isIncluded(50000L));
            assert (Segmentation.SEGMENT_WHALE.isIncluded(Long.MAX_VALUE));
        }

        @Test
        @DisplayName("it should not create range segmentValue with invalid title")
        void invalidTitleTest() {
            Assertions.assertThrows(IllegalArgumentException.class, () -> {
                new Segmentation.SegmentRangeInteger(null, SEGMENT_BOUND_LOWER_1, SEGMENT_BOUND_UPPER_1);
            });
        }
    }

    @Nested
    class SegmentGroupTest {
        @Test
        @DisplayName("it should create a valid segmentValue group")
        void validGroupTest() {
            final Segmentation.SegmentGroup<Integer> group = new Segmentation.SegmentGroup<Integer>(new ArrayList<Segmentation.AbstractSegment<Integer>>(){
                {
                    add(new Segmentation.SegmentRangeInteger(SEGMENT_TITLE_1, SEGMENT_BOUND_LOWER_1, SEGMENT_BOUND_UPPER_1));
                    add(new Segmentation.SegmentRangeInteger(SEGMENT_TITLE_2, SEGMENT_BOUND_LOWER_2, SEGMENT_BOUND_UPPER_2));
                }
            });

            assert (group.fetchSegment(SEGMENT_BOUND_LOWER_1).equals(SEGMENT_TITLE_1));
            assert (group.fetchSegment(SEGMENT_BOUND_UPPER_1).equals(SEGMENT_TITLE_1));
            assert (group.fetchSegment((SEGMENT_BOUND_LOWER_1+SEGMENT_BOUND_UPPER_1)/2).equals(SEGMENT_TITLE_1));
            assert (group.fetchSegment(SEGMENT_BOUND_LOWER_2).equals(SEGMENT_TITLE_2));
            assert (group.fetchSegment(SEGMENT_BOUND_UPPER_2).equals(SEGMENT_TITLE_2));
            assert (group.fetchSegment((SEGMENT_BOUND_LOWER_2+SEGMENT_BOUND_UPPER_2)/2).equals(SEGMENT_TITLE_2));
            assert (Objects.isNull(group.fetchSegment(SEGMENT_BOUND_UPPER_2+10)));
            assert (Objects.isNull(group.fetchSegment(SEGMENT_BOUND_LOWER_1-10)));
            assert (Objects.isNull(group.fetchSegment((SEGMENT_BOUND_UPPER_1+SEGMENT_BOUND_LOWER_2)/2)));
        }

        @Test
        @DisplayName("it should not create a group for invalid segmentValue")
        void invalidSegmentsTest() {
            Assertions.assertThrows(IllegalArgumentException.class, () -> {
                new Segmentation.SegmentGroup<Integer>(null);
            });
        }
    }
}
