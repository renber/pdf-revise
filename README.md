# pdf-revise
Command line tool which allows to edit pdf files. 
Originally, it was developed to create a non-editable draft copy of a pdf by adding watermark text and rendering all pages as images. It quickly has gained more functionality such as page extraction, high quality page rendering to image files and merging pdf files. It provides an easy to extend task model to edit pdfs.

The tool uses the com.itextpdf.itext and org.apache.pdfbox libraries.

## Usage
Call with: `java -jar pdf-revise.jar [input pdf file] [options] [task1] [task1 options] [task2] [task2 options] ... `
```  
Options: defined as --option_name=value
  Tasks are executed in the order they appear on the command line
    -o, --out
      Name of the output file.
      Default: [input pdf file].out.pdf
    --help, -?, --?, /?
      Show this help screen
  Tasks:
    add-image-watermark      Add a watermark over every image
      Usage: add-image-watermark [watermark text]

    add-page-numbers      Adds page numbers
      Usage: add-page-numbers [options]
        Options:
          -b, --bold
            Use the font's bold style
            Default: false
          --font
            The font to use
            Default: Helvetica
          --font-color
            The font color to use (name or in hex format (e.g. #FFFFFF))
            Default: java.awt.Color[r=0,g=0,b=0]
          --font-size
            The font size to use
            Default: 10
          -h, --horizontal
            Horizontal alignment on the page
            Default: center
            Possible Values: [near, center, far]
          --horizontal-margin, --h-margin
            Horizontal margin
            Default: 20.0
          -i, --italic
            Use the font's italic style
            Default: false
          --start-at
            Numbering starts with this number
            Default: 1
          --style
            The style of the page numbers
            Default: arabic
            Possible Values: [arabic, roman]
          -v, --vertical
            Vertical alignment on the page
            Default: far
            Possible Values: [near, center, far]
          --vertical-margin, --v-margin
            Vertical margin
            Default: 20.0

    add-page-watermark      Add a watermark to every page
      Usage: add-page-watermark [options] [watermark text]
        Options:
          --layer
            Where to put the watermark text
            Default: background
            Possible Values: [foreground, fg, background, bg]

    append      Appends the specified pdf file
      Usage: append [file to append]

    blacken      Blackens the specified term in the pdf (original term is 
            completely removed)
      Usage: blacken [options] [text to be blackened]
        Options:
          -color
            The color to use for blackening (name or in hex format (e.g. 
            #FFFFFF)) 
            Default: java.awt.Color[r=0,g=0,b=0]

    disable-copy-paste      Disables the Copy/Paste function in pdf readers
      Usage: disable-copy-paste

    extract      Extract only the filtered pages from the pdf and copy them to 
            the target
      Usage: extract

    pages      Specifies the pages to which subsequent tasks should be 
            applied. 
      Usage: pages [page sequence (e.g. 1,2-5,!3,6-10)]

    render-pages      Replace page contents by a rendered image of the page
      Usage: render-pages [options]
        Options:
          --dpi
            The DPI to render the pdf pages with
            Default: 150

    render-to-folder      Render pages as images to the given folder (in png 
            format) 
      Usage: render-to-folder [options] [target folder]
        Options:
          --dpi
            The DPI to render the pdf pages with
            Default: 150

    replace-text      Replaces text in a pdf (experimental!)
      Usage: replace-text [options] [text to be replaced]
        Options:
          --with
            The text to insert
            Default: <empty string>

    reverse-pages      Reverses the order of pages
      Usage: reverse-pages

    supersede-text      Replaces text in a pdf by removing the old occurrence 
            and adding new text instead (experimental!)
      Usage: supersede-text [options] [text to be replaced]
        Options:
          -b, --bold
            Use the font's bold style
            Default: false
          --font
            The font to use
            Default: Helvetica
          --font-color
            The font color to use (name or in hex format (e.g. #FFFFFF))
            Default: java.awt.Color[r=0,g=0,b=0]
          --font-size
            The font size to use
            Default: 10
          -i, --italic
            Use the font's italic style
            Default: false
          --reuse-font
            reuse the font used in the pdf for the matched word. depending on 
            the pdf output might not be as exptected (e.g. missing letters)
            Default: false
          --with
            text to insert
            Default: <empty string>
```

## Example

Sample output for a pdf for options `add-page-watermark --layer=foreground render-pages --dpi=90`:

PDF page, before (left) and after (right) processing:<br>
<img src="doc/images/page_before.png" alt="PDF page before processing" width="40%" height="40%" /> <img src="doc/images/page_after.png" alt="PDF page after processing" width="40%" height="40%" />
<br><br>
Zoomed at 400%, before (left) and after (right) processing:<br>
<img src="doc/images/detail_before.png" alt="Zoomed at 400% before processing" width="40%" height="40%" /> <img src="doc/images/detail_after.png" alt="Zoomed at 400% after processing" width="40%" height="40%" />

## Compiling

The project uses maven, so compiling and creating an executable jar is easy.
Calling `mvn package` will package the tool and all dependencies in a single jar which can be executed.

## Planned features

The following features are planned to be included in the future:
* form filling
* adding images
* extracting images
* adding primitive elements (rectangles, lines ...)
* adding arbitrary text
* rotating pages
* editing meta data

