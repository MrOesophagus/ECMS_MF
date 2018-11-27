/*
	Change Management
 	Written by: kcsmbf 23/05/18
	Change Log:	kcsmbf 27/09/18 Removed superfluous function
*/

List<Document> caseDocs = _this.collect("Documents")

for (doc in caseDocs) {
  if (!doc.docDef) {
    DocDef newDocDef = DocDef.get(doc.cAADocuments.cfTypeofDocument.toString())
    doc.add(newDocDef)
    //addDocDef(doc, doc.cAADocuments.cfTypeofDocument.toString())
  }
}

/*
private void addDocDef(Document fDoc, String fDocDefNum) {
  DocDef newDocDef = DocDef.get(fDocDefNum)
  fDoc.add(newDocDef)
}
*/



