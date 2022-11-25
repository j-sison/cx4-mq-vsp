package com.jay.mqconsumer.business.logic.service.impl;

import com.jay.mqconsumer.business.logic.service.MqMessageService;
import com.jay.mqconsumer.data.entity.MqMessage;
import com.jay.mqconsumer.data.repository.MqMessageRepository;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MqMessageServiceImpl implements MqMessageService {

    private final MqMessageRepository mqMessageRepository;
    //private final MongoTemplate mqMessageRepository;

    public MqMessageServiceImpl(MqMessageRepository mqMessageRepository) {
        super();
        this.mqMessageRepository = mqMessageRepository;
    }

    @Override
    public MqMessage createMessage(MqMessage mqMessage) {
        return mqMessageRepository.save(mqMessage);
    }

    @Override
    public List<MqMessage> getMqMessages() {
        return mqMessageRepository.findAll(Sort.by(Sort.Direction.DESC, "_id"));
    }
}
