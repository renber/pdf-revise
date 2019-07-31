package de.renebergelt.pdfrevise.types;

import com.itextpdf.awt.DefaultFontMapper;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Font;
import com.itextpdf.text.pdf.BaseFont;

import java.awt.*;
import java.io.File;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Allows to convert a java.awt.Font to an itext FOnt
 */
public class PdfFontFactory {

    public PdfFontFactory() {
        // --
    }

    /**
     * Convert the given java.awt.Font instance to a iText Font object including
     * color information
     *
     * @param font
     * @param color
     * @return
     */
    public static Font convertFont(java.awt.Font font, Color color) {
        Font newFont = null;
        try {
            newFont = PdfFontCache.getInstance().fromAwtFont(font, color);
            if (newFont == null)
            {
                // use itext's default mapper to find a replacement font
                DefaultFontMapper dfm = new DefaultFontMapper();
                BaseFont baseFont = dfm.awtToPdf(font);
                newFont = new Font(baseFont, font.getSize() * 0.75f, font.getStyle(), new BaseColor(color.getRed(), color.getGreen(), color.getBlue()));
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return newFont;
    }

    /**
     * Cache for itext fonts which have been created from awt fonts
     * (all created fonts are marked for embedding)
     */
    private static class PdfFontCache {

        // itext mapper to choose a fallback font for fonts which cannot be embedded
        DefaultFontMapper defaultFontMapper;

        // font manager to get the font file of an awt font (if available)
        Object fontManager;

        HashMap<String, BaseFont> cachedFonts;

        // all directories on this system which may hold font files (e.g. ttf)
        // all entries are ended with a path separator (i.e. / or \) or are an empty string
        List<String> fontDirectories;

        static PdfFontCache _instance = new PdfFontCache();

        private String OS = System.getProperty("os.name").toLowerCase();

        public static PdfFontCache getInstance() {
            return _instance;
        }

        private PdfFontCache() {

            defaultFontMapper = new DefaultFontMapper();
            cachedFonts = new HashMap<>();

            // we access the sun.font.* classes using reflection
            // to keep it compilable with JDKs >= 9 and the release flag
            // however, we produce a "illegal relfective access" warning during runtime
            fontManager = null;

            try {
                Method fmInstance = Class.forName("sun.font.FontManagerFactory").getMethod("getInstance");
                fontManager= fmInstance.invoke(null);
            } catch (Exception e)
            {
                fontManager = null;
                //LogService.error(LogGroup.Export, "Could not find class sun.font.FontManagerFactory. Font files will not be embedded in PDF files.");
            }

            fontDirectories = getFontDirectories();
        }

        /**
         * Return a itext font which represents the given java.awt.Font
         *
         * @param font
         *            the awt font to convert
         * @param color
         *            the color to use
         * @return an itext font which is marked for embedding
         */
        public Font fromAwtFont(java.awt.Font font, java.awt.Color color) {
            BaseFont baseFont = null;

            // has the BaseFont already been created?
            baseFont = cachedFonts.get(font.getFontName());

            // if the font has not yet been created
            // try to create an embedded font
            if (baseFont == null) {
                if (fontManager != null) {
                    try {
                        String fn = getFileNameForFontName(font.getFontName());

                        if (fn != null && !fn.isEmpty()) {
                            // search for the font file in the font directories
                            for(String fontDir: fontDirectories) {
                                String fontFile = fontDir + fn;
                                if (new File(fontFile).exists()) {
                                    // we found the font file -> convert it
                                    // create an itext pdf font using the font file and embed it
                                    baseFont = BaseFont.createFont(fontFile, BaseFont.IDENTITY_H, BaseFont.EMBEDDED);

                                    // cache it
                                    if (baseFont != null) {
                                        cachedFonts.put(font.getFontName(), baseFont);
                                        // we created the basefont and do not need to query the other directories, if any
                                        break;
                                    }
                                }
                            }
                        }
                    } catch (Exception e) {
                        baseFont = null;
                    }
                }
            }

            // choose a fallback font, if we do not have a font by now
            if (baseFont == null) {
                baseFont = defaultFontMapper.awtToPdf(font);
                // log "Could not embed PDF font '" + font.getFontName() + "', using fallback font '" + baseFont.getFamilyFontName()[0][3] + "' instead");
            }

            // font size has to be converted from pixels to points (for 96 dpi)
            return new Font(baseFont, font.getSize() * 0.75f, font.getStyle(), new BaseColor(color.getRed(), color.getGreen(), color.getBlue()));
        }

        private String getFileNameForFontName(String fontName)
        {
            if (fontManager != null) {
                try {
                    Method getFname = fontManager.getClass().getMethod("getFileNameForFontName", String.class);

                    return (String) getFname.invoke(fontManager, fontName);
                } catch (Exception e) {
                    // log "Could not invoke getFileNameForFontName");
                }
            }

            return "";
        }

        private String getPlatformFontPath()
        {
            if (fontManager != null) {
                try {
                    Method getFname = fontManager.getClass().getMethod("getPlatformFontPath", boolean.class);
                    return (String) getFname.invoke(fontManager, false);
                } catch (Exception e) {
                    // log "Could not invoke getPlatformFontPath");
                }
            }

            return "";
        }

        /**
         * Return the common directories where fonts are stored for the current OS
         *
         * @return Default directory paths, not guaranteed to exist
         */
        private List<String> getFontDirectories() {
            ArrayList<String> dirs = new ArrayList<>();

            // try to query the actual directory from the JVM
            // (e.g. if windows is installed on a different drive than C or someone renamed their WINDOWS folder, this will be the correct path)
            if (fontManager != null)
            {
                String fontDir = getPlatformFontPath();
                if (fontDir != null && !fontDir.isEmpty())
                {
                    // 	ensure that the directory ends with the path separator (i.e. \ or /)
                    if (!fontDir.endsWith(File.pathSeparator))
                        fontDir += File.pathSeparator;

                    dirs.add(fontDir);
                }
            }

            if (isWindows())
            {
                // easy on windows, there is only one directory
                dirs.add("C:\\Windows\\Fonts\\");
                dirs.add("C:\\WINNT\\Fonts\\"); // WinNT / Win200
            }
            else
            if (isUnix())
            {
                // see http://superuser.com/questions/137826/location-of-truetype-fonts
                // there is no standard among distributions so these will most likely be the wrong paths
                dirs.add("/usr/share/fonts/truetype/");
                dirs.add("/usr/share/fonts/");
                dirs.add("~/.fonts");
            }
            else
            if (isMac()) {
                // see https://support.apple.com/de-de/HT201722
                dirs.add("/System/Library/Fonts/");
                dirs.add("/Library/Fonts/");
                dirs.add("~/Library/Fonts/");
            }

            // also try the queried font file name itself as a last resort (may be in PATH)
            dirs.add("");

            return dirs;
        }

        // ---
        // get OS according to
        // http://www.mkyong.com/java/how-to-detect-os-in-java-systemgetpropertyosname/
        // ---

        private boolean isWindows() {
            return (OS.indexOf("win") >= 0);
        }

        private boolean isMac() {
            return (OS.indexOf("mac") >= 0);
        }

        private boolean isUnix() {
            return (OS.indexOf("nix") >= 0 ||
                    OS.indexOf("nux") >= 0 || OS.indexOf("aix") > 0);
        }
    }

}
