package com.was.gallery;

import com.was.model.CelestialBody;

import java.util.List;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import java.util.concurrent.ThreadLocalRandom;

/** Randomly selects a celestial body on each launch, avoiding the last one shown. */
public class LaunchCelestialService {
    private static final String LAST_BODY_KEY = "last-launched-celestial-id";
    private final List<CelestialBody> catalog;
    private final Preferences preferences = Preferences.userNodeForPackage(LaunchCelestialService.class);

    public LaunchCelestialService(List<CelestialBody> catalog) {
        if (catalog == null || catalog.isEmpty()) {
            throw new IllegalArgumentException("천체 카탈로그가 비어 있습니다.");
        }
        this.catalog = List.copyOf(catalog);
    }

    public CelestialBody getRandomForNextLaunch() {
        String lastBodyId = preferences.get(LAST_BODY_KEY, "");
        List<CelestialBody> candidates = catalog.stream()
                .filter(body -> !body.getId().equals(lastBodyId))
                .toList();
        if (candidates.isEmpty()) {
            candidates = catalog;
        }

        CelestialBody selected = candidates.get(ThreadLocalRandom.current().nextInt(candidates.size()));
        preferences.put(LAST_BODY_KEY, selected.getId());
        try {
            preferences.flush();
        } catch (BackingStoreException exception) {
            throw new IllegalStateException("다음 실행에서 사진을 바꾸기 위한 설정을 저장하지 못했습니다.", exception);
        }
        return selected;
    }
}
