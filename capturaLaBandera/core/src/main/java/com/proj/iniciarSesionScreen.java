package com.proj;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Net;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.google.gson.Gson;

import java.util.HashMap;
import java.util.Map;

public class iniciarSesionScreen implements Screen {
    private final Stage stage;
    private final Skin skin;
    private final Main game;

    public iniciarSesionScreen(Main game) {
        this.game = game;
        stage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(stage);

        skin = new Skin(Gdx.files.internal("uiskin.json"));

        Table table = new Table(skin);
        table.setFillParent(true);
        stage.addActor(table);

        final TextField usernameField = new TextField("", skin);
        final TextField emailField = new TextField("", skin);
        final TextField phoneField = new TextField("", skin);

        final TextButton registerButton = new TextButton("Register", skin);
        final Label messageLabel = new Label("", skin);

        registerButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                Net.HttpRequest request = new Net.HttpRequest(Net.HttpMethods.POST);
                request.setUrl("https://bandera2.ieti.site/api/auth/register");

                Map<String, Object> data = new HashMap<>();
                data.put("nickname", usernameField.getText());
                data.put("email", emailField.getText());
                data.put("phone", phoneField.getText());
                data.put("termsAccepted", true); // Muy importante

                Gson gson = new Gson();
                String json = gson.toJson(data);
                System.out.println("JSON ENVIADO: " + json);
                request.setHeader("Content-Type", "application/json");
                request.setContent(json);

                Gdx.net.sendHttpRequest(request, new Net.HttpResponseListener() {
                    @Override
                    public void handleHttpResponse(Net.HttpResponse httpResponse) {

                        Gdx.app.postRunnable(() -> {
                            System.out.println(httpResponse.getHeaders());
                            int status = httpResponse.getStatus().getStatusCode();
                            String response = httpResponse.getResultAsString();
                            System.out.println("STATUS: " + status);
                            System.out.println("RESPONSE: " + response);

                            if (status == 200) {
                                messageLabel.setText("Registro correcto. Entrando en la sala de espera...");

                                // Guardar solo el email en un archivo
                                String email = emailField.getText();
                                Gdx.files.local("email.txt").writeString(email, false);
                                System.out.println("📧 Email guardado: " + email);

                                game.setScreen(new WaitingRoomScreen(game));
                                dispose();
                            }

                            else {
                                messageLabel.setText("Error al registrar: " + (response.isEmpty() ? "Respuesta vacía del servidor" : response));
                            }
                        });
                    }

                    @Override
                    public void failed(Throwable t) {
                        Gdx.app.postRunnable(() -> messageLabel.setText("Error de conexión: " + t.getMessage()));
                    }

                    @Override
                    public void cancelled() {
                        Gdx.app.postRunnable(() -> messageLabel.setText("Petición cancelada."));
                    }
                });
            }
        });

        table.add("Username").left(); table.row();
        table.add(usernameField).width(300).pad(10); table.row();
        table.add("Email").left(); table.row();
        table.add(emailField).width(300).pad(10); table.row();
        table.add("Phone (optional)").left(); table.row();
        table.add(phoneField).width(300).pad(10); table.row();
        table.add(registerButton).pad(20); table.row();
        table.add(messageLabel).pad(10);
    }

    @Override public void show() {}
    @Override public void render(float delta) { stage.act(delta); stage.draw(); }
    @Override public void resize(int width, int height) { stage.getViewport().update(width, height, true); }
    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}
    @Override public void dispose() { stage.dispose(); skin.dispose(); }
}
