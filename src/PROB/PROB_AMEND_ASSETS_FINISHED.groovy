/*
	Change Management
 	Written by: kcsmbf 06/09/18
	Change Log:	kcsmbf 13/10/18 Nullsafed SAL regneration (caused errors on OA)
				kcsmbf 09/11/18 Added stamp mechanism for re-generated SAL
*/

Case cas = _actionDoc.case
amendments = cas.ctProbateAssets.findAll{it.cfIsNew || it.cfIsAmended}
boolean isIssued = false
if (_actionDoc.subCase.status == "ISS") { isIssued = true }

// Re-Issue Statement of Assets & Liabilities
Document sal = cas.documents.find{it.docDef.number == "529803"}
if (sal) {
  runRule("DOC_CREATE_FROM_TEMPLATE", ["document": sal])
  runRule("StampDocument_v2", ["document": sal, "stampPosition": "TR", "addSeal": "N", "addText": "Y", "pageNumBlankForAll":1 ,"stampChildDocuments": "N", "useIssueDate": false])
  sal.setIntendedToAmend(true)
}

for (asset in amendments) {
  // Re-Generate Registrar's Certificates if Grant has been Issued
  if (isIssued && asset.cfRegCert == "Y") {runRule("DOC_CREATE_REG_CERT", ["asset": asset, "document": asset.document])}
  // Reset Folder View flags to false
  asset.setCfIsNew(false)
  asset.setCfIsAmended(false)
}