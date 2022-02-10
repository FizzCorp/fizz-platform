package io.fizz.gdpr.application.service;

import io.fizz.common.Utils;
import io.fizz.common.domain.ApplicationId;
import io.fizz.common.domain.DomainErrorException;
import io.fizz.common.domain.QueryRange;
import io.fizz.common.domain.UserId;
import io.fizz.gdpr.application.repository.AbstractGDPRRequestRepository;
import io.fizz.gdpr.domain.GDPRRequest;
import io.fizz.gdpr.domain.GDPRRequestSearchResult;
import io.fizz.gdpr.domain.GDPRRequestStatus;

import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class GDPRService {
    private final AbstractGDPRRequestRepository gdprRequestRepo;

    public GDPRService(final AbstractGDPRRequestRepository aGDPRRequestRepo) {
        Utils.assertRequiredArgument(aGDPRRequestRepo, "invalid gdpr request repository specified");
        gdprRequestRepo = aGDPRRequestRepo;
    }

    public CompletableFuture<String> createGDPRRequest(final String aAppId,
                                                   final String aUserId,
                                                   final boolean aClearMessageData,
                                                   final String aRequestedBy) {
        try {
            final String id = UUID.randomUUID().toString();
            final ApplicationId appId = new ApplicationId(aAppId);
            final UserId userId = new UserId(aUserId);
            final UserId requestedBy = new UserId(aRequestedBy);

            final GDPRRequest request = new GDPRRequest(
                    id,
                    appId,
                    userId,
                    aClearMessageData,
                    requestedBy,
                    GDPRRequestStatus.SCHEDULED,
                    Utils.now(),
                    null,
                    0L);
            return gdprRequestRepo.save(request);
        } catch (Exception ex) {
            return Utils.failedFuture(ex);
        }
    }

    public CompletableFuture<Void> cancelGDPRRequest(final String aAppId,
                                                       final String aRequestId,
                                                       final String aCancelledBy) {
        try {
            final ApplicationId appId = new ApplicationId(aAppId);
            final UserId cancelledBy = new UserId(aCancelledBy);

            CompletableFuture<Void> resultFuture = new CompletableFuture<>();
            gdprRequestRepo.searchGDPRRequests(appId, aRequestId, null, null, null, null, new QueryRange(0L, QueryRange.MAX_TIMESTAMP), 0, 1)
                    .thenAccept(result -> {
                        if (result.gdprRequests().size() == 0) {
                            resultFuture.completeExceptionally(new DomainErrorException("request not found"));
                        }
                        else {
                            GDPRRequest request = result.gdprRequests().get(0);
                            if (request.status() == GDPRRequestStatus.COMPLETED) {
                                resultFuture.completeExceptionally(new IllegalStateException("request already completed"));
                                return;
                            }
                            if (request.status() == GDPRRequestStatus.CANCELLED) {
                                resultFuture.completeExceptionally(new IllegalStateException("request already cancelled"));
                                return;
                            }
                            request.cancel(cancelledBy, GDPRRequestStatus.CANCELLED, Utils.now());
                            gdprRequestRepo.save(request)
                                    .handle((id, ex) -> {
                                        if (Objects.isNull(ex)) {
                                            resultFuture.complete(null);
                                        }
                                        else {
                                            resultFuture.completeExceptionally(ex);
                                        }
                                       return null;
                                    });
                        }
                    });

            return resultFuture;
        }
        catch (Exception ex) {
            return Utils.failedFuture(ex);
        }
    }

    public CompletableFuture<GDPRRequestSearchResult> searchRequests(final String aAppId,
                                                                                  final String aRequestId,
                                                                                  final String aUserId,
                                                                                  final String aRequestedBy,
                                                                                  final String aCancelledBy,
                                                                                  final String aStatus,
                                                                                  final Integer aCursor,
                                                                                  final Integer aPageSize,
                                                                                  final Long aStartTs,
                                                                                  final Long aEndTs) {
        try {
            final ApplicationId appId = new ApplicationId(aAppId);
            final UserId userId = Objects.nonNull(aUserId) ? new UserId(aUserId) : null;
            final UserId requestedBy = Objects.nonNull(aRequestedBy) ? new UserId(aRequestedBy) : null;
            final UserId cancelledBy = Objects.nonNull(aCancelledBy) ? new UserId(aCancelledBy) : null;
            final GDPRRequestStatus status = Objects.nonNull(aStatus) ? GDPRRequestStatus.fromValue(aStatus) : null;
            final QueryRange range = new QueryRange(aStartTs, aEndTs);

            return gdprRequestRepo.searchGDPRRequests(appId, aRequestId, userId, requestedBy, cancelledBy, status, range, aCursor, aPageSize);
        }
        catch (Exception ex) {
            return Utils.failedFuture(ex);
        }
    }
}