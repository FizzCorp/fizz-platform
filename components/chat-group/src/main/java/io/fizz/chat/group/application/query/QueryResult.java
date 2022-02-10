package io.fizz.chat.group.application.query;

import java.util.ArrayList;
import java.util.List;

public class QueryResult<ItemType> {
    private final List<ItemType> items = new ArrayList<>();
    private String nextPageCursor;

    public QueryResult() {
    }

    public boolean hasNext() {
        return nextPageCursor != null;
    }

    public void add(final ItemType aItem) {
        items.add(aItem);
    }

    public void setNextPageCursor(final String aCursor) {
        nextPageCursor = aCursor;
    }

    public List<ItemType> items() {
        return items;
    }

    public String nextPageCursor() {
        return nextPageCursor;
    }
}
