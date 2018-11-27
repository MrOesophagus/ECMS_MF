/*
	Change Management
 	Written by: kcsmbf 17/01/18
	Change Log:	kcsmbf 17/04/18 Added endDate functionality
				kcsmbf 29/05/18 Added WFProcess code section
				kcsmbf 04/06/18 Rewrote to handle childDocs
				kcsmbf 16/07/18 Edited endOtherStatuses code inside function
*/

Date today = new Date()
Set<Document> childDocs = _document.documents

addNewDocStatus(_document, _status, today, _endDateOtherStatuses)
for (Document childDoc in childDocs) {
  addNewDocStatus(childDoc, _status, today, _endDateOtherStatuses)
  childDoc.saveOrUpdate()
}

// Uplift Will functionality
if (_document.docDefNumber == "539911" && _status == "FILED") {
  Document will = _document.subCase.documents.find{it.docDefNumber == "519902"}
  addNewDocStatus(will, "PENDING", today, "N")
}

_document.saveOrUpdate()

private void addNewDocStatus(Document fDoc, String fStatus, Date fDate, String fEndOthers) {
  DocumentStatus newDocStatus = new DocumentStatus()
  newDocStatus.setDocument(fDoc)
  newDocStatus.setStatusType(fStatus)
  newDocStatus.setBeginDate(fDate)
  fDoc.statuses.add(newDocStatus)
  if (fEndOthers == "Y") {
    fDoc.statuses.findAll{it.endDate == null && !["AUTO","FILED"].contains(it.statusType)}.minus(newDocStatus).each{it.endDate = fDate}
  }
}





