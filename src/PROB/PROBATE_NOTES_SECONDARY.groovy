/*
Change Management
Written by:	kcsmbf 15/05/18
Change Log:	kisnum	17/8/2018 Assets & Liability changes for grant
			kiseeg 22/08/2018 Partial date of will code WPDAC changed for Caveat
			kcsmbf 03/10/2018 Added fourth argument for addPNote function
			kcsmbf 10/10/18 Added setting of Requisition Text in addPNote function
*/

import java.io.StringWriter
import org.apache.velocity.app.Velocity
import org.apache.velocity.VelocityContext

Date today = new Date()
SubCase sc = _curDoc.subCase
Case cas = _curDoc.case
Party dec = sc.collect("Parties[PartyType=='DEC']").first()
List<String> noteTypes = cas.collect("ctProbateNotes").collect("cfType")


switch (cas.filingType) {
  case "510000": // Probate Grant

    if (["519908"].contains(_curDoc.docDefNumber)){
      Document grant =  _curDoc
      if (!grant.collect("Statuses[statusType == 'ACCEPT' && (endDate == null || endDate >= #p1)]", today).isEmpty() && !noteTypes.contains("GACCPT"))
      { addPNote("GACCPT", sc.case, today, null) }
    }
    if (["519902"].contains(_curDoc.docDefNumber)){
      Document will =  _curDoc
      if (!will.collect("Statuses[statusType == 'ACCEPT' && (endDate == null || endDate >= #p1)]", today).isEmpty() && !noteTypes.contains("WACCPT"))
      { addPNote("WACCPT", sc.case, today, null) }
    }

    // Assets & Liabilities
    if (!noteTypes.contains("REALNVG")){
      cas.ctProbateAssets.findAll{it.cfProbateAssetType == "REAL" && it.cfValuedBy == "OTH"}.each{ addPNote("REALNVG", cas, today, null) }
    }
    if (!noteTypes.contains("MVEHVAE")){
      cas.ctProbateAssets.findAll{it.cfProbateAssetType == "MVEH" && it.cfValuedBy == "AE"}.each{ addPNote("MVEHVAE", cas, today, null) }
    }
    if (!noteTypes.contains("PTRUSTVAE")){
      cas.ctProbateAssets.findAll{it.cfProbateAssetType == "PTRUST" && it.cfValuedBy == "AE"}.each{ addPNote("PTRUSTVAE", cas, today, null) }
    }
    if (!noteTypes.contains("EINTVAE")){
      cas.ctProbateAssets.findAll{it.cfProbateAssetType == "EINT" && it.cfValuedBy == "AE"}.each{ addPNote("EINTVAE", _cas, today, null) }
    }
    if (!noteTypes.contains("OAVAE")){
      cas.ctProbateAssets.findAll{it.cfProbateAssetType == "OTHERA" && it.cfValuedBy == "AE"}.each{ addPNote("OAVAE", cas, today, null) }
    }
    if (!noteTypes.contains("FURN")){
      cas.ctProbateAssets.findAll{it.cfProbateAssetType == "PERS" && it.cfValuedBy == "AE"}.each{ addPNote("FURN", cas, today, null) }
    }
    break;

  case "520000":
    if (["529901", "529903"].contains(_curDoc.docDefNumber)) {
      _curDoc.documents.findAll{it.docDefNumber == '519902' && it.cAADocuments.cfIsWillDated == 'N'}.each{ addPNote("WNDAT", sc.case, today, null) }
      _curDoc.documents.findAll{it.docDefNumber == '519902' && it.cAADocuments.cfIsWillDated == 'P'}.each{ addPNote("WPDAC", sc.case, today, null) }
    }
    break;
} // End switch

// Function for adding Probate Note
private void addPNote(String fType, Case fCase, Date fDate, String fInfo) {
  CtProbateNotes pnote = new CtProbateNotes()
  pnote.cfType = fType
  pnote.cfNoteDate = fDate
  if (fInfo) pnote.cfAdditionalInfo = fInfo
  // Returning Requisition text from LUList Attribute
  StringWriter swOut = new StringWriter()
  String logError = "logError"
  String attText = com.sustain.lookuplist.model.LookupItem.getItem("CL_PROBATE_NOTE_TYPE", fType).getAttributes()?.find{it.name == 'PNOTE'}.getValue()
  Velocity.init()
  VelocityContext context = new VelocityContext()
  context.put("case", fCase)
  context.put("sysPropTool", SystemProperty)
  Velocity.evaluate(context, swOut, logError, attText)
  pnote.cfMemo = swOut.getBuffer().toString()
  pnote.case = fCase
  pnote.cfStatus = "O"
  fCase.add(pnote)
  pnote.saveOrUpdate()
}




