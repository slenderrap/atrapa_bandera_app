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
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
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

    private final HashMap<String, Float> attackTimes = new HashMap<>();
    private final HashMap<String, Boolean> attackingFlags = new HashMap<>();



    private Dpad dPad;

    private final HashMap<String, Animation<TextureRegion>[]> raceAnimations = new HashMap<>();
    private final HashMap<String, Animation<TextureRegion>[]> atakAnimations = new HashMap<>();
    private final HashMap<String, Animation<TextureRegion>[]> deathAnimations = new HashMap<>();
    private final HashMap<String, Animation<TextureRegion>[]> hurtWalkAnimations = new HashMap<>();
    private final HashMap<String, Animation<TextureRegion>[]> hurtRunAnimations = new HashMap<>();
    private final HashMap<String, Animation<TextureRegion>[]> walkAnimations = new HashMap<>();

    private final HashMap<String, int[]> rowOrderByRace = new HashMap<>();

    private String keyOwnerId = "";
    private Texture textureUp,textureDown;

    private final HashMap<String, Float> damageTimes = new HashMap<>();




    public String attackDirection = "down";

    public GameScreen(Main game, WebSocket socket, String userId) {
        this.game = game;
        this.socket = socket;
        this.userId = userId;
        socket.addListener(new MyWSListener());
    }

    @Override
    public void show() {
        smallKeyTexture = new Texture(Gdx.files.internal("mapa/key.png"));

        textureUp = new Texture(Gdx.files.internal("swingB.png"));
        textureDown = new Texture(Gdx.files.internal("swingW.png"));

        TextureRegionDrawable drawableUp = new TextureRegionDrawable(new TextureRegion(textureUp));
        TextureRegionDrawable drawableDown = new TextureRegionDrawable(new TextureRegion(textureDown));


        ImageButton.ImageButtonStyle buttonStyle = new ImageButton.ImageButtonStyle();
        buttonStyle.imageUp = drawableUp;
        buttonStyle.imageDown = drawableDown;

        ImageButton button = new ImageButton(buttonStyle);

        button.setPosition(game.viewport.getScreenWidth()/1.3f, 50);

        button.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                Gson json = new Gson();
                HashMap<String, Object> message = new HashMap<>();
                message.put("type", "attack");
                message.put("value",attackDirection);
                System.out.println(attackDirection);
                String jsonMessage = json.toJson(message);
                if (socket != null && socket.isOpen()) {
                    socket.send(jsonMessage);
                }
            }
        });

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
                attackDirection = "down";
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
                attackDirection = vertical+horizontal;
                String jsonMessage = json.toJson(message);
                if (socket != null && socket.isOpen()) {
                    socket.send(jsonMessage);
                }
            }
        });

        dPad.setPosition(50, 50);
        game.Pad.addActor(dPad);
        game.Pad.addActor(button);

        rowOrderByRace.put("human", new int[]{0, 1, 2, 3});
        rowOrderByRace.put("orc", new int[]{0, 1, 2, 3});
        rowOrderByRace.put("vampire", new int[]{0, 1, 2, 3});
        rowOrderByRace.put("slime", new int[]{0, 1, 2, 3});

        raceAnimations.put("human", loadAnimations("sprites/Sword_Run_full.png", rowOrderByRace.get("human"),8));
        raceAnimations.put("orc", loadAnimations("sprites/orc_run_full.png", rowOrderByRace.get("orc"),8));
        raceAnimations.put("vampire", loadAnimations("sprites/Vampires_Run_full.png", rowOrderByRace.get("vampire"),8));
        raceAnimations.put("slime", loadAnimations("sprites/Slime_Run_full.png", rowOrderByRace.get("slime"),8));

        atakAnimations.put("human", loadAnimations("sprites/attack/Sword_attack_full.png", rowOrderByRace.get("human"),8));
        atakAnimations.put("orc", loadAnimations("sprites/attack/orc_attack_full.png", rowOrderByRace.get("orc"),8));
        atakAnimations.put("vampire", loadAnimations("sprites/attack/Vampires_Attack_full.png", rowOrderByRace.get("vampire"),12));
        atakAnimations.put("slime", loadAnimations("sprites/attack/Slime_Attack_full.png", rowOrderByRace.get("slime"),9));

        walkAnimations.put("human", loadAnimations("sprites/Sword_Walk_full.png", rowOrderByRace.get("human"), 6));
        walkAnimations.put("orc", loadAnimations("sprites/orc_walk_full.png", rowOrderByRace.get("orc"), 6));
        walkAnimations.put("vampire", loadAnimations("sprites/Vampires_Walk_full.png", rowOrderByRace.get("vampire"), 6));
        walkAnimations.put("slime", loadAnimations("sprites/Slime_Walk_full.png", rowOrderByRace.get("slime"), 8));

        hurtWalkAnimations.put("human", loadAnimations("sprites/hurt/Sword_Walk_Hurt_full.png", rowOrderByRace.get("human"), 6));
        hurtWalkAnimations.put("orc", loadAnimations("sprites/hurt/Orc_Walk_Hurt_full.png", rowOrderByRace.get("orc"), 6));
        hurtWalkAnimations.put("vampire", loadAnimations("sprites/hurt/Vampires_Walk_Hurt_full.png", rowOrderByRace.get("vampire"), 6));
        hurtWalkAnimations.put("slime", loadAnimations("sprites/hurt/Slime_Walk_Hurt_full.png", rowOrderByRace.get("slime"), 6));

        hurtRunAnimations.put("human", loadAnimations("sprites/hurt/Sword_Run_Hurt_full.png", rowOrderByRace.get("human"), 8));
        hurtRunAnimations.put("orc", loadAnimations("sprites/hurt/Orc_Run_Hurt_full.png", rowOrderByRace.get("orc"), 8));
        hurtRunAnimations.put("vampire", loadAnimations("sprites/hurt/Vampires_Run_Hurt_full.png", rowOrderByRace.get("vampire"), 8));
        hurtRunAnimations.put("slime", loadAnimations("sprites/hurt/Slime_Run_Hurt_full.png", rowOrderByRace.get("slime"), 8));

    }

    private Animation<TextureRegion>[] loadAnimations(String path, int[] rowOrder,int larg) {
        Texture spriteSheet = new Texture(Gdx.files.internal(path));
        TextureRegion[][] tmpFrames = TextureRegion.split(spriteSheet, spriteSheet.getWidth() / larg, spriteSheet.getHeight() / 4);
        Animation<TextureRegion>[] animations = new Animation[4];

        for (int i = 0; i < 4; i++) {
            TextureRegion[] row = new TextureRegion[larg];
            for (int j = 0; j < larg; j++) {
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
                boolean estaEnMovimiento = (p.speedX != 0 || p.speedY != 0);
                boolean tieneLlave = false;
                boolean usaWalk = false;

                if (currentState.keys != null && !currentState.keys.isEmpty()) {
                    datosLlave key = currentState.keys.get(0);
                    if (key.keyOwnerId != null && key.keyOwnerId.equals(p.id)) {
                        tieneLlave = true;
                        usaWalk = estaEnMovimiento;
                    }
                }

                int direccionJugador = IDLE;
                if (p.speedY > 0) direccionJugador = UP;
                else if (p.speedY < 0) direccionJugador = DOWN;
                else if (p.speedX < 0) direccionJugador = LEFT;
                else if (p.speedX > 0) direccionJugador = RIGHT;
                if (direccionJugador == IDLE) direccionJugador = 0;

                // Animaciones base
                Animation<TextureRegion>[] baseAnim = usaWalk
                    ? walkAnimations.get(p.race)
                    : raceAnimations.get(p.race);
                Animation<TextureRegion>[] attackAnim = atakAnimations.get(p.race);
                Animation<TextureRegion>[] hurtAnim = null;

                float velocidad = (float) Math.sqrt(p.speedX * p.speedX + p.speedY * p.speedY);
                boolean estaCorriendo = velocidad >= 60f;
                if (p.isDamaged) {
                    hurtAnim = estaCorriendo
                        ? hurtRunAnimations.get(p.race)
                        : hurtWalkAnimations.get(p.race);
                }

                float attackTime = attackTimes.getOrDefault(p.id, 0f);
                float damageTime = damageTimes.getOrDefault(p.id, 0f);
                boolean isAttacking = attackingFlags.getOrDefault(p.id, false);
                TextureRegion currentFrame = null;

                if (p.isDamaged && hurtAnim != null && hurtAnim[direccionJugador] != null) {
                    damageTime += delta;
                    currentFrame = hurtAnim[direccionJugador].getKeyFrame(damageTime, false);
                    if (hurtAnim[direccionJugador].isAnimationFinished(damageTime)) {
                        damageTime = 0f;
                    }
                    attackTimes.put(p.id, 0f);
                    attackingFlags.put(p.id, false);
                    damageTimes.put(p.id, damageTime);

                } else if (p.attacking && attackAnim != null && attackAnim[direccionJugador] != null) {
                    attackTime += delta;
                    currentFrame = attackAnim[direccionJugador].getKeyFrame(attackTime, false);
                    if (attackAnim[direccionJugador].isAnimationFinished(attackTime)) {
                        attackTime = 0f;
                        isAttacking = false;
                    }
                    attackTimes.put(p.id, attackTime);
                    attackingFlags.put(p.id, isAttacking);
                    damageTimes.put(p.id, 0f);

                } else if (baseAnim != null && baseAnim[direccionJugador] != null) {
                    currentFrame = baseAnim[direccionJugador].getKeyFrame(
                        estaEnMovimiento ? stateTime : 0, true);
                    attackTimes.put(p.id, 0f);
                    damageTimes.put(p.id, 0f);
                    attackingFlags.put(p.id, false);
                }

                if (currentFrame != null) {
                    game.batch.begin();
                    game.batch.draw(currentFrame, p.x, p.y, p.width, p.height);

                    if (tieneLlave) {
                        float keyDrawX = p.x + p.width / 2f - 6;
                        float keyDrawY = p.y + p.height + 4;
                        game.batch.draw(smallKeyTexture, keyDrawX, keyDrawY, 12, 24);
                    }
                    game.batch.end();
                }

                // Dibujar barra de vida
                shapeRenderer.setProjectionMatrix(game.camera.combined);
                shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
                shapeRenderer.setColor(Color.DARK_GRAY);
                shapeRenderer.rect(p.x, p.y + p.height, p.width, 5);

                float lifePercent = Math.max(0f, Math.min(p.hp / 100f, 1f));
                shapeRenderer.setColor(new Color(1 - lifePercent, lifePercent, 0, 1));
                shapeRenderer.rect(p.x, p.y + p.height, p.width * lifePercent, 5);
                shapeRenderer.end();
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
            System.out.println("[GameScreen] Mensaje: " + packet);
            if (packet.contains("\"type\":\"update\"")) {
                HashMap data = gson.fromJson(packet, HashMap.class);
                Object gameStateObj = data.get("gameState");
                String gameStateJson = gson.toJson(gameStateObj);
                int tileHeight = mapRenderer.getTileHeight();
                int mapaAlturaPx = (int) (mapRenderer.getMapHeight() * tileHeight);
                currentState = gson.fromJson(gameStateJson, GameState.class);

                for (datosJugador p : currentState.players) {
                    p.y = mapaAlturaPx - p.y - p.height;
                    //System.out.println("Attaking: "+p.attacking +", Damaged: "+p.isDamaged +" , Alive: " +p.alive);

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
