package io.fizz.analytics.jobs.profileBuildup;

import io.fizz.analytics.common.AbstractTransformer;
import io.fizz.analytics.common.HiveTime;
import io.fizz.analytics.common.Profile;
import io.fizz.analytics.common.adapter.UserRowAdapter;
import io.fizz.analytics.common.repository.AbstractUserRepository;
import io.fizz.analytics.common.repository.HBaseUserRepository;
import io.fizz.analytics.common.source.hive.HiveProfileEnrichedEventTableSchema;
import io.fizz.analytics.common.source.hive.HiveRawEventTableSchema;
import io.fizz.analytics.domain.User;
import io.fizz.common.LoggingService;
import io.fizz.common.domain.*;
import io.fizz.common.domain.events.AbstractDomainEvent;
import org.apache.spark.api.java.function.FlatMapGroupsFunction;
import org.apache.spark.api.java.function.MapFunction;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Encoders;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.catalyst.encoders.RowEncoder;

import java.io.Serializable;
import java.util.*;

/**
 *  Updates and appends user profile segment to events.
 */
public class ProfileSegmentTransformer implements AbstractTransformer<Row,Row>, Serializable {
    private final LoggingService.Log logger = LoggingService.getLogger(ProfileSegmentTransformer.class);

    @Override
    public Dataset<Row> transform(final Dataset<Row> sourceDS, final HiveTime time) {
        if (Objects.isNull(sourceDS)) {
            throw new IllegalArgumentException("invalid source data set specified.");
        }

        return sourceDS
        .groupByKey((MapFunction<Row,String>) row -> {
            final String appId = HiveRawEventTableSchema.appId(row);
            final String userId = HiveRawEventTableSchema.userId(row);
            return appId + ":" + userId;
        }, Encoders.STRING())
        .flatMapGroups((FlatMapGroupsFunction<String,Row,Row>)(aActorId, aGroupIterator) -> {
            try {
                final AbstractUserRepository userRepo = userRepoFactory();
                final List<Row> events = convertToList(aGroupIterator);
                final ApplicationId appId = new ApplicationId(HiveProfileEnrichedEventTableSchema.appId(events.get(0)));
                final UserId userId = new UserId(HiveProfileEnrichedEventTableSchema.userId(events.get(0)));
                final User user = userRepo.fetchWithId(appId, userId);
                final Profile profile = buildProfile(events, user);
                final List<Row> outEvents = createSegmentedEvents(events, profile);

                if (profile.firstSession()) {
                    emitNewUserEvent(events, profile, outEvents, time);
                }

                emitUserUpdateProfileEvent(profile.user(), outEvents, time);

                return outEvents.iterator();
            }
            catch (DomainErrorException ex) {
                logger.error(ex.error().reason());
                return null;
            }
        }, RowEncoder.apply(new HiveProfileEnrichedEventTableSchema().schema()));
    }

    protected AbstractUserRepository userRepoFactory() {
        return new HBaseUserRepository();
    }

    private List<Row> convertToList(final Iterator<Row> eventsIterator) {
        final List<Row> rows = new ArrayList<>();
        while (eventsIterator.hasNext()) {
            rows.add(eventsIterator.next());
        }
        return rows;
    }

    private Profile buildProfile(final List<Row> aEvents, final User aUser) throws DomainErrorException {
        return new Profile.Builder()
                .setEvents(aEvents)
                .setUser(aUser)
                .get();
    }

    private List<Row> createSegmentedEvents(final List<Row> aSourceEvents, final Profile aProfile) {
        final List<Row> outEvents = new ArrayList<>();

        for (final Row event: aSourceEvents) {
            outEvents.add(
                new HiveProfileEnrichedEventTableSchema.RowBuilder()
                .setId(HiveRawEventTableSchema.id(event))
                .setAppId(HiveRawEventTableSchema.appId(event))
                .setUserId(HiveRawEventTableSchema.userId(event))
                .setCountryCode(HiveRawEventTableSchema.countryCode(event))
                .setSessionId(HiveRawEventTableSchema.sessionId(event))
                .setType(HiveRawEventTableSchema.eventType(event))
                .setVersion(HiveRawEventTableSchema.version(event))
                .setOccurredOn(HiveRawEventTableSchema.occurredOn(event))
                .setPlatform(HiveRawEventTableSchema.platform(event))
                .setBuild(HiveRawEventTableSchema.build(event))
                .setCustom01(HiveRawEventTableSchema.custom01(event))
                .setCustom02(HiveRawEventTableSchema.custom02(event))
                .setCustom03(HiveRawEventTableSchema.custom03(event))
                .setFields(HiveRawEventTableSchema.fields(event))
                .setAge(aProfile.lifecycleSegment())
                .setSpend(aProfile.spenderSegment())
                .setYear(HiveRawEventTableSchema.year(event))
                .setMonth(HiveRawEventTableSchema.month(event))
                .setDay(HiveRawEventTableSchema.day(event))
                .get()
            );
        }

        return outEvents;
    }

    private void emitNewUserEvent(final List<Row> aSourceEvents, final Profile aProfile,
                                  final List<Row> aOutEvents, HiveTime time) {
        final Row row = aSourceEvents.get(0);
        final long firstTimeActiveTS = aProfile.user().firstTimeActiveTS();

         aOutEvents.add(
                 new HiveProfileEnrichedEventTableSchema.RowBuilder()
                .setId(UUID.randomUUID().toString())
                .setAppId(HiveProfileEnrichedEventTableSchema.appId(row))
                .setUserId(HiveProfileEnrichedEventTableSchema.userId(row))
                .setCountryCode(HiveProfileEnrichedEventTableSchema.countryCode(row))
                .setSessionId(HiveProfileEnrichedEventTableSchema.sessionId(row))
                .setType(EventType.NEW_USER_CREATED.value())
                .setVersion(AbstractDomainEvent.VERSION)
                .setOccurredOn(firstTimeActiveTS)
                .setPlatform(HiveProfileEnrichedEventTableSchema.platform(row))
                .setBuild(HiveProfileEnrichedEventTableSchema.build(row))
                .setCustom01(HiveProfileEnrichedEventTableSchema.custom01(row))
                .setCustom02(HiveProfileEnrichedEventTableSchema.custom02(row))
                .setCustom03(HiveProfileEnrichedEventTableSchema.custom03(row))
                .setFields("{}")
                .setAge(aProfile.lifecycleSegment())
                .setSpend(aProfile.spenderSegment())
                .setYear(Integer.toString(time.year.getValue()))
                .setMonth(time.yyyymmm())
                .setDay(time.yyyymmmdd())
                .get()
         );
    }

    private void emitUserUpdateProfileEvent(final User aUser, final List<Row> aOutEvents, HiveTime time) {
        aOutEvents.add(UserRowAdapter.toEventRow(aUser, time));
    }
}
