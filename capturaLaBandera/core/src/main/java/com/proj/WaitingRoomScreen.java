package com.proj;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.github.czyzby.websocket.WebSocket;
import com.github.czyzby.websocket.WebSocketListener;
import com.github.czyzby.websocket.WebSockets;
import com.google.gson.Gson;

import java.util.HashMap;

public class WaitingRoomScreen implements Screen {
    private final Main game;
    private Stage stage;
    private Label playerCountLabel;
    private Label countdownLabel;
    private WebSocket socket;
    private String address = "bandera2.ieti.site";
    private int port = 443;
    private String userId;

    public WaitingRoomScreen(Main game) {
        this.game = game;
    }

    @Override
    public void show() {
        stage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(stage);

        Skin skin = new Skin(Gdx.files.internal("uiskin.json"));

        playerCountLabel = new Label("Jugadores conectados: 0", skin);
        countdownLabel = new Label("Esperando...", skin);

        playerCountLabel.setFontScale(2f);
        countdownLabel.setFontScale(2f);

        Table table = new Table();
        table.setFillParent(true);
        table.center();
        table.add(playerCountLabel).pad(20).row();
        table.add(countdownLabel).pad(20);
        float worldWidth = game.viewport.getWorldWidth();
        float worldHeight = game.viewport.getWorldHeight();
        game.batch.begin();
        Texture backwround = new Texture("mainMenu.png");

        game.batch.draw(backwround, 0, 0, worldWidth, worldHeight);
        //game.batch.draw(game.backwround,0,0,game.viewport.getScreenWidth(),game.viewport.getScreenHeight());

//        game.font.draw(game.batch, "Welcome To The Websockets App!!! ", 2.6f, 3f);
//        game.font.draw(game.batch, "Tap anywhere to send the position to the server", 2.2f, 2.5f);
        game.batch.end();
        stage.addActor(table);

        // Conexión WebSocket
        socket = WebSockets.newSocket(WebSockets.toSecureWebSocketUrl(address, port));
        socket.addListener(new WebSocketListener() {
            @Override
            public boolean onOpen(WebSocket webSocket) {
                System.out.println("Conectado al servidor");
                return true;
            }

            @Override
            public boolean onClose(WebSocket webSocket, int closeCode, String reason) {
                System.out.println("WebSocket cerrado");
                return false;
            }

            @Override
            public boolean onMessage(WebSocket webSocket, String packet) {
                System.out.println("Mensaje: " + packet);
                Gson gson = new Gson();
                if (packet.contains("\"type\":\"newSize\"")) {
                    HashMap data = gson.fromJson(packet, HashMap.class);
                    String size = gson.toJson(data.get("size"));
                    Gdx.app.postRunnable(() -> playerCountLabel.setText("Jugadores conectados: " + size));
                } else if (packet.contains("\"type\":\"countdown\"")) {
                    HashMap data = gson.fromJson(packet, HashMap.class);
                    Object time = data.get("timeleft");
                    Gdx.app.postRunnable(() -> countdownLabel.setText("La partida comienza en: " + time));
                } else if (packet.contains("\"type\":\"gameStart\"")) {
                    Gdx.app.postRunnable(() -> {
                        game.setScreen(new GameScreen(game, socket, userId));

                        dispose();
                    });
                } else if (packet.contains("\"type\":\"newClient\"")) {
                    HashMap<String, String> newClientMsg = gson.fromJson(packet, HashMap.class);
                    if (userId == null) {
                        userId = newClientMsg.get("id");
                        System.out.println("Mi ID en WaitingRoom es: " + userId);
                    }
                } else if (packet.contains("\"type\":\"update\"")) {
                    Gdx.app.postRunnable(() -> countdownLabel.setText("La partida esta en marcha"));
                }
                return false;
            }

            @Override
            public boolean onMessage(WebSocket webSocket, byte[] packet) { return false; }

            @Override
            public boolean onError(WebSocket webSocket, Throwable error) {
                System.err.println("[WaitingRoom] Error WebSocket: " + error.getMessage());
                return false;
            }
        });
        socket.connect();
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        stage.act(delta);
        stage.draw();
    }

    @Override public void resize(int width, int height) { stage.getViewport().update(width, height, true); }
    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}

    @Override
    public void dispose() {
        stage.dispose();

    }
}
