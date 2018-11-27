/*
Change Management
	Written by: kcsmbf 22/06/18
	Change Log:	kcsmbf 01/08/18 Added word/pdf limitations
				kcsmbf 09/08/18 Added NOT_STAMPABLE check to _document
				kcsmbf 20/08/18 Added Issue Date functionality
				kcsmbf 28/08/18 Added Probate Court Seal selection
				kcsmbf 05/10/18 Added exception catch & height/width parameters to applyImageStamp
				kcsmbf 12/10/18 Converted to ReadOnly with withTx usage for dbase safety
				kcsmbf 09/11/18 Added more mime types and SysProp for error emails recipient

NOTE: the coordinate system is as follows:
0,0 is the bottom-left corner
x is horizontal and increases to right
y is vertical and increase upward
Command structure for textstamp:
textStamp(String text, int rotation, int originX, int originY, int fontSize, String fontName, Color fontColor, Integer pageNumber)
*/

String serverEnv = SystemProperty.getValue("general.server.environmentType")
String errorRecipient = SystemProperty.getValue("mail.errors.sysadmin.emailTo")
List mimeTypes = ["application/pdf", "application/msword", "application/rtf", "application/vnd.openxmlformats-officedocument.wordprocessingml.document"]

if (mimeTypes.contains(_document.storageMimeType)) {
  File docPdf = _document.toPdf(_document.file)
  if (_stampPosition != "STR" && _addText == "Y" && !_document.cfDocumentNumber) { runRule("ADD_DOC_FDN", ["document": _document])}
  try {
    StampThis(docPdf, _document)
  } catch (Exception e) {
    failedBody = (!serverEnv.equalsIgnoreCase("prod")) ? serverEnv : ""
    failedBody += "\r\n I failed on " + _document.fullName + " (id: " + _document.id + ") on case: " + _document.case.caseNumber + "\r\n\r\n" + e
    runRule("SEND_EMAIL", ["emailAddress": errorRecipient, "subject": "Stamping Rule Failure", "body": failedBody])
  }
} else {
  failedBody = (!serverEnv.equalsIgnoreCase("prod")) ? serverEnv : ""
  failedBody += "\r\n I failed to convert to PDF " + _document.fullName + " (id: " + _document.id + ") on case: " + _document.case.caseNumber
  runRule("SEND_EMAIL", ["emailAddress": errorRecipient, "subject": "Stamping Rule Failure", "body": failedBody])
}

Set<Document> childDocs = _document.documents.findAll{ it.docDef.stampType.toString() != "NOT_STAMPABLE" }
if (_stampChildDocuments == "Y" && !childDocs.isEmpty()) {
  for (Document childDoc in childDocs) {
    if (mimeTypes.contains(childDoc.storageMimeType)) {
      File childDocPdf = childDoc.toPdf(childDoc.file)
      if (_stampPosition != "STR" && _addText == "Y" && !childDoc.cfDocumentNumber) { runRule("ADD_DOC_FDN", ["document": childDoc])}
      try {
        StampThis(childDocPdf, childDoc)
      } catch (Exception e) {
        failedBody = (!serverEnv.equalsIgnoreCase("prod")) ? serverEnv : ""
        failedBody += "\r\n I failed on " + childDoc.fullName + " (id: " + childDoc.id + ") on case: " + childDoc.case.caseNumber + "\r\n\r\n" + e
        runRule("SEND_EMAIL", ["emailAddress": errorRecipient, "subject": "Stamping Rule Failure", "body": failedBody])
      }
    } else {
      failedBody = (!serverEnv.equalsIgnoreCase("prod")) ? serverEnv : ""
      failedBody += "\r\n I failed to convert to PDF " + _document.fullName + " (id: " + _document.id + ") on case: " + _document.case.caseNumber
      runRule("SEND_EMAIL", ["emailAddress": errorRecipient, "subject": "Stamping Rule Failure", "body": failedBody])
    }
  }
}

private StampThis(File fDoc, Document fParent) {
  int x = com.sustain.lookuplist.model.LookupItem.getItem("CL_STAMP_POSITION", _stampPosition).getAttributes().find{ it.name == 'x' }.value.toInteger()
  int y = com.sustain.lookuplist.model.LookupItem.getItem("CL_STAMP_POSITION", _stampPosition).getAttributes().find{ it.name == 'y' }.value.toInteger()
  if (["TR","BL","STR"].contains(_stampPosition)) {
    DirOrgUnit org = DirOrgUnit.get(8339)
    switch (fParent.subCase.cfJurisdiction) {
      case "PC": // Probate
        stampImage = "SC_Seal"
        break;
    }
    DirAttachment da = org.attachments.find{ it.attachmentType == 'Stamp' && it.caption == stampImage }
    withTx {
      if (_addSeal == "Y") fDoc = Document.applyImageStamp(fDoc, da.getAttachmentFile().absolutePath, x+2, y-2, 130, 96, _pageNumBlankForAll)
      if (_addText == "Y") {
        if (_useIssueDate) {
          if (!fParent.cfdateIssued) { fParent.cfdateIssued = new Date() }
          line2 = "Issue Date: " + fParent.cfdateIssued.format("dd MMM yyyy").toString()
        } else {
          line2 = "Filing Date: " + fParent.dateFiled.format("dd MMM yyyy").toString()
        }
        fDoc = Document.applyTextStamp(fDoc, "FDN: " + fParent.cfDocumentNumber.toString()?:'', 0, x+10, y-12, 10, "Arial", java.awt.Color.black, _pageNumBlankForAll)
        fDoc = Document.applyTextStamp(fDoc, line2, 0, x+10, y-25, 10, "Arial", java.awt.Color.black, _pageNumBlankForAll)
        fDoc = Document.applyTextStamp(fDoc, "Pages: " + fParent.storedPageCount.toString(), 0, x+10, y-39, 10, "Arial", java.awt.Color.black, _pageNumBlankForAll)
      }
    }
  } else {
    String textString = fParent.case.caseNumber + '     Filed: ' + fParent.dateFiled.format("dd MMM yyyy h:mm:ss a")
    int rotation = (_stampPosition == "LE") ? 90 : 0
    withTx { fParent.applyTextStamp(fDoc, textString, rotation, x, y, 12, "Arial", java.awt.Color.blue, _pageNumBlankForAll) }
  }
  withTx { // Finishing up
    fParent.store(fDoc)
    fParent.saveOrUpdate()
  }
}



