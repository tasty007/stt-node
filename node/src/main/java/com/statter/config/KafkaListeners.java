package com.statter.config;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;

public class KafkaListeners {
	private static final Logger logger = Logger.getLogger(KafkaListeners.class);

    private Gson gson = new GsonBuilder().create();
    
    
    
    @KafkaListener(topics = {"tradeList"})
    public void processListMessage(String content) {
    	
    
    }
    
   
}
