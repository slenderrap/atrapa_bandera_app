package com.proj;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.Json;
import com.github.czyzby.websocket.WebSocket;
import com.github.czyzby.websocket.WebSocketListener;
import com.github.czyzby.websocket.WebSockets;
import com.google.gson.Gson;

import java.util.HashMap;

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
    private MapRender mapRenderer;
    public String stop;
    private String userId;
    private GameState currentState;
    private ShapeRenderer shapeRenderer = new ShapeRenderer();
    public String numJugadores;




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
        //socket.send("Enviar dades");


    }



    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        //game.camera.setToOrtho(true);



        //game.batch.setProjectionMatrix(game.camera.combined);
        game.batch.begin();

        mapRenderer.render(game.batch);
        game.batch.setProjectionMatrix(game.uiCamera.combined);
        game.font.setColor(Color.WHITE);
        game.font.getData().setScale(0.3f);
        game.font.draw(game.batch, "Jugadores: " + numJugadores, 2, 48); // Esquina superior izquierda

        game.batch.end();
//        if (currentState != null) {
//            shapeRenderer.setProjectionMatrix(game.camera.combined); // Usa la cámara
//
//            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
//            for (datosJugador p : currentState.players) {
//                if (p.color.equals("red")) shapeRenderer.setColor(Color.RED);
//                else if (p.color.equals("purple")) shapeRenderer.setColor(Color.PURPLE);
//                else shapeRenderer.setColor(Color.WHITE);
//
//                shapeRenderer.rect(p.x, p.y, p.width/2, p.height/2);
//            }
//            shapeRenderer.end();
//        }

//            stateTime += delta;
//
//            // Solo enviar datos si la conexión está abierta y ha pasado al menos 1s
//            if (socket.isOpen() && stateTime - lastSend > 5.0f) {
//                lastSend = stateTime;
//                socket.send("Enviar dades");
//            }


        if (socket.isOpen()) {

            Dpad dPad = new Dpad(new Dpad.DPadListener() {

                public void onDirectionPressed(String direction) {
                    // Envía al servidor el movimiento
                    System.out.println("Moviendo: " + direction);
                    // ejemplo:
                    // websocket.send("move:" + direction);
                    sendMovementMessage("",direction,"");
                }

                public void onDirectionReleased(String direction) {
                    // Envía al servidor que ha soltado
                    System.out.println("Parar: " + direction);
                    // ejemplo:
                    // websocket.send("stop:" + direction);
                    sendMovementMessage("","","none");
                }
                private void sendMovementMessage(String horizontal, String vertical, String stop) {
                    Gson json = new Gson();
                    System.out.println(vertical + " " + horizontal);
                    if (vertical != "" && horizontal != "") {
                        horizontal = horizontal.substring(0, 1).toUpperCase() + horizontal.substring(1).toLowerCase();
                    }
                    HashMap<String, Object> message = new HashMap<>();
                    message.put("type", "direction");
                    if (!stop.equals("")) {
                        message.put("value", stop);
                    } else {
                        message.put("value", vertical + horizontal);
                        //message.put("value", vertical != null ? vertical : "");
                    }
                    System.out.println("JSON enviado: " + message);
                    String jsonMessage = json.toJson(message);
                    System.out.println("JSON enviado: " + jsonMessage);

                    if (socket != null && socket.isOpen()) {
                        socket.send(jsonMessage);
                    } else {
                        System.out.println("Error: La conexión WebSocket no está abierta.");
                    }
                }
            }, game);
            dPad.setPosition(50, 50);
            game.Pad.addActor(dPad);
            game.Pad.draw();

//                Gdx.input.setInputProcessor(new InputAdapter() {
//                    @Override
//                    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
//                        if (Gdx.input.getX()<game.viewport.getScreenWidth()/2){
//                            oldX = screenX;
//                            oldY = screenY;
//                            return true;
//                        } else {
//                            oldX = 0;
//                            oldY = 0;
//                        }
//                        return false;
//                    }
//
//                    @Override
//                    public boolean touchDragged(int screenX, int screenY, int pointer) {
//                        // Verifica si hay cambios significativos en las coordenadas
//
//                        if (oldY > 0 || oldX > 0) {
//                            // Movimiento vertical
//                            if (oldY > screenY && !up) { // Movimiento hacia arriba
//                                up = true;
//                                down = false;
//                                vertical = "up";
//                                stop = "";
//                            } else if (oldY < screenY && !down) { // Movimiento hacia abajo
//                                down = true;
//                                up = false;
//                                vertical = "down";
//                                stop = "";
//                            } else if (up || down) { // Detener movimiento vertical
//
//                                up = false;
//                                down = false;
//                                vertical = "";
//                                if (stop !=""){
//                                    stop = "none";
//                                }
//                            }
//
//                            // Movimiento horizontal
//                            if (oldX > screenX && !left) { // Movimiento hacia la izquierda
//
//                                left = true;
//                                right = false;
//                                lado = "left";
//                                stop = "";
//                            } else if (oldX < screenX && !right) { // Movimiento hacia la derecha
//
//                                right = true;
//                                left = false;
//                                lado = "right";
//                                stop = "";
//                            } else if (right || left) { // Detener movimiento horizontal
//                                right = false;
//                                left = false;
//                                lado = "";
//                                if (stop !=""){
//                                    stop = "none";
//                                }
//                            }
//                            if (!lado.isEmpty() || !vertical.isEmpty()){
//                                sendMovementMessage(lado, vertical, stop);
//                            }
//                            // Actualiza las coordenadas antiguas
//                            oldX = screenX;
//                            oldY = screenY;
//
//                            return true;
//                        }
//                        return false;
//                    }
//
//                    // Método auxiliar para enviar un mensaje de movimiento
//                    private void sendMovementMessage(String horizontal, String vertical, String stop) {
//                        Gson json = new Gson();
//                        System.out.println(vertical+" "+horizontal);
//                        if (vertical != "" && horizontal != ""){
//                           horizontal = horizontal.substring(0,1).toUpperCase()+horizontal.substring(1).toLowerCase();
//                        }
//                        HashMap<String, Object> message = new HashMap<>();
//                        message.put("type", "direction");
//                        if (!stop.equals("")){
//                            message.put("value", stop);
//                        }
//                        else {
//                            message.put("value", vertical+horizontal);
//                            //message.put("value", vertical != null ? vertical : "");
//                        }
//                        System.out.println("JSON enviado: " + message);
//                        String jsonMessage = json.toJson(message);
//                        System.out.println("JSON enviado: " + jsonMessage);
//
//                        if (socket != null && socket.isOpen()) {
//                            socket.send(jsonMessage);
//                        } else {
//                            System.out.println("Error: La conexión WebSocket no está abierta.");
//                        }
//                    }
//                    @Override
//                    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
//
//                        System.out.println("STOP");
//
//                        up = false;
//                        down = false;
//                        left = false;
//                        right = false;
//                        lado="";
//                        vertical="";
//                        stop = "none";
//                        sendMovementMessage(lado, vertical, stop);
//                        return true;
//                    }
//                });




        }
        datosJugador myPlayer = null;
        if (currentState != null && userId != null) {

            for (datosJugador p : currentState.players) {
                if (p.id.equals(userId)) {
                    myPlayer = p;
                    break;
                }
            }
        }
        if (myPlayer != null) {
            // CENTRAMOS LA CÁMARA en el jugador
            game.camera.position.set(
                myPlayer.x + myPlayer.width / 2,
                myPlayer.y + myPlayer.height / 2,
                0
            );
            game.camera.update();


        }

        game.viewport.apply();
        game.batch.setProjectionMatrix(game.camera.combined);
        shapeRenderer.setProjectionMatrix(game.camera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        if (currentState != null) {
            for (datosJugador p : currentState.players) {
                switch (p.race) {
                    case "human":
                        shapeRenderer.setColor(Color.RED);
                        break;
                    case "orc":
                        shapeRenderer.setColor(Color.PURPLE);
                        break;
                    case "vampire":
                        shapeRenderer.setColor(Color.BLUE);
                        break;
                    case "slime":
                        shapeRenderer.setColor(Color.ORANGE);
                        break;
                    default:
                        shapeRenderer.setColor(Color.WHITE);
                        break;
                }
                shapeRenderer.rect(p.x, p.y, p.width, p.height);
            }
        }

        shapeRenderer.end();



    }

    @Override
    public void show() {
        mapRenderer = new MapRender();
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
        mapRenderer.dispose();
        socket.close();
    }

    // Listener del WebSocket
    private class MyWSListener implements WebSocketListener {
        @Override
        public boolean onOpen(WebSocket webSocket) {
            System.out.println("WebSocket conectado.");
            //webSocket.send("Hola desde el cliente!");  // Ahora enviamos el mensaje aquí
            return true;
        }

        @Override
        public boolean onClose(WebSocket webSocket, int closeCode, String reason) {
            System.out.println("WebSocket cerrado.");
            return false;
        }

        @Override
        public boolean onMessage(WebSocket webSocket, String packet) {
            //System.out.println("Mensaje recibido: " + packet);
            Gson gson = new Gson();

            if (packet.contains("\"type\":\"newClient\"")) {
                HashMap<String, String> newClientMsg = gson.fromJson(packet, HashMap.class);
                if (userId == null){
                    userId = newClientMsg.get("id");
                    System.out.println("Mi ID es: " + userId);
                }
            } else if (packet.contains("\"type\":\"update\"")) {
                HashMap data = gson.fromJson(packet, HashMap.class);
                Object gameStateObj = data.get("gameState");
                String gameStateJson = gson.toJson(gameStateObj);
                int tileHeight = mapRenderer.getTileHeight(); // tamaño de tile en píxeles
                int mapaAlturaPx = (int) (mapRenderer.getMapHeight() * tileHeight);
                currentState = gson.fromJson(gameStateJson, GameState.class);
                 // o una constante si no tienes método
                for (datosJugador p : currentState.players) {
                    //System.out.println("Posición Y recibida: " + p.y);
                    p.y = mapaAlturaPx - p.y - p.height;
                    //System.out.println("Posición Y modificada: " + p.y);
                }
                for (Key key: currentState.keys) {
                    mapRenderer.setKeyX(key.x);
                    mapRenderer.setKeyY(key.y);
                    mapRenderer.setKeyWidth(key.width);
                    mapRenderer.setKeyHeight(key.height);

                }

                } else if (packet.contains("\"type\":\"newSize\"")) {
                HashMap data = gson.fromJson(packet, HashMap.class);
                Object playersCount = data.get("size");
                String count = gson.toJson(playersCount);
                System.out.println("Cantidad Jugadores: "+count);
                numJugadores = count;
            }

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
