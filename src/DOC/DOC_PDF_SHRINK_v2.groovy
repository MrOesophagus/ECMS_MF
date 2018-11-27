/*
	Change Management
	Written by: kcsmbf 03/10/18
	Change Log:	kcsmbf 08/10/18 - Cleaned up rule

A4 size: 595 x 842 points
*/
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.pdmodel.PDPage
import org.apache.pdfbox.pdmodel.PDPageContentStream
import static org.apache.pdfbox.util.Matrix.getScaleInstance

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
    PDPageContentStream cs = new PDPageContentStream(document, page, PDPageContentStream.AppendMode.APPEND, false, true)
    cs.transform getScaleInstance(0.83f, 0.83f)
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
