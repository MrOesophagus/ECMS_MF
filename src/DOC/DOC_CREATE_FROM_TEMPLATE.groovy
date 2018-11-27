/*
	Change Management
 	Written by: kcsmbf 14/03/18
	Change Log:	kcsmbf 19/03/18 Added context elements
				kcsmbf 26/06/18 Commented out doc stored check (not sure why it was there())
				kcsmbf 07/08/18 Added ctProbateAssets parameter for Statement of A&L
*/

HashMap params = new HashMap<String, Object>()
params.put("CAADocument", _document.cAADocuments)
params.put("Document", _document)
params.put("Case", _document.case)
if (_document.ctProbateAssets) params.put("CAAProbateAssets", _document.ctProbateAssets)
// params.put("partyDocuments", _document.partyDocuments)
_document.generateTemplate(params)
_document.saveOrUpdate()




