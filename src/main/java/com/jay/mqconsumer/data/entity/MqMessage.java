package com.jay.mqconsumer.data.entity;


import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.MongoId;

import javax.persistence.*;


@Document("mq_message")
public class MqMessage {
    @Transient
    public static final String SEQUENCE_NAME = "mq_msg_id";
    @MongoId
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name="message_id")
    private long id;

    @Column(name="message")
    private String message;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
