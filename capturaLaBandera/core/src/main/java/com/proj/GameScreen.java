package com.proj;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.utils.Json;
import com.github.czyzby.websocket.WebSocket;
import com.github.czyzby.websocket.WebSocketListener;
import com.github.czyzby.websocket.WebSockets;

public class GameScreen implements Screen {
    private float stateTime = 0;
    private float lastSend = 0;
    private WebSocket socket;
    private String address = "bandera2.ieti.site";
    private int port = 443;
    private Main game;
    public float oldX;
    public float oldY;
    public boolean up;
    public boolean down;
    public boolean left;
    public boolean right;
    public String lado = "";
    public String vertical = "";


    // Constructor
    public GameScreen(Main game) {
        this.game = game;
//            if( Gdx.app.getType()== Application.ApplicationType.Android )
//                // en Android el host és accessible per 10.0.2.2
//                address = "10.0.2.2";
//            String wsUrl = WebSockets.toWebSocketUrl(address, port);
//            System.out.println("Conectando a: " + wsUrl);

        //socket = WebSockets.newSocket(WebSockets.toWebSocketUrl(address, port));
        socket = WebSockets.newSocket(WebSockets.toSecureWebSocketUrl(address, port));
        socket.setSendGracefully(false);
        socket.addListener(new MyWSListener());
        socket.connect();
        socket.send("Enviar dades");


    }



    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
//            stateTime += delta;
//
//            // Solo enviar datos si la conexión está abierta y ha pasado al menos 1s
//            if (socket.isOpen() && stateTime - lastSend > 5.0f) {
//                lastSend = stateTime;
//                socket.send("Enviar dades");
//            }

        if (Gdx.input.justTouched() && socket.isOpen()) {

                Gdx.input.setInputProcessor(new InputAdapter() {
                    @Override
                    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
                        if (Gdx.input.getX()<game.viewport.getScreenWidth()/2){
                            oldX = screenX;
                            oldY = screenY;
                            return true;
                        } else {
                            oldX = 0;
                            oldY = 0;
                        }
                        return false;
                    }

                    @Override
                    public boolean touchDragged(int screenX, int screenY, int pointer) {
                        String jsonMessage;
                        Json json = new Json();
                        if (oldY > 0 || oldX > 0){
                            if (oldY > screenY){

                                if (!up){
                                    up = true;
                                    down = false;
                                    System.out.println("up");
                                    vertical = "UP";
                                    Message mensaje = new Message(vertical,lado,0);
                                    wrapMessage mensFin = new wrapMessage(mensaje);
                                    jsonMessage = json.toJson(mensFin);

                                    System.out.println(jsonMessage);
                                    socket.send(jsonMessage);
                                }
                            } else if (oldY < screenY) {

                                if (!down){
                                    down = true;
                                    up = false;
                                    System.out.println("down");
                                    vertical = "DOWN";
                                    Message mensaje = new Message(vertical,lado,0);
                                    wrapMessage mensFin = new wrapMessage(mensaje);
                                    jsonMessage = json.toJson(mensFin);

                                    System.out.println(jsonMessage);
                                    socket.send(jsonMessage);
                                }
                            } else {
                                if(up || down){
                                    down = false;
                                    up = false;
                                    vertical = "";

                                    Message mensaje = new Message(vertical,lado,0);

                                    wrapMessage mensFin = new wrapMessage(mensaje);
                                    jsonMessage = json.toJson(mensFin);

                                    System.out.println(jsonMessage);
                                    socket.send(jsonMessage);
                                }
                            }
                            if (oldX > screenX){
                                if (!left){
                                    left = true;
                                    right = false;
                                    System.out.println("left");
                                    lado = "LEFT";
                                    Message mensaje = new Message(vertical,lado,0);

                                    wrapMessage mensFin = new wrapMessage(mensaje);
                                    jsonMessage = json.toJson(mensFin);

                                    System.out.println(jsonMessage);
                                    socket.send(jsonMessage);

                                }

                            } else if (oldX < screenX) {
                                if (!right){
                                    right = true;
                                    left = false;
                                    System.out.println("right");
                                    lado = "RIGHT";
                                    Message mensaje = new Message(vertical,lado,0);
                                    wrapMessage mensFin = new wrapMessage(mensaje);
                                    jsonMessage = json.toJson(mensFin);

                                    System.out.println(jsonMessage);
                                    socket.send(jsonMessage);
                                }

                            } else {
                                if (right || left){
                                    right = false;
                                    left = false;
                                    lado = "";
                                    Message mensaje = new Message(vertical,lado,0);
                                    wrapMessage mensFin = new wrapMessage(mensaje);
                                    jsonMessage = json.toJson(mensFin);

                                    System.out.println(jsonMessage);
                                    socket.send(jsonMessage);
                                }
                            }
                            //String message = screenX+","+screenY+","+oldX+","+oldY;
                            //socket.send(vertical+","+lado);
                            return true;
                        }
                        return false;
                    }
                    @Override
                    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
                        String jsonMessage;
                        Json json = new Json();
                        System.out.println("STOP");

                        up = false;
                        down = false;
                        left = false;
                        right = false;
                        Message mensaje = new Message(vertical,lado,1);
                        wrapMessage mensFin = new wrapMessage(mensaje);
                        jsonMessage = json.toJson(mensFin);

                        System.out.println(jsonMessage);
                        socket.send(jsonMessage);
                        return true;
                    }
                });




        }
    }

    @Override
    public void show() {

    }

    @Override
    public void resize(int width, int height) { }

    @Override
    public void pause() { }

    @Override
    public void resume() {

    }

    @Override
    public void hide() {

    }

    @Override
    public void dispose() {
        socket.close();
    }

    // Listener del WebSocket
    private class MyWSListener implements WebSocketListener {
        @Override
        public boolean onOpen(WebSocket webSocket) {
            System.out.println("WebSocket conectado.");
            webSocket.send("Hola desde el cliente!");  // Ahora enviamos el mensaje aquí
            return true;
        }

        @Override
        public boolean onClose(WebSocket webSocket, int closeCode, String reason) {
            System.out.println("WebSocket cerrado.");
            return false;
        }

        @Override
        public boolean onMessage(WebSocket webSocket, String packet) {
            System.out.println("Mensaje recibido: " + packet);
            return false;
        }

        @Override
        public boolean onMessage(WebSocket webSocket, byte[] packet) {
            System.out.println("Mensaje binario recibido.");
            return false;
        }

        @Override
        public boolean onError(WebSocket webSocket, Throwable error) {
            System.err.println("Error en WebSocket: " + error.getMessage());
            return false;
        }
    }
}
