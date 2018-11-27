/*
	Change Management
 	Written by: 	kcsmbf 19/07/18
	Last change by:	kcsmbf 03/08/18 Added functionality to handle Orig Apps (530000)
					kiseeg 14/08/18 Changed condition _sc.status != "PENDING" from ==
					kcsmbf 20/11/18 Added accepted vs rejected flag as per idea from kiseeg/Boss Lady
*/

String newStatus = (_accepted) ? "AUTO" : "REJECTED"

Date today = new Date()
Set<Document> scDocs = _sc.documents.findAll{it.statuses.find{st -> st.statusType == "AUTOPENDING" && !st.endDate}}

if (scDocs && (_sc.filingType == "530000" || (_sc.filingType != "530000" && _sc.status != "PENDING"))) {
  for (Document doc in scDocs) {
    doc.statuses.findAll{it.statusType == "AUTOPENDING" && !it.endDate}.each{it.endDate = today}
    DocumentStatus newDocStatus = new DocumentStatus()
    doc.add(newDocStatus)
    newDocStatus.with{
      setStatusType(newStatus)
      setBeginDate(today)
      setDocument(doc)
    }
    newDocStatus.saveOrUpdate()
    doc.saveOrUpdate()
  }
}