package com.findora.findora.common.email;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class EmailSender {

    private final JavaMailSender mailSender;

    public void send(String to, String subject, String content) {
         try {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject(subject);
        message.setText(content);
        mailSender.send(message);
        System.out.println("이메일 발송 성공: " + to);
    } catch (Exception e) {
        System.out.println("이메일 발송 실패: " + e.getMessage());
        e.printStackTrace(); // 콘솔에 전체 에러 출력
        throw e; // 예외를 다시 던져서 서버 콘솔에 반드시 찍히게!
    }
    } 
}