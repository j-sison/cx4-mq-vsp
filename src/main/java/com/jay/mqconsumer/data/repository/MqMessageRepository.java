package com.jay.mqconsumer.data.repository;


import com.jay.mqconsumer.data.entity.MqMessage;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MqMessageRepository extends MongoRepository<MqMessage, Long> {

}
