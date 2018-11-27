/*
Change Management
Written by: kcsmbf 25/06/18
Change Log:	kcsmbf 01/07/18 Added Doc Status & link to ctProbateAssets for doc
            kcsmbf 19/07/18 Added UpdateReason functionality for (Re)Issue
*/


SubCase sc = _asset.case.collect("SubCases").first()
Party dec = sc.parties.find{ it.partyType == "DEC" }
HashMap params = new HashMap<String, Object>()
params.put("CAAProbateAssets", _asset)
params.put("Deceased", dec)
params.put("Case", _asset.case)

if (!_document) {
  docDef = DocDef.get("529805")
  doc = docDef.saveAndGenerateDocument(sc, params)
  doc.setCtProbateAssets(_asset)
  _asset.setDocument(doc)
  DocumentStatus newDocStatus = new DocumentStatus()
  doc.add(newDocStatus)
  newDocStatus.with{
    setStatusType("FILED")
    setBeginDate(new Date())
    setDocument(doc)
  }
  newDocStatus.saveOrUpdate()
  doc.saveOrUpdate()
  _asset.updateReason = 'N'
  _asset.saveOrUpdate()
} else {
  _document.generateTemplate(params)
  _asset.updateReason = 'N'
  _document.intendedToAmend = true
}

runRule("StampDocument_v2", ["document": _document?:doc, "stampPosition": "TR", "addSeal": "Y", "addText": "Y", "pageNumBlankForall": 1, "stampChildDocuments": "N", "useIssueDate": true])





