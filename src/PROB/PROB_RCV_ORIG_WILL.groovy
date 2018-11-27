/*
	Change Management
	Written by: kcsmbf 02/08/2018
	Change Log:	kisnum 09/08/2018 - added a check for cfUploadDocument is true, only then should the Will be set to ACCEPTED
				kcsmbf 15/08/2018 - Added WD & Renunc Close code
*/

import com.sustain.rule.context.*

Date now = new Date()
SubCase sc = _doc.subCase

//check if will matches original, then proceed
if( _doc.cAADocuments.cfUploadDocument == 'Y'){

// Marking Will as Accepted
  addDocStatus(_doc, "ACCEPT", now, true)

  if (sc.filingType == "510000") {  // Grant functionality
    runRule("PROBATE_NOTES_SECONDARY", ["curDoc": _doc])
  } else if (sc.filingType == "540000") {  // Subpoena functionality
    if (!_doc.statuses.find{["FILED", "AUTO"].contains(it.statusType) && !it.endDate}) {
      addDocStatus(_doc, "AUTO", now, false)
    }
    String bodyMessage = "The Will in the matter of " + sc.case.caseName + " has been received by the Probate registry. Refer to fact sheet for next steps."
    runRule("SEND_EMAIL_VIA_PARTY_TYPE", ["case": _doc.case, "partyType": "EXAD", "bodyMessage": bodyMessage, "tokens": null])
  }
}

_doc.saveOrUpdate()

// Closing Will Deposit and Renunciation after Original Will is Received
if (sc.filingType == "550000" || (sc.filingType == "560000" && sc.ctCAAProbates.cfHasOriginalWill == "HOW")) {
  sc.setStatus("CLOS")
  sc.setStatusDate(now)
  sc.case.setStatus("CLOS")
  sc.case.setStatusDate(now)
}
private addDocStatus(Document fDoc, String fStatus, Date fDate, boolean fEndOthers) {
  DocumentStatus newDocStatus = new DocumentStatus()
  newDocStatus.setDocument(fDoc)
  fDoc.statuses.add(newDocStatus)
  newDocStatus.setStatusType(fStatus)
  newDocStatus.setBeginDate(fDate)
  if (fEndOthers) {
    fDoc.statuses.findAll{!["FILED", "AUTO", "AUTOPENDING"].contains(it.statusType) && !it.endDate}.minus(newDocStatus).each{it.endDate = fDate}
  }
}
