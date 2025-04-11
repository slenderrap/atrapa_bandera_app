package com.proj;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class Dpad extends Group {
    public interface DPadListener {
        void onDirectionPressed(String direction);
        void onDirectionReleased(String direction);
    }
    private Main game;
    private DPadListener listener;
    private HashMap<String, Image> botones = new HashMap<>();
    private HashMap<String, Texture> normales = new HashMap<>();
    private HashMap<String, Texture> presionadas = new HashMap<>();

    private Set<String> currentDirections = new HashSet<>();

    public Dpad(DPadListener listener, Main game) {
        this.game = game;
        this.listener = listener;
        crearBoton("up", 64, 128);
        crearBoton("down", 64, 0);
        crearBoton("left", 0, 64);
        crearBoton("right", 128, 64);

        addListener(new InputListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                handleTouch(x, y);
                return true;
            }

            @Override
            public void touchDragged(InputEvent event, float x, float y, int pointer) {
                handleTouch(x, y);
            }

            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                for (String dir : currentDirections) {
                    cambiarEstadoBoton(dir, false);
                    if (listener != null) listener.onDirectionReleased(dir);
                }
                currentDirections.clear();
            }
        });
    }

    private void crearBoton(String direction, float x, float y) {
        Texture normal = new Texture(Gdx.files.internal("pad/" + direction + ".png"));
        Texture pressed = new Texture(Gdx.files.internal("pad/" + direction + "Press.png"));

        normales.put(direction, normal);
        presionadas.put(direction, pressed);

        Image image = new Image(new TextureRegionDrawable(new TextureRegion(normal)));
        image.setPosition(x, y);
        image.setSize(256, 256);

        botones.put(direction, image);
        addActor(image);
    }

    private void handleTouch(float x, float y) {

        Set<String> newDirections = new HashSet<>();

        for (String dir : botones.keySet()) {
            Image img = botones.get(dir);
            if (x >= img.getX() && x <= img.getX() + img.getWidth()
                && y >= img.getY() && y <= img.getY() + img.getHeight()) {
                newDirections.add(dir);
            }
        }

        if (!newDirections.equals(currentDirections)) {
            // Soltar las direcciones anteriores
            for (String dir : currentDirections) {
                if (!newDirections.contains(dir)) {
                    cambiarEstadoBoton(dir, false);
                    if (listener != null) listener.onDirectionReleased(dir);
                }
            }

            // Activar las nuevas
            for (String dir : newDirections) {
                if (!currentDirections.contains(dir)) {
                    cambiarEstadoBoton(dir, true);
                    if (listener != null) listener.onDirectionPressed(dir);
                }
            }

            currentDirections = newDirections;

            // Combinar las direcciones y mandar una única dirección compuesta (como "upLeft")
            if (!currentDirections.isEmpty()) {
                String combined = combinarDirecciones(currentDirections);
                if (listener != null) listener.onDirectionPressed(combined);
            }
        }
    }

    private String combinarDirecciones(Set<String> dirs) {
        if (dirs.contains("up") && dirs.contains("left")) return "upLeft";
        if (dirs.contains("up") && dirs.contains("right")) return "upRight";
        if (dirs.contains("down") && dirs.contains("left")) return "downLeft";
        if (dirs.contains("down") && dirs.contains("right")) return "downRight";
        if (dirs.size() == 1) return dirs.iterator().next();
        return ""; // Sin dirección válida
    }

    private void cambiarEstadoBoton(String dir, boolean presionado) {
        Image img = botones.get(dir);
        if (presionado) {
            img.setDrawable(new TextureRegionDrawable(new TextureRegion(presionadas.get(dir))));
        } else {
            img.setDrawable(new TextureRegionDrawable(new TextureRegion(normales.get(dir))));
        }
    }
}
