package com.niceprice.sd4ktv;

public final class Channel {
    public final int streamId;
    public final String name;
    public final String categoryId;

    public Channel(int streamId, String name, String categoryId) {
        this.streamId = streamId;
        this.name = name;
        this.categoryId = categoryId;
    }

    @Override public String toString() {
        return name;
    }
}
