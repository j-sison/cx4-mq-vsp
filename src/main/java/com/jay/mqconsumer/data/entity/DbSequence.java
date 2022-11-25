package com.jay.mqconsumer.data.entity;

import org.springframework.data.mongodb.core.mapping.Document;

import javax.persistence.*;


@Document(collection="db_sequence")
public class DbSequence {
    @Id
    private String id;

    private Long seq;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Long getSeq() {
        return seq;
    }

    public void setSeq(Long seq) {
        this.seq = seq;
    }
}