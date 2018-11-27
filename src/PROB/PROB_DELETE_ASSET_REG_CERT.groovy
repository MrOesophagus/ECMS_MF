/*
	Change Management
 	Written by: 	kcsmbf 05/09/18
	Last change by:
*/

Document regCert = _asset?.document

if (regCert) {
  def formatter = java.text.NumberFormat.currencyInstance
  regCert.cfAdditionalInfo = _asset.cfProbateAssetTypeLabel + ": " + formatter.format(_asset.cfAmount)
  Date now = new Date()
  DocumentStatus newDocStatus = new DocumentStatus()
  newDocStatus.setDocument(regCert)
  newDocStatus.setStatusType("DELETED")
  newDocStatus.setBeginDate(now)
  regCert.statuses.add(newDocStatus)
  regCert.statuses.findAll{it.endDate == null}.minus(newDocStatus).each{it.endDate = now}
}





