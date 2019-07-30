package de.renebergelt.pdfrevise.types;

import com.beust.jcommander.Parameter;

import java.awt.*;

/**
 * Base class for TaskOptions where the user can specify a font
 */
public abstract class FontOptions implements TaskOptions {

    @Parameter(names = {"--font-size"}, description = "The font size to use")
    public float fontSize = 10;

    @Parameter(names = {"--font"}, description = "The font to use")
    public String fontName = "Helvetica";

    @Parameter(names = {"--font-color"}, description = "The font color to use")
    public Color fontColor = Color.BLACK;
}
