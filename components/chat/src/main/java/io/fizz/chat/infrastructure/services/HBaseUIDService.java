package io.fizz.chat.infrastructure.services;

import io.fizz.client.hbase.client.AbstractHBaseClient;
import io.fizz.common.Config;
import io.fizz.chat.infrastructure.ConfigService;

public class HBaseUIDService extends io.fizz.client.hbase.HBaseUIDService {
    private static final Config config = ConfigService.config();
    private static final String HBASE_NAMESPACE = config.getString("chat.hbase.namespace");

    public HBaseUIDService(final AbstractHBaseClient aClient) {
        super(aClient, HBASE_NAMESPACE);
    }
}
