package com.mailservice.fny.common;

import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import org.springframework.stereotype.Component;

@Component
public class IdGenerator {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyMMdd");
    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";

    private final SecureRandom secureRandom = new SecureRandom();

    public String generate(String prefix) {
        StringBuilder randomPart = new StringBuilder(6);
        for (int index = 0; index < 6; index++) {
            int position = secureRandom.nextInt(CHARACTERS.length());
            randomPart.append(CHARACTERS.charAt(position));
        }

        return prefix + "_" + LocalDate.now().format(DATE_FORMATTER) + "_" + randomPart;
    }
}
