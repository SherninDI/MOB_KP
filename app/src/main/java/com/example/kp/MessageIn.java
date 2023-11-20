package com.example.kp;

public class MessageIn extends Messages {
    public  MessageIn(String message){
        super(message);    // если базовый класс определяет конструктор
        //  то производный класс должен его вызвать
    }
}
