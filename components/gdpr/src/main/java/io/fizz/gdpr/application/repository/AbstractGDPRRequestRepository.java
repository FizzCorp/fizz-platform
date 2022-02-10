package io.fizz.gdpr.application.repository;

import io.fizz.common.domain.ApplicationId;
import io.fizz.common.domain.QueryRange;
import io.fizz.common.domain.UserId;
import io.fizz.gdpr.domain.GDPRRequest;
import io.fizz.gdpr.domain.GDPRRequestSearchResult;
import io.fizz.gdpr.domain.GDPRRequestStatus;

import java.util.concurrent.CompletableFuture;

public interface AbstractGDPRRequestRepository {
    CompletableFuture<String> save(final GDPRRequest aRequest);
    CompletableFuture<GDPRRequestSearchResult> searchGDPRRequests(final ApplicationId aAppId,
                                                              final String aRequestId,
                                                              final UserId aUserId,
                                                              final UserId aRequestedBy,
                                                              final UserId aCancelledBy,
                                                              final GDPRRequestStatus aStatus,
                                                              final QueryRange aRange,
                                                              final Integer aCursor,
                                                              final Integer aPageSize);
}
