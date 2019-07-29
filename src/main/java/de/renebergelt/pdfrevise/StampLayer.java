package de.renebergelt.pdfrevise;

public enum StampLayer {
    FOREGROUND(false),
    FG(false),
    BACKGROUND(true),
    BG(true);

    private boolean background;

    StampLayer(boolean isBackground) {
        background = isBackground;
    }

    public boolean isBackground() {
        return background;
    }

    @Override
    public String toString() {
        return name().toLowerCase();
    }
}
