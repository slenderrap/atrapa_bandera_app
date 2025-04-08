package com.proj;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.ScreenUtils;

/** First screen of the application. Displayed after the application is created. */
public class FirstScreen implements Screen {
    final Main game;

    public FirstScreen(final Main game) {
        this.game = game;
    }

    @Override
    public void show() {
        // Prepare your screen here.
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(Color.BLACK);

        game.viewport.apply();
        game.batch.setProjectionMatrix(game.viewport.getCamera().combined);

        //Skin skin = new Skin(Gdx.files.internal("uiskin.json"));

        Texture buttonUp = new Texture("start.png");
        BitmapFont font = new BitmapFont();

        TextButton.TextButtonStyle style = new TextButton.TextButtonStyle();

        style.up = new TextureRegionDrawable(new TextureRegion(buttonUp));
        style.font = font;
        style.font.getData().setScale(5f);
        TextButton button = new TextButton("START",style);

        float worldWidth = game.viewport.getWorldWidth();
        float worldHeight = game.viewport.getWorldHeight();
        button.setPosition(game.viewport.getScreenWidth()/2,game.viewport.getScreenHeight()/6);
        button.setSize(game.viewport.getScreenWidth()/3,game.viewport.getScreenHeight()/3 );
        game.batch.begin();
        Texture backwround = new Texture("mainMenu.png");



        game.batch.draw(backwround, 0, 0, worldWidth, worldHeight);
        //game.batch.draw(game.backwround,0,0,game.viewport.getScreenWidth(),game.viewport.getScreenHeight());

//        game.font.draw(game.batch, "Welcome To The Websockets App!!! ", 2.6f, 3f);
//        game.font.draw(game.batch, "Tap anywhere to send the position to the server", 2.2f, 2.5f);
        game.batch.end();
        game.stage.addActor(button);
        game.stage.act(Gdx.graphics.getDeltaTime());
        game.stage.draw();
        if (Gdx.input.isTouched() || Gdx.input.isKeyJustPressed(Input.Keys.ANY_KEY)) {
            game.setScreen(new GameScreen(game));
            dispose();
        }
    }

    @Override
    public void resize(int width, int height) {
        game.viewport.update(width, height, true);
    }


    @Override
    public void pause() {
        // Invoked when your application is paused.
    }

    @Override
    public void resume() {
        // Invoked when your application is resumed after pause.
    }

    @Override
    public void hide() {
        // This method is called when another screen replaces this one.
    }

    @Override
    public void dispose() {
        // Destroy screen's assets here.
    }
}
