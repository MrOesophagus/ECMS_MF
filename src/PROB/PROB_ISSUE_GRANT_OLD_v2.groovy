/*
	Change Management
	Written by: kcsmbf 31/10/18
	Change Log: kcsmbf 09/11/18 Rewritten to be self contained
				kcsmbf 11/11/18 Minor code tweaks
*/

import java.awt.Color
import groovy.transform.Field
import com.sustain.lookuplist.model.LookupItem
import org.apache.pdfbox.multipdf.PDFMergerUtility

// Main script variables
String emailBody
Document grantDoc, origGrantDoc, willDoc, certTransDoc, ndaDoc
File newGrantFile, newWillFile, newCertTransFile, newOrigGrantFile, regCertFile
SubCase sc = _actionDoc.subCase
HashMap regCertDocAndFile = new HashMap<Document, File>()
int docNumMax = (_actionDoc.case.collect("Documents").cfDocumentNumber.max()?:0).toInteger()

// Global script variables
@Field Date now = new Date()
@Field Set<DomainObject> objectsToSave = new HashSet<>()
@Field String serverEnv = SystemProperty.getValue("general.server.environmentType")
@Field String sysErrorsTo = SystemProperty.getValue("mail.errors.sysadmin.emailTo")
@Field List mimeTypes = ["application/pdf", "application/msword", "application/rtf", "application/vnd.openxmlformats-officedocument.wordprocessingml.document"]

// *******
// 1.0 Document checks & processing
// *******
try {
  // 1.1 Grant processing
  String grantDDN = (_amended) ? "519915" : "529804"  // Amended Grant vs Generated Grant
  grantDoc = sc.documents.find{it.docDef.number == grantDDN && !it.statuses}
  if (!grantDoc || !grantDoc.stored) {
    addError("ERROR: There is something wrong with the draft grant document (not present, no file attached, or it has a status).")
    return
  }
  objectsToSave.add(grantDoc)
  if (sc.category == "510400") { // Handling Reseal of Grant applications
    origGrantDoc = sc.documents.find{it.docDef.number == "519908" && it.statuses.find{st -> st.statusType == "CHKD"}}
    if (!origGrantDoc || !origGrantDoc.stored) {
      addError("ERROR: There is something wrong with the Original Grant document (not present, no file attached, or it has a status).")
      return
    }
    newOrigGrantFile = origGrantDoc.toPdf(origGrantDoc.file)
    objectsToSave.add(origGrantDoc)
  }
  // 1.2 Will processing
  if (_actionDoc.case.category != "510300") { // Checking it's not a LoA without Will
    String willDDN = (!sc.collect("Documents[docDefNumber == '519920']").isEmpty()) ? "519920" : "519902"
    // Engrossed vs normal will
    if (willDDN == "519902") {
      willDoc = sc.documents.find { it.docDef.number == willDDN && it.statuses.find { st -> st.statusType == "CHKD" } }
      if (!willDoc || !willDoc.stored) {
        addError("ERROR: There is something wrong with the Will document (no file attached, or not Checked).")
        return
      }
    } else {
      willDoc = sc.documents.find { it.docDef.number == willDDN }
      if (!willDoc || !willDoc.stored) {
        addError("ERROR: There is something wrong with the Engrossed Will document (no file attached, or not Checked).")
        return
      }
    }
    objectsToSave.add(willDoc)
  }
  certTransDoc = sc.documents.find{it.docDefNumber == "519904" && it.collect("Statuses[StatusType == 'REJ']").isEmpty()}
  if (certTransDoc) {
    if (!certTransDoc.stored) {
      addError("ERROR: The certified translation of will document does not have a file attached.")
      return
    }
    objectsToSave.add(certTransDoc)
  }
} catch (Exception e) {
  addError("Unfortunately the Issue Grant process has failed - a systems administrator has been notified and will contact you shortly.")
  sendFailure("BR error: PROB_ISSUE_GRANT_OLD_v2 failed in Sections 1.1 - 1.2 for " + sc.case.caseNumber + "\r\n\r\n" + e.toString())
  return
}

// 1.3 Notice as to Disclosure of Assets processing
withTx {
  docDef = DocDef.get("529909")
  ndaDoc = docDef.saveAndGenerateDocument(sc, ["case": sc.case])
}

// 1.4 Stamping the documents
try {
  grantDoc.cfDocumentNumber = (grantDoc.cfDocumentNumber) ?: ++docNumMax // FDN check
  addROA(grantDoc)
  grantDoc.cfdateIssued = (grantDoc.cfdateIssued) ?: now
  newGrantFile = stampDoc(grantDoc, "Y", 1, true)
  if (sc.category == "510400" && origGrantDoc) { // Handling Reseal of Grant applications
    //newOrigGrantFile = stampDoc(origGrantDoc, "Y", 0, false) // Not stamped apparently?
    if (!origGrantDoc.cfDocumentNumber) { origGrantDoc.cfDocumentNumber = ++docNumMax } // FDN check
  }
  if (willDoc) {
    if (!willDoc.cfDocumentNumber) { willDoc.cfDocumentNumber = ++docNumMax } // FDN check
    newWillFile = stampDoc(willDoc, "N", 0, false)
    if (certTransDoc) {
      if (!certTransDoc.cfDocumentNumber) { certTransDoc.cfDocumentNumber = ++docNumMax } // FDN check
      newCertTransFile = stampDoc(certTransDoc, "N", 0, false)
    }
  }
  //newNdaFile = stampDoc(ndaDoc, "Y", 1, false) // Not stamped apparently?
} catch (Exception e) {
  addError("Unfortunately the Issue Grant process has failed - a systems administrator has been notified and will contact you shortly.")
  sendFailure("BR error - PROB_ISSUE_GRANT_OLD_v2 failed in Section 1.4 for " + sc.case.caseNumber + "\r\n\r\n" + e.toString())
  ndaDoc.deleteDocumentAndStorage()
  return
}

// *******
// 2.0 Document consolidation & RegCerts
// *******
// 2.1 Merging the documents into one PDF
try {
  PDFMergerUtility PDFMerger = new PDFMergerUtility()
  PDFMerger.addSource(newGrantFile)
  if (newWillFile) {
    PDFMerger.addSource(newWillFile)
    if (newCertTransFile) { PDFMerger.addSource(newCertTransFile) }
  }
  if (sc.category == "510400" && newOrigGrantFile) { PDFMerger.addSource(newOrigGrantFile) }
  PDFMerger.addSource(ndaDoc.file)
  withTx {
    tmpFile = File.createTempFile("tmp", ".pdf")
    PDFMerger.setDestinationFileName(tmpFile.toString())
    PDFMerger.mergeDocuments(null)
  }
  newGrantFile = tmpFile
} catch (Exception e) {
  addError("Unfortunately the Issue Grant process has failed - a systems administrator has been notified and will contact you shortly.")
  sendFailure("BR error: PROB_ISSUE_GRANT_OLD_v2 failed in Section 2.1 for " + sc.case.caseNumber + "\r\n\r\n" + e.toString())
  return
}

// 2.2 Generating the Registrar's Certificates for each asset
Party dec = sc.parties.find{ it.partyType == "DEC" }
HashMap assetParams = new HashMap<String, Object>()
assetParams.put("Deceased", dec)
assetParams.put("Case", sc.case)
if (!_amended) {
  try {
    for (asset in _actionDoc.case.ctProbateAssets.findAll{ it.cfRegCert == "Y" }) {
      Document regCert
      assetParams.put("CAAProbateAssets", asset)
      withTx {
        docDef = DocDef.get("529805")
        regCert = docDef.saveAndGenerateDocument(sc, assetParams)
      }
      assetParams.remove("CAAProbateAssets")
      objectsToSave.add(regCert)
      regCert.with {
        setCtProbateAssets(asset)
        setSubCase(sc)
        setCase(sc.case)
        setCfDocumentNumber(++docNumMax)
        setCfdateIssued(now)
      }
      sc.documents.add(regCert)
      asset.setDocument(regCert)
      DocumentStatus newDocStatus = new DocumentStatus()
      objectsToSave.add(newDocStatus)
      regCert.add(newDocStatus)
      newDocStatus.with {
        setStatusType("FILED")
        setBeginDate(now)
        setDocument(regCert)
      }
      regCertFile = stampDoc(regCert, "Y", 1, true)
      regCertDocAndFile.put(regCert, regCertFile)
      addROA(regCert)
    }
  } catch (Exception e) {
    addError("Unfortunately the Issue Grant process has failed - a systems administrator has been notified and will contact you shortly.")
    sendFailure("BR error: PROB_ISSUE_GRANT_OLD_v2 failed in Section 2.2 for " + sc.case.caseNumber + "\r\n\r\n" + e.toString())
    return
  }
}

// *******
// 3.0 Final processing and database transaction
// *******
try {
  // 3.1 Making available on Portal
  DocumentStatus newDocStatus = new DocumentStatus()
  objectsToSave.add(newDocStatus)
  grantDoc.add(newDocStatus)
  newDocStatus.with{
    setStatusType("FILED")
    setBeginDate(now)
    setDocument(grantDoc)
  }
  if (_amended) { // Un-hiding amended grant draft Word Document from Portal FV now that we're issuing it
    grantDoc.docDef.formGroups.clear()
    grantDoc.docDef.formGroups.add("OTH")
  }

  // 3.2 Setting case & subCase statuses
  sc.setStatus('ISS')
  sc.setStatusDate(now)
  sc.case.setStatus('CLOS')
  sc.case.setStatusDate(now)

  // 3.3 Final save transaction
  withTx {
    grantDoc.store(newGrantFile)
    if (willDoc) {
      willDoc.store(newWillFile)
      if (certTransDoc) { certTransDoc.store(newCertTransFile) }
    }
    if (sc.category == "510400") { origGrantDoc.store(newOrigGrantFile) }
    // RegCerts
    regCertDocAndFile.each{ k,v ->
      k.store(v)
    }
    DomainObject.saveOrUpdateAll(objectsToSave)
  }
} catch (Exception e) {
  addError("Unfortunately the Issue Grant process has failed - a systems administrator has been notified and will contact you shortly.")
  sendFailure("BR error: PROB_ISSUE_GRANT_OLD_v2 failed in Section 3 for " + sc.case.caseNumber + "\r\n\r\n" + e.toString())
  return
} finally {
  ndaDoc.deleteDocumentAndStorage()
  tmpFile.delete()
}

// *******
// 4.0 Email processing
// *******
// 4.1 Sending email to Lodging Party/Parties
if (_amended) {
  emailBody = "Your application to amend the Grant for " + sc.case.caseShortName + " has been approved and a new Grant has been issued. This can be accessed via CourtSA."
} else {
  emailBody = "A Grant and Registrar's Certificates for " + sc.case.caseShortName + " have been issued and can be downloaded from CourtSA."
}
try {
  appPtyNum = 0 // Applicants on OrigApp are parties[0]
  if (sc.case.filingType == "510000") { appPtyNum = 1 } // Applicants on Grants are parties[1]
  runRule("SEND_EMAIL_VIA_PARTY_TYPE", ["case": sc.case, "partyType": sc.parties[appPtyNum].partyType, "bodyMessage": emailBody])
} catch (Exception e) {
  addError("Unfortunately the Issue Grant process has failed - a systems administrator has been notified and will contact you shortly.")
  sendFailure("BR error: PROB_ISSUE_GRANT_OLD_v2 failed in Section 4.1 for " + sc.case.caseNumber + "\r\n\r\n" + e.toString())
  return
}

// 4.2 Sending email to PT if case is Letters of Admin
try {
  if (!_amended && ["510200", "510300"].contains(sc.case.category)) {
    toList = new ArrayList()
    toList.add(SystemProperty.getValue("mail.publicTrustee.emailTo"))
    subject = "In relation to " + sc.case.caseNumber + " - " +  sc.case.caseShortName
    StringBuilder sb = new StringBuilder()
    sb.append("Supreme Court of South Australia \r\n")
    sb.append("Probate Registry \r\n\r\n")
    sb.append("RE: " + sc.case.caseNumber + " - " + sc.case.caseShortName + "\r\n\r\n")
    sb.append("Dear Public Trustee, \r\n\r\n")
    sb.append("A grant for the above mentioned estate has now been issued, please find the sealed Grant and a Statement of Assets and Liabilities attached. \r\n\r\n")
    sb.append("Yours faithfully, \r\n" + "Registrar of Probates")
    body = sb.toString()
    sal = sc.documents.find{ it.docDefNumber == '529803' }  // Statement of A&L
    runRule("SEND_EMAIL_WITH_ATTACH", ["emailAddresses": toList, "subject": subject, "body": body, "attachment1": grantDoc, "attachment2": sal, "attachment3": null])
  }
} catch (Exception e) {
  addError("Unfortunately the Issue Grant process has failed - a systems administrator has been notified and will contact you shortly.")
  sendFailure("BR error: PROB_ISSUE_GRANT_OLD_v2 failed in Section 4.2 for " + sc.case.caseNumber + "\r\n\r\n" + e.toString())
  return
}

// *******
// 5.0 Functions
// *******
private sendFailure(String fString) {
  // Swap these around for testing vs. live
  //logger.debug(fString)
  runRule("SEND_EMAIL", ["emailAddress": sysErrorsTo, "subject": serverEnv + ": Issue Grant failure", "body": fString])
}

private stampDoc(Document fDoc, String fAddText, Integer fPageNum, boolean fUseIssueDate) {
  fPageNum = (fPageNum != 0) ? fPageNum : null
  if (mimeTypes.contains(fDoc.storageMimeType)) {
    // Conversion to PDF
    try {
      docPdfFile = fDoc.toPdf(fDoc.file)
    } catch (Exception e) {
      String errorString = "Could not convert the requested document to a PDF on " + fDoc + " (" + fDoc.fullName + ")\r\n\r\n" + e.toString()
      sendFailure(errorString)
      return
    }
    // Create and apply stamp
    try {
      int x = LookupItem.getItem("CL_STAMP_POSITION", "TR").getAttributes().find {
        it.name == 'x'
      }.value.toInteger()
      int y = LookupItem.getItem("CL_STAMP_POSITION", "TR").getAttributes().find {
        it.name == 'y'
      }.value.toInteger()
      DirOrgUnit org = DirOrgUnit.getByCode("CAA")
      DirAttachment da = org.attachments.find { it.attachmentType == 'Stamp' && it.caption == "SC_Seal" }
      docPdfFile = Document.applyImageStamp(docPdfFile, da.getAttachmentFile().absolutePath, x, y, 135, 100, fPageNum)
      if (fAddText == "Y") {
        if (fUseIssueDate) {
          line2 = "Issue Date: " + fDoc.cfdateIssued.format("dd MMM yyyy").toString()
        } else {
          line2 = "Filing Date: " + fDoc.dateFiled.format("dd MMM yyyy").toString()
        }
        docPdfFile = Document.applyTextStamp(docPdfFile, "FDN: " + fDoc.cfDocumentNumber.toString() ?: '', 0, x + 10, y - 12, 10, "Arial", Color.black, fPageNum)
        docPdfFile = Document.applyTextStamp(docPdfFile, line2, 0, x + 10, y - 25, 10, "Arial", Color.black, fPageNum)
        docPdfFile = Document.applyTextStamp(docPdfFile, "Pages: " + fDoc.storedPageCount.toString(), 0, x + 10, y - 39, 10, "Arial", Color.black, fPageNum)
      }
    } catch (Exception e) {
      String errorString = "Could not stamp the requested document on " + fDoc + " (" + fDoc.fullName + ")\r\n\r\n" + e.toString()
      sendFailure(errorString)
      return
    }
  } else {
    String errorString = "Supplied document is not a Word or PDF type file on " + fDoc + " (" + fDoc.fullName + ")\r\n\r\n"
    sendFailure(errorString)
    return
  }
  return docPdfFile
}

private addROA(fRoaDoc) {
  ROAMessage roa = new ROAMessage()
  objectsToSave.add(roa)
  roa.category = 'COR'
  roa.subCategory = 'FILED'
  roa.message = fRoaDoc.fullName + ' filed  FDN:' + fRoaDoc.cfDocumentNumber
  roa.recordEntityName = fRoaDoc.entityShortName
  roa.recordId = fRoaDoc.id
  roa.timestamp = now
  fRoaDoc.case.add(roa)
}


