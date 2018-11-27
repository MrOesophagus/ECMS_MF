/*
	Change Management
	Written by: kcsmbf 23/07/18
	Change Log:	kcsmbf 31/07/18 Added Revoke functionality
*/

SubCase sc = _doc.subCase
Date now = new Date()
_retVal = false

// End Date current Application MRN status, add Filed Status, hide Amended Grant Word doc
addDocStatus(_doc, "FILED", now, true)
Document childGrant = _doc.documents.find{it.docDefNumber == "519915"}
if (childGrant) {
  childGrant.docDef.formGroups.clear()
  childGrant.docDef.formGroups.add('HIDEME')
  sc.setStatus('OPEN')
  sc.setStatusDate(now)
  sc.case.setStatus('OPEN')
  sc.case.setStatusDate(now)
  _retVal = true
}

// Cancel/Revoke previous Grant and Amendments (if not done already)
String badStatus = (_revoked) ? "REVOKED" : "CANCEL"
Document grant = sc.documents.find{it.docDefNumber == '529804'}
if (!grant.collect("Statuses[(StatusType == 'CANCEL' || StatusType == 'REVOKED') && endDate == null]")) {
  addDocStatus(grant, badStatus, now, true)
}
sc.collect("Documents[docDefNumber == '519915']").each{
  if (it.statuses.findAll{st-> st.statusType == 'FILED' && !st.endDate}) addDocStatus(it, badStatus, now, true)
}

private addDocStatus(Document fDoc, String fStatus, Date fDate, boolean fEndOthers) {
  DocumentStatus newDocStatus = new DocumentStatus()
  newDocStatus.setDocument(fDoc)
  fDoc.statuses.add(newDocStatus)
  newDocStatus.setStatusType(fStatus)
  newDocStatus.setBeginDate(fDate)
  if (fEndOthers) {
    fDoc.statuses.findAll{it.endDate == null}.minus(newDocStatus).each{it.endDate = fDate}
  }
}