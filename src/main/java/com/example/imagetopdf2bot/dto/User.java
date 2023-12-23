package com.example.imagetopdf2bot.dto;
import com.example.imagetopdf2bot.step.UserStep;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class User {
    private Long chatId;
    private UserStep step;
}
