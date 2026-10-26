package com.was.data;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.was.model.CelestialBody;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/** Stores favorites separately from the read-only application catalog. */
public class FavoritesRepository {
    private static final Type ID_SET_TYPE = new TypeToken<LinkedHashSet<String>>() { }.getType();
    private final Path storageFile;
    private final Gson gson = new Gson();

    public FavoritesRepository() {
        this(Path.of(System.getProperty("user.home"), ".was", "favorites.json"));
    }

    public FavoritesRepository(Path storageFile) {
        this.storageFile = storageFile;
    }

    public boolean isFavorite(String bodyId) {
        return readIds().contains(bodyId);
    }

    /** Adds the ID when absent, removes it when present, and returns the new state. */
    public boolean toggleFavorite(String bodyId) {
        Set<String> ids = readIds();
        boolean isNowFavorite;
        if (ids.add(bodyId)) {
            isNowFavorite = true;
        } else {
            ids.remove(bodyId);
            isNowFavorite = false;
        }
        writeIds(ids);
        return isNowFavorite;
    }

    public void removeFavorite(String bodyId) {
        Set<String> ids = readIds();
        if (ids.remove(bodyId)) {
            writeIds(ids);
        }
    }

    public List<CelestialBody> findFavorites(List<CelestialBody> catalog) {
        Set<String> ids = readIds();
        List<CelestialBody> favorites = new ArrayList<>();
        for (CelestialBody body : catalog) {
            if (ids.contains(body.getId())) {
                favorites.add(body);
            }
        }
        return List.copyOf(favorites);
    }

    private Set<String> readIds() {
        if (!Files.isRegularFile(storageFile)) {
            return new LinkedHashSet<>();
        }
        try {
            String json = Files.readString(storageFile, StandardCharsets.UTF_8);
            Set<String> ids = gson.fromJson(json, ID_SET_TYPE);
            return ids == null ? new LinkedHashSet<>() : new LinkedHashSet<>(ids);
        } catch (IOException exception) {
            throw new IllegalStateException("즐겨찾기 파일을 읽지 못했습니다.", exception);
        }
    }

    private void writeIds(Set<String> ids) {
        try {
            Path parent = storageFile.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            Files.writeString(storageFile, gson.toJson(ids), StandardCharsets.UTF_8);
        } catch (IOException exception) {
            throw new IllegalStateException("즐겨찾기를 저장하지 못했습니다.", exception);
        }
    }
}
