/*
	Change Management
	Written by: kcsmbf 30/06/18
	Change Log:	kcsmbf 09/08/18 Added doc5 functionality
*/

import org.apache.pdfbox.multipdf.PDFMergerUtility
import java.io.File

tmpFile = File.createTempFile("tmp", ".pdf")

PDFMergerUtility PDFMerger = new PDFMergerUtility()
PDFMerger.setDestinationFileName(tmpFile.toString())

File doc1Pdf = _doc1.toPdf(_doc1.file)
PDFMerger.addSource(doc1Pdf)

File doc2Pdf = _doc2.toPdf(_doc2.file)
PDFMerger.addSource(doc2Pdf)

if (_doc3) {
  File doc3Pdf = _doc3.toPdf(_doc3.file)
  PDFMerger.addSource(doc3Pdf)
}

if (_doc4) {
  File doc4Pdf = _doc4.toPdf(_doc4.file)
  PDFMerger.addSource(doc4Pdf)
}

if (_doc5) {
  File doc5Pdf = _doc5.toPdf(_doc5.file)
  PDFMerger.addSource(doc5Pdf)
}

PDFMerger.mergeDocuments()
_doc1.store(tmpFile)
_doc1.saveOrUpdate()
tmpFile.delete()




