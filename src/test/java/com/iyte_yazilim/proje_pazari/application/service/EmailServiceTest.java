package com.iyte_yazilim.proje_pazari.application.service;

import com.icegreen.greenmail.util.GreenMail;
import com.icegreen.greenmail.spring.GreenMailBean;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import com.icegreen.greenmail.configuration.GreenMailConfiguration;
import com.icegreen.greenmail.junit5.GreenMailExtension;
import org.junit.jupiter.api.extension.RegisterExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
class EmailServiceTest {

    @Autowired
    private GreenMail greenMail;

    @Test
    void shouldSendWelcomeEmail() {
        // Test email sending
        MimeMessage[] receivedMessages = greenMail.getReceivedMessages();
        assertEquals(1, receivedMessages.length);
    }
}
