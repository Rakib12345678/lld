package com.rocky.shorturl.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "counters")
public class CounterSeq {
    @Id
    private String id; // e.g. "url_seq"
    private long seq;

    public CounterSeq() {}
    public CounterSeq(String id, long seq) { this.id = id; this.seq = seq; }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public long getSeq() { return seq; }
    public void setSeq(long seq) { this.seq = seq; }
}