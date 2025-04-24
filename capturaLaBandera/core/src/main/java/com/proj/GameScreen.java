package com.proj;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.github.czyzby.websocket.WebSocket;
import com.github.czyzby.websocket.WebSocketListener;
import com.google.gson.Gson;

import java.util.HashMap;
import java.util.List;

public class GameScreen implements Screen {
    private float stateTime = 0;
    private final WebSocket socket;
    private final Main game;
    private final String userId;

    private MapRender mapRenderer;
    private GameState currentState;
    private ShapeRenderer shapeRenderer = new ShapeRenderer();
    public String numJugadores;

    private final int IDLE = -1, UP = 0, DOWN = 1, LEFT = 2, RIGHT = 3;
    private int direccion = IDLE;
    private int lastDirection = DOWN;
    private Texture smallKeyTexture;


    private Dpad dPad;

    private final HashMap<String, Animation<TextureRegion>[]> raceAnimations = new HashMap<>();
    private final HashMap<String, int[]> rowOrderByRace = new HashMap<>();

    private String keyOwnerId = "";

    public GameScreen(Main game, WebSocket socket, String userId) {
        this.game = game;
        this.socket = socket;
        this.userId = userId;
        socket.addListener(new MyWSListener());
    }

    @Override
    public void show() {
        smallKeyTexture = new Texture(Gdx.files.internal("mapa/key.png"));

        mapRenderer = new MapRender();
        Gdx.input.setInputProcessor(game.Pad);
        dPad = new Dpad(new Dpad.DPadListener() {
            @Override
            public void onDirectionPressed(String direction) {
                switch (direction) {
                    case "up": case "upLeft": case "upRight": direccion = UP; break;
                    case "down": case "downLeft": case "downRight": direccion = DOWN; break;
                    case "left":  direccion = LEFT; break;
                    case "right":  direccion = RIGHT; break;
                    default: direccion = IDLE; break;
                }
                sendMovementMessage("", direction, "");
            }

            @Override
            public void onDirectionReleased(String direction) {
                direccion = IDLE;
                sendMovementMessage("", "", "none");
            }

            private void sendMovementMessage(String horizontal, String vertical, String stop) {
                Gson json = new Gson();
                HashMap<String, Object> message = new HashMap<>();
                message.put("type", "direction");
                if (!stop.isEmpty()) {
                    message.put("value", stop);
                } else {
                    if (!vertical.isEmpty() && !horizontal.isEmpty()) {
                        horizontal = horizontal.substring(0, 1).toUpperCase() + horizontal.substring(1).toLowerCase();
                    }
                    message.put("value", vertical + horizontal);
                }
                String jsonMessage = json.toJson(message);
                if (socket != null && socket.isOpen()) {
                    socket.send(jsonMessage);
                }
            }
        });

        dPad.setPosition(50, 50);
        game.Pad.addActor(dPad);

        rowOrderByRace.put("human", new int[]{0, 1, 2, 3});
        rowOrderByRace.put("orc", new int[]{0, 1, 2, 3});
        rowOrderByRace.put("vampire", new int[]{0, 1, 2, 3});
        rowOrderByRace.put("slime", new int[]{0, 1, 2, 3});

        raceAnimations.put("human", loadAnimations("sprites/Sword_Run_full.png", rowOrderByRace.get("human")));
        raceAnimations.put("orc", loadAnimations("sprites/orc_run_full.png", rowOrderByRace.get("orc")));
        raceAnimations.put("vampire", loadAnimations("sprites/Vampires_Run_full.png", rowOrderByRace.get("vampire")));
        raceAnimations.put("slime", loadAnimations("sprites/Slime_Run_full.png", rowOrderByRace.get("slime")));
    }

    private Animation<TextureRegion>[] loadAnimations(String path, int[] rowOrder) {
        Texture spriteSheet = new Texture(Gdx.files.internal(path));
        TextureRegion[][] tmpFrames = TextureRegion.split(spriteSheet, spriteSheet.getWidth() / 8, spriteSheet.getHeight() / 4);
        Animation<TextureRegion>[] animations = new Animation[4];

        for (int i = 0; i < 4; i++) {
            TextureRegion[] row = new TextureRegion[8];
            for (int j = 0; j < 8; j++) {
                row[j] = tmpFrames[rowOrder[i]][j];
            }
            animations[i] = new Animation<>(0.1f, row);
        }
        return animations;
    }

    @Override
    public void render(float delta) {
        stateTime += delta;

        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        game.batch.begin();
        mapRenderer.render(game.batch);
        game.batch.end();

        game.Pad.act(delta);
        game.Pad.draw();

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
            game.camera.position.set(
                myPlayer.x + myPlayer.width / 2,
                myPlayer.y + myPlayer.height / 2,
                0
            );
            game.camera.update();
        }

        game.viewport.apply();
        game.batch.setProjectionMatrix(game.camera.combined);

        if (currentState != null) {
            for (datosJugador p : currentState.players) {
                Animation<TextureRegion>[] animations = raceAnimations.get(p.race);
                if (animations == null) continue;

                boolean estaEnMovimiento = (p.speedX != 0 || p.speedY != 0);
                int direccionJugador = IDLE;
                if (p.speedY > 0) direccionJugador = UP;
                else if (p.speedY < 0) direccionJugador = DOWN;
                else if (p.speedX < 0) direccionJugador = LEFT;
                else if (p.speedX > 0) direccionJugador = RIGHT;

                if (direccionJugador == IDLE) {
                    System.out.println(p.lastRenderDirection);
                    direccionJugador = p.lastRenderDirection;
                }
                else{

                    p.lastRenderDirection = direccionJugador;
                    System.out.println(p.lastRenderDirection);
                }

                TextureRegion currentFrame = animations[direccionJugador].getKeyFrame(
                    estaEnMovimiento ? stateTime : 0,
                    true
                );

                game.batch.begin();
                game.batch.draw(currentFrame, p.x, p.y, p.width, p.height);

                if (currentState.keys != null && !currentState.keys.isEmpty()) {
                    datosLlave key = currentState.keys.get(0);
                    if (key.keyOwnerId != null && key.keyOwnerId.equals(p.id)) {
                        float keyDrawX = p.x + p.width / 2f - 6;
                        float keyDrawY = p.y + p.height + 4;
                        game.batch.draw(smallKeyTexture, keyDrawX, keyDrawY, 12, 24); // más pequeño
                    }
                }
                game.batch.end();

            }
        }
    }

    @Override public void resize(int width, int height) {
        game.viewport.update(width, height, true);
        game.Pad.getViewport().update(width, height, true);
    }

    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}

    @Override
    public void dispose() {
        mapRenderer.dispose();
        socket.close();
        if (smallKeyTexture != null) {
            smallKeyTexture.dispose();
        }

    }

    private class MyWSListener implements WebSocketListener {
        @Override public boolean onOpen(WebSocket webSocket) { return true; }
        @Override public boolean onClose(WebSocket webSocket, int closeCode, String reason) { return false; }

        @Override
        public boolean onMessage(WebSocket webSocket, String packet) {
            Gson gson = new Gson();
            if (packet.contains("\"type\":\"update\"")) {
                HashMap data = gson.fromJson(packet, HashMap.class);
                Object gameStateObj = data.get("gameState");
                String gameStateJson = gson.toJson(gameStateObj);
                int tileHeight = mapRenderer.getTileHeight();
                int mapaAlturaPx = (int) (mapRenderer.getMapHeight() * tileHeight);
                currentState = gson.fromJson(gameStateJson, GameState.class);

                for (datosJugador p : currentState.players) {
                    p.y = mapaAlturaPx - p.y - p.height;
                }
                if (currentState.keys != null && !currentState.keys.isEmpty()) {
                    datosLlave key = currentState.keys.get(0);
                    if (!key.pickedUp) {
                        Gdx.app.postRunnable(() -> mapRenderer.setKey(key.x, key.y, key.width, key.height));
                    } else {
                        Gdx.app.postRunnable(() -> mapRenderer.clearKey());
                    }
                }
            } else if (packet.contains("\"type\":\"gameOver\"")) {
                Gdx.app.postRunnable(() -> {
                    socket.removeListener(this);
                    game.setScreen(new WaitingRoomScreen(game));
                    dispose();
                });
            }
            return false;
        }

        @Override public boolean onMessage(WebSocket webSocket, byte[] packet) { return false; }
        @Override public boolean onError(WebSocket webSocket, Throwable error) { return false; }
    }
}
//toHome
