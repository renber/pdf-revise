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

    @Parameter(names = {"-b", "--bold"}, description = "Use the font's bold style")
    public boolean isBold = false;

    @Parameter(names = {"-i", "--italic"}, description = "Use the font's italic style")
    public boolean isItalic = false;

    @Parameter(names = {"--font-color"}, description = "The font color to use (name or in hex format (e.g. #FFFFFF))", converter = ColorConverter.class)
    public Color fontColor = Color.BLACK;

    /**
     * Creates the font with the specified fontName and fontSize
     */
    public Font createFont() {
        int style = Font.PLAIN;
        if (isBold) style |= Font.BOLD;
        if (isItalic) style |= Font.ITALIC;

        return new Font(fontName, style, fontSize);
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
