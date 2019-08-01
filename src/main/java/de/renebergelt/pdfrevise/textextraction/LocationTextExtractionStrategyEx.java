package de.renebergelt.pdfrevise.textextraction;

import com.itextpdf.text.FontFactory;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.DocumentFont;
import com.itextpdf.text.pdf.parser.LineSegment;
import com.itextpdf.text.pdf.parser.LocationTextExtractionStrategy;
import com.itextpdf.text.pdf.parser.TextRenderInfo;
import com.itextpdf.text.pdf.parser.Vector;

import java.util.ArrayList;
import java.util.List;

/**
 * A text eaxtraction strategy which searches for a given text and returns the position (rectangle)
 * a which it was found in the document
 */
public class LocationTextExtractionStrategyEx extends LocationTextExtractionStrategy
{
    private List<ExtendedTextChunk> m_DocChunks = new ArrayList<ExtendedTextChunk>();
    private List<LocationTextExtractionStrategyEx.LineInfo> m_LinesTextInfo = new ArrayList<LineInfo>();
    private List<SearchResult> m_SearchResultsList = new ArrayList<SearchResult>();
    private String m_SearchText;
    public static final float PDF_PX_TO_MM = 0.3528f;


    public LocationTextExtractionStrategyEx(String sSearchText)
    {
        super();
        this.m_SearchText = sSearchText;
    }

    public List<SearchResult> getSearchResults() {
        return m_SearchResultsList;
    }

    private void searchText()
    {
        for (LineInfo aLineInfo: m_LinesTextInfo)
        {
            int iIndex = aLineInfo.m_Text.indexOf(m_SearchText);
            while (iIndex != -1)
            {
                List<TextRenderInfo> letters = new ArrayList<>();
                letters.addAll(aLineInfo.m_LineCharsList.subList(iIndex, iIndex + m_SearchText.length()));

                SearchResult aSearchResult = new SearchResult(m_SearchText, letters);
                this.m_SearchResultsList.add(aSearchResult);

                // more occurences?
                iIndex = aLineInfo.m_Text.indexOf(m_SearchText, iIndex + m_SearchText.length());
            }
        }
    }

    private void groupChunksbyLine()
    {
        LocationTextExtractionStrategyEx.ExtendedTextChunk textChunk1 = null;
        LocationTextExtractionStrategyEx.LineInfo textInfo = null;
        for (LocationTextExtractionStrategyEx.ExtendedTextChunk textChunk2: this.m_DocChunks)
        {
            if (textChunk1 == null)
            {
                textInfo = new LocationTextExtractionStrategyEx.LineInfo(textChunk2);
                this.m_LinesTextInfo.add(textInfo);
            }
            else if (textChunk2.sameLine(textChunk1))
            {
                textInfo.appendText(textChunk2);
            }
            else
            {
                textInfo = new LocationTextExtractionStrategyEx.LineInfo(textChunk2);
                this.m_LinesTextInfo.add(textInfo);
            }
            textChunk1 = textChunk2;
        }
    }

    @Override
    public String getResultantText()
    {
        groupChunksbyLine();
        searchText();
        //In this case the return value is not useful
        return "";
    }

    @Override
    public void renderText(TextRenderInfo renderInfo)
    {
        LineSegment baseline = renderInfo.getBaseline();
        //Create ExtendedChunk
        ExtendedTextChunk aExtendedChunk = new ExtendedTextChunk(renderInfo.getText(), baseline.getStartPoint(), baseline.getEndPoint(), renderInfo.getSingleSpaceWidth(), new ArrayList(renderInfo.getCharacterRenderInfos()));
        this.m_DocChunks.add(aExtendedChunk);
    }

    public static class ExtendedTextChunk
    {
        public String m_text;
        private Vector m_startLocation;
        private Vector m_endLocation;
        private Vector m_orientationVector;
        private int m_orientationMagnitude;
        private int m_distPerpendicular;
        private float m_charSpaceWidth;
        public List<TextRenderInfo> m_ChunkChars;


        public ExtendedTextChunk(String txt, Vector startLoc, Vector endLoc, float charSpaceWidth,List<TextRenderInfo> chunkChars)
        {
            this.m_text = txt;
            this.m_startLocation = startLoc;
            this.m_endLocation = endLoc;
            this.m_charSpaceWidth = charSpaceWidth;
            this.m_orientationVector = this.m_endLocation.subtract(this.m_startLocation).normalize();
            this.m_orientationMagnitude = (int)(Math.atan2((double)this.m_orientationVector.get(1), (double)this.m_orientationVector.get(0)) * 1000.0);
            this.m_distPerpendicular = (int)this.m_startLocation.subtract(new Vector(0.0f, 0.0f, 1f)).cross(this.m_orientationVector).get(2);
            this.m_ChunkChars = chunkChars;

        }


        public boolean sameLine(LocationTextExtractionStrategyEx.ExtendedTextChunk textChunkToCompare)
        {
            return this.m_orientationMagnitude == textChunkToCompare.m_orientationMagnitude && this.m_distPerpendicular == textChunkToCompare.m_distPerpendicular;
        }


    }

    public static class SearchResult
    {
        String text;
        List<TextRenderInfo> textRenderInfo;

        public List<TextRenderInfo> getTextRenderInfo() {
            return textRenderInfo;
        }

        public float getLeft() {
            // x of first letter
            Vector vTopLeft = textRenderInfo.get(0).getDescentLine().getStartPoint();
            return vTopLeft.get(Vector.I1);
        }

        public float getBottom() {
            // min desc
            float minDesc = Float.MAX_VALUE;
            for(TextRenderInfo letter: textRenderInfo) {
                float desc = letter.getDescentLine().getStartPoint().get(Vector.I2);
                if (desc < minDesc) minDesc = desc;
            }

            return  minDesc;
        }

        public float getBaselineY() {
            // return baseline Y of the first letter
            return textRenderInfo.get(0).getBaseline().getStartPoint().get(Vector.I2);
        }

        public float getWidth() {
            // right x of last char - left x of first char
            return textRenderInfo.get(textRenderInfo.size() - 1).getAscentLine().getEndPoint().get(Vector.I1) - textRenderInfo.get(0).getAscentLine().getStartPoint().get(Vector.I1);
        }

        public float getMaxAscent() {
            float maxAsc = 0;
            for(TextRenderInfo letter: textRenderInfo) {
                float asc = letter.getAscentLine().getStartPoint().get(Vector.I2);
                if (asc > maxAsc) maxAsc = asc;
            }
            return maxAsc;
        }

        public float getHeight() {
            // maximum ascent - minimum descent
            float maxAsc = getMaxAscent();
            float minDesc = getBottom();

            // ascent is relative to the baseline
            return maxAsc + (getBaselineY() - getBottom()) - minDesc;
        }

        /**
         * Get the (approximate) font size
         */
        public float getFontSize() {
            return getHeight() - (getBaselineY() - getBottom());
        }

        public Rectangle getBounds() {
            return new Rectangle(getLeft(), getBottom(), getLeft() + getWidth(), getBottom() + getHeight());
        }

        /**
         * Return a rectangle which extends from the baseline
         */
        public Rectangle getBaselineBounds() {
            return new Rectangle(getLeft(), getBaselineY(), getLeft() + getWidth(), getMaxAscent());
        }

        /**
         * Returns the font used for the first glyph
         */
        public DocumentFont getFont() {
            return textRenderInfo.get(0).getFont();
        }

        public SearchResult(String text, List<TextRenderInfo> letters)
        {
            this.text = text;
            this.textRenderInfo = new ArrayList(letters);
        }
    }

    public static class LineInfo
    {
        public String m_Text;
        public List<TextRenderInfo> m_LineCharsList;

        public LineInfo(LocationTextExtractionStrategyEx.ExtendedTextChunk initialTextChunk)
        {
            this.m_Text = initialTextChunk.m_text;
            this.m_LineCharsList = initialTextChunk.m_ChunkChars;
        }

        public void appendText(LocationTextExtractionStrategyEx.ExtendedTextChunk additionalTextChunk)
        {
            m_LineCharsList.addAll(additionalTextChunk.m_ChunkChars);
            this.m_Text += additionalTextChunk.m_text;
        }
    }
}
