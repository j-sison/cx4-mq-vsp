package com.jay.mqconsumer.business.logic.service;

import com.jay.mqconsumer.data.entity.MqMessage;

import java.util.List;

public interface MqMessageService {

    MqMessage createMessage(MqMessage mqMessage);

    List<MqMessage> getMqMessages();
}
