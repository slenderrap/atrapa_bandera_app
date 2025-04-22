package com.proj;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;

import java.util.ArrayList;
import java.util.List;

public class MapRender {
    private List<Texture> tilesets = new ArrayList<>();
    private List<TextureRegion[][]> tileRegionsList = new ArrayList<>();
    private List<int[][]> tileMaps = new ArrayList<>();
    private List<Integer> tileWidths = new ArrayList<>();
    private List<Integer> tileHeights = new ArrayList<>();

    private int mapWidth;
    private int mapHeight;

    private Texture keyTexture;
    private float keyX, keyY;
    private float keyWidth, keyHeight;

    private float keyStateTime = 0;

    public MapRender() {
        FileHandle file = Gdx.files.internal("mapa/game_data.json");
        JsonValue base = new JsonReader().parse(file);

        JsonValue level = base.get("levels").get(0);
        JsonValue layers = level.get("layers");

        for (int i = 0; i < layers.size; i++) {
            JsonValue layer = layers.get(i);

            String tilesSheetFile = layer.getString("tilesSheetFile");
            Texture tileset = new Texture(Gdx.files.internal("mapa/" + tilesSheetFile));
            tilesets.add(tileset);

            int tileWidth = layer.getInt("tilesWidth");
            int tileHeight = layer.getInt("tilesHeight");
            TextureRegion[][] regions = TextureRegion.split(tileset, tileWidth, tileHeight);
            tileRegionsList.add(regions);
            tileWidths.add(tileWidth);
            tileHeights.add(tileHeight);

            JsonValue tileMapJson = layer.get("tileMap");
            int height = tileMapJson.size;
            int width = tileMapJson.get(0).size;
            int[][] tileMap = new int[height][width];

            for (int y = 0; y < height; y++) {
                JsonValue fila = tileMapJson.get(y);
                for (int x = 0; x < width; x++) {
                    tileMap[y][x] = fila.getInt(x);
                }
            }
            tileMaps.add(tileMap);

            if (i == 0) {
                mapWidth = width;
                mapHeight = height;
            }
        }
    }

    public void setKey(float x, float y, float width, float height) {
        if (keyTexture == null) {
            keyTexture = new Texture(Gdx.files.internal("mapa/key.png"));
        }
        this.keyX = x;
        this.keyY = y;
        this.keyWidth = width;
        this.keyHeight = height;
    }

    public void render(SpriteBatch batch) {
        keyStateTime += Gdx.graphics.getDeltaTime();

        for (int layerIndex = 0; layerIndex < tileMaps.size(); layerIndex++) {
            int[][] tileMap = tileMaps.get(layerIndex);
            TextureRegion[][] tileRegions = tileRegionsList.get(layerIndex);
            int tileWidth = tileWidths.get(layerIndex);
            int tileHeight = tileHeights.get(layerIndex);
            int tilesetCols = tilesets.get(layerIndex).getWidth() / tileWidth;

            for (int y = 0; y < mapHeight; y++) {
                for (int x = 0; x < mapWidth; x++) {
                    int tileIndex = tileMap[y][x];

                    if (tileIndex < 0) continue;

                    int row = tileIndex / tilesetCols;
                    int col = tileIndex % tilesetCols;

                    TextureRegion region = tileRegions[row][col];
                    batch.draw(region, x * tileWidth, (mapHeight - 1 - y) * tileHeight);
                }
            }
        }

        if (keyTexture != null) {
            float offsetY = (float)Math.sin(keyStateTime * 2f) * 2f; // Velocidad * amplitud
            batch.draw(keyTexture, keyX, (mapHeight * tileHeights.get(0)) - keyY - keyHeight + offsetY, keyWidth, keyHeight);
        }
    }

    public void dispose() {
        for (Texture tileset : tilesets) {
            tileset.dispose();
        }
        if (keyTexture != null) {
            keyTexture.dispose();
        }
    }

    public float getMapHeight() {
        return this.mapHeight;
    }

    public int getTileHeight() {
        return tileHeights.get(0);
    }
}
