# pdf-revise
Command line tool which allows to edit pdf files. 
Originally, it was developed to create a non-editable draft copy of a pdf by adding watermark text and rendering all pages as images. It quickly has gained more functionality such as page extraction, high quality page rendering to image files and merging pdf files. It provides an easy to extend task model to edit pdfs.

The tool uses the com.itextpdf.itext and org.apache.pdfbox libraries.

## Usage
Call with: `java -jar pdf-revise.jar [input pdf file] [options] [command1] [command1 options] [command2] [command2 options] ...`
```
  Options:
    -o, --out
      Name of the output file.
      Default: [input pdf file].out.pdf
    --help, -?, --?, /?
      Show this help screen
  Commands:
    add-image-watermark      Add a watermark over every image
      Usage: add-image-watermark The watermark text

    add-page-watermark      Add a watermark to every page
      Usage: add-page-watermark [options] The watermark text
        Options:
          --layer
            Where to put the watermark text for pages (background or 
            foreground) 
            Default: background
            Possible Values: [foreground, fg, background, bg]

    add-page-numbers      Adds page numbers
      Usage: add-page-numbers [options]
        Options:
          --font
            The font to use
            Default: Helvetica
          --font-color
            The font color to use
            Default: java.awt.Color[r=0,g=0,b=0]
          --font-size
            The font size to use
            Default: 10.0
          -h, --horizontal
            Horizontal alignment on the page
            Default: center
            Possible Values: [near, center, far]
          --horizontal-margin
            Horizontal margin
            Default: 20.0
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
          --vertical-margin
            Vertical margin
            Default: 20.0

    render-pages      Add a watermark to every page
      Usage: render-pages [options]
        Options:
          --dpi
            The DPI to render the pdf pages with
            Default: 0

    extract      Extract only the filtered pages from the pdf and copy them to 
            the target
      Usage: extract

    append      Appends the specified pdf file
      Usage: append The pdf file to append

    render-to-folder      Render pages as images to the given folder
      Usage: render-to-folder [options] The target folder to render pages to
        Options:
          --dpi
            The DPI to render the pdf pages with
            Default: 0

    disable-copy-paste      Disables the Copy/Paste function in pdf readers
      Usage: disable-copy-paste

    pages      Specifies the pages to which subsequent tasks should be 
            applied 
      Usage: pages The pages to which the subsequent tasks should be applied 
            to (e.g. 1,2-5,!3,6-10). All pages are kept in the pdf and only 
            the selected will be modified.
```

## Example

Sample output for a pdf for options `add-page-watermark --layer=foreground render-pages --dpi 90`:

PDF page, before (left) and after (right) processing:<br>
<img src="doc/images/page_before.png" alt="PDF page before processing" width="40%" height="40%" /> <img src="doc/images/page_after.png" alt="PDF page after processing" width="40%" height="40%" />
<br><br>
Zoomed at 400%, before (left) and after (right) processing:<br>
<img src="doc/images/detail_before.png" alt="Zoomed at 400% before processing" width="40%" height="40%" /> <img src="doc/images/detail_after.png" alt="Zoomed at 400% after processing" width="40%" height="40%" />

## Compiling

The project uses maven, so compiling and creating an executable jar is easy.
Calling `mvn package` will package the tool and all dependencies in a single jar which can be executed.

