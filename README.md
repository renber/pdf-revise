# pdf-revise
Command line tool which allows to edit pdf files. 
Originally, it was developed to create a non-editable draft copy of a pdf by adding watermark text and rendering all pages as images. It quickly has gained more functionality such as page extraction, high quality page rendering to image files and merging pdf files. It provides an easy to extend task model to edit pdfs.

The tool uses the com.itextpdf.itext and org.apache.pdfbox libraries.

## Usage
Call with: `java -jar pdf-revise.jar [input pdf file] [options]`
```
Options:
  -o, --out
    Name of the output file.
    Default: [input pdf file].out.pdf
  --pages
    The pages to which operations should be applied to (e.g. 1,2-5,!3,6-10). 
    All pages are kept in the pdf and only the selected will be modified.
    Default: <empty string>
  --page-watermark
    Add a watermark to the background of every page
    Default: false
  --image-watermark
    Add a watermark over every image
    Default: false
  --watermark-text
    The watermark text
    Default: DRAFT
  --page-watermark-layer
    Where to put the watermark text for pages (background or foreground)
    Default: background
    Possible Values: [foreground, fg, background, bg]
  --render
    Convert pages to images
    Default: false
  --dpi
    The DPI to render the pdf pages with
    Default: 120
  --render-to-folder
    Render pages as images to the given folder
    Default: <empty string>
  --extract
    Extract only the filtered pages from the pdf and copy them to the target
    Default: false
  --append
    Append the given pdf file to the input file
    Default: <empty string>
  --disable-copy-paste
    Disables the Copy/Paste function in pdf readers
    Default: false
```

## Example

Sample output for a pdf for options `--page-watermark --page-watermark-layer foreground --render --dpi 90`:

PDF page, before (left) and after (right) processing:<br>
<img src="doc/images/page_before.png" alt="PDF page before processing" width="40%" height="40%" /> <img src="doc/images/page_after.png" alt="PDF page after processing" width="40%" height="40%" />
<br><br>
Zoomed at 400%, before (left) and after (right) processing:<br>
<img src="doc/images/detail_before.png" alt="Zoomed at 400% before processing" width="40%" height="40%" /> <img src="doc/images/detail_after.png" alt="Zoomed at 400% after processing" width="40%" height="40%" />

## Compiling

The project uses maven, so compiling and creating an executable jar is easy.
Calling `mvn package` will package the tool and all dependencies in a single jar which can be executed.

