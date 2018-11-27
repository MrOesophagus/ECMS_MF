/*
	Change Management
	Written by: kcsmbf 03/10/18
	Change Log:	kcsmbf 08/10/18 - Cleaned up rule

A4 size: 595 x 842 points
*/
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.pdmodel.PDPage
import org.apache.pdfbox.pdmodel.PDPageContentStream
import org.apache.pdfbox.util.Matrix

final File docPdf
List mimeTypes = ["application/pdf", "application/msword", "application/rtf", "application/vnd.openxmlformats-officedocument.wordprocessingml.document"]

if (mimeTypes.contains(_input.storageMimeType)) {
  docPdf = _input.toPdf(_input.file)
} else {
  addError("Sorry but this document format can not be shrunk (must be PDF or MS Word).")
}

if (docPdf) { // Ensuring nullsafe (for reasons)
  PDDocument document = PDDocument.load(docPdf)
  for (i = 1; i <= document.getNumberOfPages(); i++) {
    PDPage page = document.getPage(i-1)
    //PDPage page = document.getDocumentCatalog().getPages().get(0);
    PDPageContentStream cs = new PDPageContentStream(document, page, PDPageContentStream.AppendMode.PREPEND, false, false)
    cs.transform(Matrix.getScaleInstance(0.83f, 0.83f))
    cs.close()
  }

  // Save & store resized file
  tmpFile = File.createTempFile("tmp", ".pdf")
  document.save(tmpFile)
  document.close()
  _input.store(tmpFile)
  if (!_input.cAADocuments) {
    CAADocument caaDoc = new CAADocument()
    caaDoc.setDocument(_input)
    caaDoc.setCfIsResized(true)
    _input.add(caaDoc)
    caaDoc.saveOrUpdate()
  } else { _input.cAADocuments.cfIsResized = true }
  _input.saveOrUpdate()
  tmpFile.delete()
}

/*  This does page rotation for if I ever need Portrait <-> Landscape (albeit this is 45deg)
import org.apache.pdfbox.pdmodel.common.PDRectangle
import java.awt.Rectangle

Matrix matrix = Matrix.getRotateInstance(Math.toRadians(45), 0, 0);
Matrix matrix = new Matrix()
PDRectangle cropBox = page.getCropBox();
float tx = (cropBox.getLowerLeftX() + cropBox.getUpperRightX()) / 2;
float ty = (cropBox.getLowerLeftY() + cropBox.getUpperRightY()) / 2;

Rectangle rectangle = cropBox.transform(matrix).getBounds();
float scale = Math.min(cropBox.getWidth() / (float)rectangle.getWidth(), cropBox.getHeight() / (float)rectangle.getHeight());

cs.transform(Matrix.getTranslateInstance(tx, ty));
cs.transform(matrix);
cs.transform(Matrix.getTranslateInstance(-tx, -ty));
*/
