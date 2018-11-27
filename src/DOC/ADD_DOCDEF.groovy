/*
	Change Management
 	Written by: kcsmbf 02/02/2018
	Change Log:	kcsmbf 05/03/18 New docDef numbers
				kcsmbf 16/05/18 Rewrote to simplify
				kcsmbf 15/06/18 Added functionality for empty records to be added
*/

String docCategory

/*if (doc.type.contains("OFF")) {
  addDocDef(doc, "429901")
} else {
*/
selection = _document.subCase.cAAReusableFields.clList03 ?: _clList03
for (element in selection) {
  if (element) docCategory = element.toString()
}
addDocDef(_document, docCategory)

private void addDocDef(Document fDoc, String fDocDefNum) {
  DocDef newDocDef = DocDef.get(fDocDefNum)
  fDoc.add(newDocDef)
}



