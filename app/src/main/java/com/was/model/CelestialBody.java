package com.was.model;

/** One celestial object shown by the desktop pet and gallery. */
public class CelestialBody {
    private String id;
    private String name;
    private String commonName;
    private String constellation;
    private String description;
    private String imageResource;
    private String animationResource;
    private String sourceUrl;

    /** Required by Gson when reading the JSON catalog. */
    public CelestialBody() {
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getCommonName() { return commonName; }
    public String getConstellation() { return constellation; }
    public String getDescription() { return description; }
    public String getImageResource() { return imageResource; }
    public String getAnimationResource() { return animationResource; }
    public String getSourceUrl() { return sourceUrl; }

    public String getDisplayName() {
        if (commonName == null || commonName.isBlank()) {
            return name == null ? id : name;
        }
        return (name == null || name.isBlank()) ? commonName : name + " · " + commonName;
    }

    @Override
    public String toString() {
        return getDisplayName();
    }
}
