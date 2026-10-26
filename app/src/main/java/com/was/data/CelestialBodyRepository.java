package com.was.data;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.was.model.CelestialBody;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** Reads the celestial catalog packaged in the application's resources. */
public class CelestialBodyRepository {
    private static final String CATALOG_RESOURCE = "/celestial-bodies.json";
    private static final Type CATALOG_TYPE = new TypeToken<ArrayList<CelestialBody>>() { }.getType();
    private final Gson gson = new Gson();

    public List<CelestialBody> findAll() {
        InputStream stream = CelestialBodyRepository.class.getResourceAsStream(CATALOG_RESOURCE);
        if (stream == null) {
            throw new IllegalStateException("천체 데이터 파일을 찾을 수 없습니다: " + CATALOG_RESOURCE);
        }

        try (Reader reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
            List<CelestialBody> bodies = gson.fromJson(reader, CATALOG_TYPE);
            if (bodies == null || bodies.isEmpty()) {
                throw new IllegalStateException("천체 데이터 파일에 등록된 천체가 없습니다.");
            }

            Set<String> ids = new HashSet<>();
            for (CelestialBody body : bodies) {
                if (body.getId() == null || body.getId().isBlank()) {
                    throw new IllegalStateException("모든 천체에는 고유한 id가 필요합니다.");
                }
                if (!ids.add(body.getId())) {
                    throw new IllegalStateException("중복된 천체 id가 있습니다: " + body.getId());
                }
                if (body.getImageResource() == null || body.getImageResource().isBlank()) {
                    throw new IllegalStateException("천체 사진 경로가 비어 있습니다: " + body.getId());
                }
            }
            return List.copyOf(bodies);
        } catch (Exception exception) {
            if (exception instanceof IllegalStateException illegalStateException) {
                throw illegalStateException;
            }
            throw new IllegalStateException("천체 데이터 파일을 읽지 못했습니다.", exception);
        }
    }

    public CelestialBody findById(String id) {
        return findAll().stream()
                .filter(body -> body.getId().equals(id))
                .findFirst()
                .orElse(null);
    }
}
