package com.example.imagetopdf2bot.service;

import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Service
public class UsersImageService {

    Map<Long, List<String>> user = new HashMap<>();

    public boolean containsKey(Long chatId){
        return user.containsKey(chatId);
    }

    public void put(Long chatId, List<String> list){
        user.put(chatId,list);
    }
    public List<String> get(Long chatId){
        return user.get(chatId);
    }

    public void setFileId(Long chatId, String fileId) {
        List<String> strings = user.get(chatId);
        strings.add(fileId);
        user.put(chatId, strings);
    }

}
