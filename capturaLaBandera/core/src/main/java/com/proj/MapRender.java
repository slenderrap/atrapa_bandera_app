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

    // Textura y posición de la llave
    private Texture keyTexture;
    private float keyX, keyY;
    private float keyWidth, keyHeight;

    public MapRender() {
        FileHandle file = Gdx.files.internal("mapa/game_data.json");
        JsonValue base = new JsonReader().parse(file);

        // Obtener el primer nivel
        JsonValue level = base.get("levels").get(0);
        JsonValue layers = level.get("layers");

        // Iterar sobre todas las capas
        for (int i = 0; i < layers.size; i++) {
            JsonValue layer = layers.get(i);

            // Cargar tileset
            String tilesSheetFile = layer.getString("tilesSheetFile");
            Texture tileset = new Texture(Gdx.files.internal("mapa/" + tilesSheetFile));
            tilesets.add(tileset);

            // Dividir tileset en regiones
            int tileWidth = layer.getInt("tilesWidth");
            int tileHeight = layer.getInt("tilesHeight");
            TextureRegion[][] regions = TextureRegion.split(tileset, tileWidth, tileHeight);
            tileRegionsList.add(regions);
            tileWidths.add(tileWidth);
            tileHeights.add(tileHeight);

            // Cargar tileMap
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

            // Guardar dimensiones del mapa
            if (i == 0) {
                mapWidth = width;
                mapHeight = height;
            }
        }

        // Cargar la llave desde "sprites"
        JsonValue sprites = level.get("sprites");
        for (int i = 0; i < sprites.size; i++) {
            JsonValue sprite = sprites.get(i);
            if ("key".equals(sprite.getString("type"))) {
                String imageFile = sprite.getString("imageFile");
                keyTexture = new Texture(Gdx.files.internal("mapa/" + imageFile));
                keyX = sprite.getFloat("x");
                keyY = sprite.getFloat("y");
                keyWidth = sprite.getFloat("width");
                keyHeight = sprite.getFloat("height");
            }
        }
    }

    public void render(SpriteBatch batch) {
        // Dibujar el mapa
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
                    batch.draw(region, x * tileWidth, (mapHeight - y - 1) * tileHeight);
                }
            }
        }

        // Dibujar la llave
        if (keyTexture != null) {
            batch.draw(keyTexture, keyX, keyY, keyWidth, keyHeight);
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
}
