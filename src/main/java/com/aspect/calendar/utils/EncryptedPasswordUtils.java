package com.aspect.calendar.utils;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;


public class EncryptedPasswordUtils {
    // Encrypt Password with BCryptPasswordEncoder
    public static String encryptPassword(String password) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        return encoder.encode(password);
    }

    public static String genPassword(int length){
        int leftLimit = 48; // numeral '0'
        int rightLimit = 122; // letter 'z'
        Random random = new Random();

        return random.ints(leftLimit, rightLimit + 1)
                .filter(i -> (i <= 57 || i >= 65) && (i <= 90 || i >= 97))
                .limit(length)
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                .toString();
    }

    public static void main(String[] args) {
        String password = genPassword(10);
        String encryptedPassword = encryptPassword(password);
        System.out.println("Password : " + password + ", encrypted password: " + encryptedPassword);

    }
}
