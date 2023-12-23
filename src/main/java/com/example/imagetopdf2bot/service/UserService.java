package com.example.imagetopdf2bot.service;

import com.example.imagetopdf2bot.dto.User;
import com.example.imagetopdf2bot.entity.Users;
import com.example.imagetopdf2bot.repository.UserRepository;
import com.example.imagetopdf2bot.step.UserStep;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public void changeStep(Long chatId, UserStep step){
        Users usersByChatId = userRepository.findUsersByChatId(chatId);
        usersByChatId.setStep(step);
        userRepository.save(usersByChatId);
    }
    public UserStep getUserStepByChatId(Long chatId){
        Users usersByChatId = userRepository.findUsersByChatId(chatId);
        return usersByChatId.getStep();
    }
    public Users findUser(Long chatId){
        Users usersByChatId = userRepository.findUsersByChatId(chatId);
        return usersByChatId;
    }

    public void save(User user){
        Users users = Users.builder()
                .chatId(user.getChatId())
                .step(user.getStep())
                .build();
        userRepository.save(users);
    }


}
