package com.example.imagetopdf2bot.step;

public interface Messages {
    String START = "Fotosuratlarni yuboring (fayllar bilan ishlash hali qo'llab-quvvatlanmaydi) va PDF faylini olish uchun yuborish /getpdf buyurug'ini yuboring\n" +
            "Botdan foydalanish uchun yordam /help";
    String CANCEL = "Oldingi barcha fayllar o'chirildi! Boshlash uchun yangi rasmlarni yuboring...";
    String GETPDF = "Yuklanmoqda...";
    String LOADING = "Fayl jarayonda. Kuting...";
    String HASPHOTO = "PDF faylini olish uchun yuborish /getpdf buyurug'ini yuboring yoki yana rasm uzating";
    String NOFILE = "Siz rasm uzatishingiz kerak";
    String HELP = "Fotosuratni konvertatsiya qilish uchun yuboring. Bot hamma narsani bitta faylga birlashtiradi, pdf-ga o'zgartiradi va natijani yuboradi.";
}
