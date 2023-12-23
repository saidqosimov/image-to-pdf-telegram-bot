package com.example.imagetopdf2bot.controller;

import com.example.imagetopdf2bot.step.Messages;
import com.example.imagetopdf2bot.config.BotConfig;
import com.example.imagetopdf2bot.dto.User;
import com.example.imagetopdf2bot.service.UserService;
import com.example.imagetopdf2bot.service.UsersImageService;
import com.example.imagetopdf2bot.step.UserStep;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Image;
import lombok.SneakyThrows;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.ForwardMessage;
import org.telegram.telegrambots.meta.api.methods.GetFile;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.File;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.PhotoSize;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Component
public class Main extends TelegramLongPollingBot {

    private final BotConfig botConfig;
    private final UsersImageService usersImageService;
    private final UserService userService;


    public Main(BotConfig botConfig, UsersImageService usersImageService, UserService userService) {
        this.botConfig = botConfig;
        this.usersImageService = usersImageService;
        this.userService = userService;
    }


    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage()) {
            if (update.getMessage().hasText()) {
                SendMessage sendMessage = new SendMessage();
                Long chadId = update.getMessage().getChatId();
                sendMessage.setChatId(chadId);
                String text = update.getMessage().getText();
                switch (text) {
                    case "/start":
                        usersImageService.put(chadId, new ArrayList<>());
                        if (userService.findUser(chadId) == null) {
                            userService.save(User.builder()
                                    .chatId(chadId)
                                    .step(UserStep.DEFAULT)
                                    .build());
                        }
                        sendMessage.setText(Messages.START);
                        sendText(sendMessage);
                        break;
                    case "/cancel":
                        usersImageService.put(chadId, new ArrayList<>());
                        userService.changeStep(chadId, UserStep.DEFAULT);
                        sendMessage.setText(Messages.CANCEL);
                        sendText(sendMessage);
                        break;
                    case "/help":
                        sendMessage.setText(Messages.HELP);
                        sendText(sendMessage);
                        break;
                    case "/getpdf":
                        if (userService.getUserStepByChatId(chadId) == UserStep.ACTIVE) {
                            sendMessage.setText(Messages.GETPDF);
                            sendText(sendMessage);
                            userService.changeStep(chadId, UserStep.LOADING);
                            getPdf(chadId);
                        } else if (userService.getUserStepByChatId(chadId) == UserStep.LOADING) {
                            sendText(SendMessage.builder()
                                    .chatId(chadId)
                                    .text(Messages.LOADING)
                                    .build());
                            sendText(sendMessage);
                        } else {
                            sendMessage.setText(Messages.NOFILE);
                            sendText(sendMessage);
                        }
                        break;
                }
            } else if (update.getMessage().hasPhoto()) {
                Long chadId = update.getMessage().getChatId();
                List<PhotoSize> photoList = update.getMessage().getPhoto();
                String fileId = null;
                for (PhotoSize photo : photoList) {
                    fileId = photo.getFileId();
                }
                if (userService.getUserStepByChatId(chadId) == UserStep.LOADING) {
                    sendText(SendMessage.builder()
                            .chatId(chadId)
                            .text(Messages.LOADING)
                            .build());
                } else {
                    if (usersImageService.containsKey(chadId)) {
                        userService.changeStep(chadId, UserStep.ACTIVE);
                        usersImageService.setFileId(chadId, fileId);
                        sendText(SendMessage.builder()
                                .chatId(chadId)
                                .text(Messages.HASPHOTO)
                                .build());
                    } else {
                        usersImageService.put(chadId, new ArrayList<>());
                        userService.changeStep(chadId, UserStep.ACTIVE);
                        usersImageService.setFileId(chadId, fileId);
                        sendText(SendMessage.builder()
                                .chatId(chadId)
                                .text(Messages.HASPHOTO)
                                .build());
                    }
                }
            }
        }
    }

    @SneakyThrows
    private synchronized void sendText(SendMessage sendMessage) {
        execute(sendMessage);
    }

    private synchronized void getPdf(Long chatId) {
        List<String> strings = usersImageService.get(chatId);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PdfWriter writer = new PdfWriter(outputStream);
        PdfDocument pdf = new PdfDocument(writer);
        Document document = new Document(pdf);

        for (String fileId : strings) {
            byte[] imageBytes = downloadImage(fileId);
            Image img = new Image(ImageDataFactory.create(imageBytes));
            pdf.setDefaultPageSize(new PageSize(img.getImageWidth(), img.getImageHeight()));
            document.add(img);
        }
        document.close();
        usersImageService.put(chatId, new ArrayList<String>());

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
        ByteArrayInputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());
        InputFile inputFile = new InputFile(inputStream, dateFormat.format(new Date()) + ".pdf");
        sendPdf(chatId, inputFile);
    }

    private synchronized byte[] downloadImage(String fileId) {
        // Rasmlarni Telegram dan olish uchun API so'rovini yaratish
        GetFile getFileRequest = new GetFile();
        getFileRequest.setFileId(fileId);
        try {
            // API orqali rasmlarni olish
            File file = execute(getFileRequest);
            String filePath = file.getFilePath();
            URL fileUrl = new URL("https://api.telegram.org/file/bot" + getBotToken() + "/" + filePath);
            try (InputStream in = fileUrl.openStream()) {
                // Rasmni byte massiviga o'qish
                return in.readAllBytes();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @SneakyThrows
    private synchronized void sendPdf(Long chatId, InputFile inputFile) {
        SendDocument sendDocument = new SendDocument();
        sendDocument.setDocument(inputFile);
        sendDocument.setChatId(chatId);
        execute(sendDocument);
        userService.changeStep(chatId, UserStep.DEFAULT);

    }


    @Override
    public String getBotUsername() {
        return botConfig.getUsername();
    }

    @Override
    public String getBotToken() {
        return botConfig.getToken();
    }

}
