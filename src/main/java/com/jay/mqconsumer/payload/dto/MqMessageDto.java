package com.jay.mqconsumer.payload.dto;

import java.io.Serializable;
import java.util.Objects;


public class MqMessageDto {
    private final long id;
    private final String message;

    public MqMessageDto(long id, String message) {
        this.id = id;
        this.message = message;
    }

    public long getId() {
        return id;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MqMessageDto entity = (MqMessageDto) o;
        return Objects.equals(this.id, entity.id) &&
                Objects.equals(this.message, entity.message);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, message);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "(" +
                "id = " + id + ", " +
                "message = " + message + ")";
    }
}
