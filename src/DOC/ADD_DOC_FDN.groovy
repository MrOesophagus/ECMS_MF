/*
	Change Management
 	Written by: kcsmbf 01/06/18
	Change Log:	kiseeg 29/06/18 Added ignoreFDN - 529910 529911 Copy of Record
				kcsmbf 25/07/18 Added conditional Notice of Acting to ignoreFDN
				kiseeg 24/09/18 Added ROA messages
				kiseeg 26/09/18 Changed ROA RecordId to Document
*/
List<String> ignoreFDN = ["419901", "419902", "410199", "529910", "529911", "529801"]   // Ignore list = Proof of Consent&Auth, Affidavit Volume
if (_document.case.filingType == "110000") { ignoreFDN.add("110101") }

// Set Document File Number (FDN)
int docNumMax = (_document.case.collect("Documents").cfDocumentNumber.max()?:0).toInteger()
if (!_document.cfDocumentNumber && !ignoreFDN.contains(_document.docDefNumber) &&
  _document.docDefFormTypeLabel != "Action" && !_document.docDefFormGroups.contains("HIDEME")) {
  _document.cfDocumentNumber = ++docNumMax
  Send_ROA (_document)
}
// Set FDN for any Child Documents
Set<Document> childDocs = _document.documents
for (Document childDoc in childDocs) {
  if (!childDoc.cfDocumentNumber && !ignoreFDN.contains(childDoc.docDefNumber) &&
    childDoc.docDefFormTypeLabel != "Action" && !childDoc.docDefFormGroups.contains("HIDEME")) {
    childDoc.cfDocumentNumber = ++docNumMax
    Send_ROA (childDoc)
  }
}

//FUNCTIONS
private Send_ROA (Document rdoc) {
  Date today = new Date()
  ROAMessage roa = new ROAMessage()
  roa.category = 'COR'
  roa.subCategory = 'FILED'
  roa.message = rdoc.fullName + ' filed  FDN:' + rdoc.cfDocumentNumber
  roa.recordEntityName = rdoc.entityShortName
  roa.recordId = rdoc.id
  roa.timestamp = new Date()
  rdoc.case.add(roa)
  roa.saveOrUpdate()
  return
}



