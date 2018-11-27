/*
	Change Management
 	Written by: 	KCSMBF 06/03/2018
	Last change by:	KCSMBF 07/03/2018
	Change Reason:	Removed Civil Claim code - probably unnecessary
*/

SubCase sc = _subCase
Document master = sc.documents.find{it.docDefNumber == _docDefMaster}

Set<Document> doc1docs = sc.documents.findAll{it.docDefNumber == _docDefToAppend1}
if (doc1docs) {
  for (Document doc in doc1docs) {
    Document.appendPdfs(master.file, doc.file)
  }
}

if (_docDefToAppend2) {
  Set<Document> doc2docs = sc.documents.findAll{it.docDefNumber == _docDefToAppend2}
  if (doc2docs) {
    for (Document doc in doc2docs) {
      Document.appendPdfs(master.file, doc.file)
    }
  }
}
master.saveOrUpdate()
sc.saveOrUpdate()




