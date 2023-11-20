package com.example.kp;

public class MessageOut extends Messages {
    public  MessageOut(String message){
        super(message);    // если базовый класс определяет конструктор
        //  то производный класс должен его вызвать
    }
}
