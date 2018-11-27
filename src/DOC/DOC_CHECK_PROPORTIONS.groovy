/*
Change Management
	Written by: kcsmbf 22/06/18
	Change Log: kcsmbf 05/10/18 Expanded functionality for Oversize & Landscape checks
				kcsmbf 09/10/18 Reworked to use it as an entity rule
				kcsmbf 24/10/18 Added Security/Encryption remover
*/

import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.pdmodel.PDPage
import org.apache.pdfbox.pdmodel.common.PDRectangle
String sysErrorsTo = SystemProperty.getValue("mail.errors.sysadmin.emailTo")

final File docPdf

//if (["application/pdf"].contains(_input.storageMimeType)) {
if (_input.stored && _input.storageMimeType.equalsIgnoreCase("application/pdf")) {
  docPdf = _input.file
}

if (docPdf) {  // Ensuring nullsafe for reasons
  boolean isLandscape = false
  boolean isResized = false
  boolean isOverSized = false
  boolean wasEncrypted = false
  String serverEnv = SystemProperty.getValue("general.server.environmentType")

  // Allowing for (totally arbitrary) 3% size variation
  float w97 = 595 * 0.97
  float h97 = 842 * 0.97
  float w103 = 595 * 1.03
  float h103 = 842 * 1.03

  PDDocument source = PDDocument.load(docPdf)
  if (source.isEncrypted()) { // Removing any file encryption
    source.setAllSecurityToBeRemoved(true)
    wasEncrypted = true // setting flag for save check later on
  }

  for (i = 1; i <= source.getNumberOfPages(); i++) {
    pageIsLandscape = false
    PDPage page = source.getPage(i-1)
    height = page.getMediaBox().getHeight()
    width = page.getMediaBox().getWidth()
    if (width > height) {
      pageIsLandscape = true
      isLandscape = (isLandscape) ?: true
    }
    if ((height < h97 || width < w97) && height > width) { // Checks page is undersized & Portrait
      try {
        page.setMediaBox(PDRectangle.A4)
        page.setCropBox(PDRectangle.A4)
        if (!isResized) { isResized = true }
      } catch (Exception e) {
        failedBody = (!serverEnv.equalsIgnoreCase("prod")) ? serverEnv : ""
        failedBody += "\r\n\r\n I failed on " + _input.fullName + " (id: " + _input.id + ") on case: " + _input.case.caseNumber + "\r\n\r\n" + e.toString()
        runRule("SEND_EMAIL", ["emailAddress": sysErrorsTo, "subject": "Document Check Proportions Failure", "body": failedBody])
      }
    }
    if (pageIsLandscape) {
      if (!isOverSized && (width > h103 || height > w103)) { isOverSized = true }
    } else {
      if (!isOverSized && (width > w103 || height > h103)) { isOverSized = true }
    }
  }

  if (!_input.cAADocuments) {
    CAADocument caaDoc = new CAADocument()
    caaDoc.with {
      document = _input
      cfIsLandscape = isLandscape
      cfIsResized = isResized
      cfIsOversized = isOverSized
    }
    _input.add(caaDoc)
    caaDoc.saveOrUpdate()
  } else {
    _input.cAADocuments.with {
      cfIsLandscape = isLandscape
      cfIsResized = isResized
      cfIsOversized = isOverSized
    }
    _input.cAADocuments.saveOrUpdate()
  }

  // Save & store resized file
  if (isResized || wasEncrypted) {
    tmpFile = File.createTempFile("tmp", ".pdf")
    source.save(tmpFile)
    source.close()
    _input.store(tmpFile)
    tmpFile.delete()
  }
  _input.saveOrUpdate()
}




