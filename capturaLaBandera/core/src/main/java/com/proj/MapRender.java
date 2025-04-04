package com.proj;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;

public class MapRender {
    private Texture tileset;
    private TextureRegion[][] tileRegions;
    private int[][] tileMap;

    private int tileWidth = 16;
    private int tileHeight = 16;
    private int tilesetCols;

    private int mapWidth;
    private int mapHeight;

    public MapRender(){
        tileset = new Texture(Gdx.files.internal("mapa/FieldsTileset.png"));
        tileRegions = TextureRegion.split(tileset, tileWidth, tileHeight);
        tilesetCols = tileset.getWidth() / tileWidth;


        FileHandle file = Gdx.files.internal("mapa/game_data.json");
        JsonValue base = new JsonReader().parse(file);
        JsonValue mapa = base.get("levels").get(0).get("layers").get(0).get("tileMap");
        //System.out.println(base.get("levels").get(0));
        mapHeight = mapa.size;
        mapWidth = mapa.get(0).size;
        tileMap = new int[mapHeight][mapWidth];

        for (int i = 0; i < mapHeight; i++) {
            JsonValue fila = mapa.get(i);
            for (int j = 0; j < mapWidth; j++) {
                tileMap[i][j] = fila.getInt(j);
            }
        }
    }
    public void render(SpriteBatch batch) {
        for (int y = 0; y < mapHeight; y++) {
            for (int x = 0; x < mapWidth; x++) {
                int tileIndex = tileMap[y][x];


                if (tileIndex < 0) continue;

                int row = tileIndex / tilesetCols;
                int col = tileIndex % tilesetCols;

                TextureRegion region = tileRegions[row][col];
                batch.draw(region, x * tileWidth, (mapHeight - y - 1) * tileHeight);
            }
        }
    }

    public void dispose(){
        tileset.dispose();
    }


}

