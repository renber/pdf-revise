package de.renebergelt.pdfrevise.types;

import com.beust.jcommander.IStringConverter;
import com.beust.jcommander.Parameter;

import java.awt.*;

/**
 * Base class for TaskOptions where the user can specify a font
 */
public abstract class FontOptions implements TaskOptions {

    @Parameter(names = {"--font-size"}, description = "The font size to use")
    public int fontSize = 10;

    @Parameter(names = {"--font"}, description = "The font to use")
    public String fontName = "Helvetica";

    @Parameter(names = {"--font-color"}, description = "The font color to use", converter = ColorConverter.class)
    public Color fontColor = Color.BLACK;

    /**
     * Creates the font with the specified fontName and fontSize
     */
    public Font createFont() {
        return new Font(fontName, Font.PLAIN, fontSize);
    }

    public static class ColorConverter implements IStringConverter<Color> {
        public Color convert(String value) {
            Color c = getColorByName(value);
            if (c != null) return c;

            // decode hex value, e.g. #FFFFFF
            return Color.decode(value);
        }

        public static Color getColorByName(String name) {
            try {
                return (Color)Color.class.getField(name.toUpperCase()).get(null);
            } catch (IllegalArgumentException | IllegalAccessException | NoSuchFieldException | SecurityException e) {
                return null;
            }
        }
    }
}
