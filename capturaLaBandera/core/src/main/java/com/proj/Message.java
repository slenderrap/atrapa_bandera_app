package com.proj;

public class Message {
    public String vertical;
    public String horizontal;
    public int stop;




    public Message(String vertical,String horizontal,int stop){
        this.vertical = vertical;
        this.horizontal = horizontal;
        this.stop = stop;


    }
}
class wrapMessage{
    public Message move;

    public wrapMessage(Message move) {
        this.move = move;
    }
}
