/*
Change Management
Written by: 	kcsmbf 15/05/18
Change Log: 	kisnum 30/05/18 Added check for Grant Self-Rep
kisnum 30/05/18 The input for marriage detail changed
kcssmd 06/06/18 Added Originating Application PNotes
kisnum 07/06/18 Added Open Status to Probate note function
kistah 12/06/18 Added Will Deposit PNotes
kcssmd 13/06/18 Added Assets & Liabilities PNotes for Originating App
kistah 14/06/18 Added Probate Miscellaneous Application PNotes
kistah 15/06/18 Revised Probate Will Deposit and Miscellaneous Application Categories
kisnum 20/08/18 Added Probate note for Overseas Late address for Grant
kcssmd 07/09/18 Added Probate note for Other asset for Originating Application
kisnum 16/09/18 Chnaged some PNotes due to field moving to different entities
kistah 26/09/18 Changed Grant Probate note to trigger when the Valued By is not the Valuer General & when an associated party is defined on the grant init form
kisnum 28/9/2018 changed marriage details back to yes/no questions
kcsmbf 03/10/18 Addded fourth argument for addPNote function
kisnum 08/10/18 Changed Deceased Name on Will probate note as data moved to person profiles
kcsmbf 10/10/18 & 17/10/18 Added setting of Requisition Text in addPNote function
kisnum 01/11/18 Removed PNote for A&L for Other Property Valued By Applicant & added "OAVAE" for Other Asset Valued by Applicant
*/

import java.io.StringWriter
import org.apache.velocity.app.Velocity
import org.apache.velocity.VelocityContext

Date today = new Date()
SubCase sc = _case.collect("SubCases").first()
Party dec = sc.collect("Parties[PartyType=='DEC']").first()
Party wm = sc.collect("Parties[PartyType=='WM']").first()
List<Document> caseDocs = _case.collect("Documents")
List<String> DDNs = caseDocs.collect("docDefNumber")
List<Party> casePtys = sc.collect("Parties")

//Self rep probate notes
if (!_case.assignments.find{["LAW", "LAWFIRM"].contains(it.assignmentRole) && it.status == "CUR"}) {
  if (_case.filingType == "510000" || _case.filingType == "530000") {
    addPNote("SREP", _case, today, null)
  } else {
    addPNote("SREPOTH", _case, today, null)
  }
}

switch (_case.filingType) {
  case "510000": // Probate Grant
    // Document specific PNotes
    Document grant = caseDocs.find{it.docDefNumber == "519908"}
    if (DDNs.contains("510101")) { addPNote("APSF", _case, today, null) }
    if (DDNs.contains("510103")) { addPNote("ADP", _case, today, null) }

    // Will specific PNotes
    if (sc.ctCAAProbates.cfHasOriginalWill == "CHOW") { addPNote("CHOW", _case, today, null) }
    for (Document will in caseDocs.findAll{it.docDefNumber == "519902"}) {
      if (will.cAADocuments.cfIsWillDated == "N") { addPNote("WNDAT", _case, today, null) }
      if (will.cAADocuments.cfIsWillDated == 'P') { addPNote("WPDAT", _case, today, null) }
      //if (will.cAADocuments.cfIsWillNameSame == "N") { addPNote("DNWD", _case, today, null) } moved to dec person.profile
    }

    // Marriage details PNotes YES NO  Questions
    if (sc.ctCAAProbates.cfDecMarriedAfterWill == "Y") { addPNote("MARAFT", _case, today, null) }
    if (sc.ctCAAProbates.cfDecDivorcedAfterWill == "Y") { addPNote("MARENDAFT", _case, today, null) }
    if (sc.ctCAAProbates.cfDecRegRevRelationAfterWill == "Y") { addPNote ("RRRAREV", _case, today, null) }

    //Applicant Specific PNotes (changed 16/9/2018 kisnum as fields moved to person profiles by kcsvas)
    for (Party app in casePtys.findAll{(it.partyType == 'PEX') || (it.partyType =='PAD') || (it.partyType == 'PAPPR')}){
      if(app.partyType == "PAD" || app.partyType == "PAPPR")
      {
        if(app.cfAuthToMakeApp != "NOA")
        {
          addPNote("AP", _case, today, null)
        }
      }

      if(app.person.profiles[0]){
        if(app.person.profiles[0].cfIsAppNameInWillSame && app.person.profiles[0].cfIsAppNameInWillSame == "N") { addPNote("NADW", _case, today, null) 		}
        if(app.person.profiles[0].cfAppNameSameDeathCert && app.person.profiles[0].cfAppNameSameDeathCert == "N") { addPNote("NADDC", _case, today, null) }
      }}

    //Other Executor Specific PNotes (changed 28/9/2018 kisnum )
    for (Party exec in casePtys.findAll{(it.partyType == 'NPEX') || (it.partyType == 'NPAD')}){ addPNote("AEW", _case, today, exec.person.profiles[0].cfExecutorNotApply) }

    // Generic Y/N choice PNotes
    if (dec.person.cfHasAnotherName == "Y") { addPNote("AKA", _case, today, null) }
    if (sc.ctCAAProbates.cfHasDeathCertificate == "N") { addPNote("NDC", _case, today, null) }
    if (sc.ctCAAProbates.cfIsCertifiedTranslation && sc.ctCAAProbates.cfIsCertifiedTranslation == "N") { addPNote("NTWC", _case, today, null) }
    if (sc.ctCAAProbates.cfIsDCNameSame == "N") { addPNote("DNDCD",  _case, today, null) }
    if (sc.ctCAAProbates.cfIsLateAddressWillAddressSame == "N") { addPNote("DDA", _case, today, null) }
    //if (sc.ctCAAProbates.cfAppBeneAnother == "Y") { addPNote("AP", _case, today, null) }
    //if (sc.ctCAAProbates.cfIsAppNameInWillSame && sc.ctCAAProbates.cfIsAppNameInWillSame == "N") { addPNote("NADW", _case, today, null) }
    //if (sc.ctCAAProbates.cfAppNameSameDeathCert && sc.ctCAAProbates.cfAppNameSameDeathCert == "N") { addPNote("NADDC", _case, today, null) }
    if (sc.ctCAAProbates.cfOtherPersonNotAppReseal == "Y") { addPNote("APNARA", _case, today, null) }
    //if (sc.ctCAAProbates.cfOtherExecutorsNamed && sc.ctCAAProbates.cfOtherExecutorsNamed == "Y") { addPNote("AEW", _case, today, null) }
    if (sc.ctCAAProbates.cfOrderFromSACAT == "Y") { addPNote("CO", _case, today, null) }
    //if (sc.parties.cfAuthToMakeApp != "NOA" && sc.category != "510100") { addPNote("AP", _case, today, null) }


    // Assets & Liabilities
    _case.ctProbateAssets.findAll{it.cfProbateAssetType == "REAL" && it.cfValuedBy != "VG"}.each{ addPNote("REALNVG", _case, today, null) }
    _case.ctProbateAssets.findAll{it.cfProbateAssetType == "MVEH" && it.cfValuedBy == "AE"}.each{ addPNote("MVEHVAE", _case, today, null) }
    _case.ctProbateAssets.findAll{it.cfProbateAssetType == "PTRUST" && it.cfValuedBy == "AE"}.each{ addPNote("PTRUSTVAE", _case, today, null) }
    _case.ctProbateAssets.findAll{it.cfProbateAssetType == "EINT" && it.cfValuedBy == "AE"}.each{ addPNote("EINTVAE", _case, today, null) }
    //_case.ctProbateAssets.findAll{it.cfProbateAssetType == "OTHERPROP" && it.cfValuedBy == "AE"}.each{ addPNote("OPVAE", _case, today, null) }
    _case.ctProbateAssets.findAll{it.cfProbateAssetType == "PERS" && it.cfValuedBy == "AE"}.each{ addPNote("FURN", _case, today, null) }
    _case.ctProbateAssets.findAll{it.cfProbateAssetType == "OTHERA" && it.cfValuedBy == "AE"}.each{ addPNote("OAVAE", _case, today, null) }

    //Deceased specific PNotes
    if(dec.person.address.country != "AU"){ addPNote("OLT", _case, today, null) }

    if(dec.person.profiles[0]){
      if(dec.person.profiles[0].cfIsWillNameSame && dec.person.profiles[0].cfIsWillNameSame == "N") { addPNote("DNWD", _case, today, null) }

      if( dec.person.profiles[0].dateOfDeath){
        days = today - dec.person.profiles[0].dateOfDeath
        if(days < 28){ addPNote("DOD28", _case, today, null) }
      }
    }

    break;

  case "520000": // Probate Caveat
    if (sc.ctCAAProbates.cfHasDeathCertificate == "N") { addPNote("NDC", _case, today, null) }
    if (dec.person.cfHasAnotherName == "Y") { addPNote("AKA", _case, today, null) }
    if (dec.person.collect("Profiles").first().cfKnownDOD == "UKN") { addPNote("UNKDOD", _case, today, null) }
    break;

  case "530000": // Originating Application
    // Will specific PNotes
    //if (sc.ctCAAProbates.cfHasOriginalWill == "CHOW") { addPNote("CHOW", _case, today, null) }
    for (Document will in caseDocs.findAll{it.docDefNumber == "519902"}) {
      if (will.cAADocuments.cfIsWillDated == "N") { addPNote("WNDAT", _case, today, null) }
      if (will.cAADocuments.cfIsWillDated == 'P') { addPNote("WPDAT", _case, today, null) }
    }
    // Assets & Liabilities
    _case.ctProbateAssets.findAll{it.cfProbateAssetType == "REAL" && it.cfValuedBy == "OTH"}.each{ addPNote("REALNVG", _case, today, null) }
    _case.ctProbateAssets.findAll{it.cfProbateAssetType == "MVEH" && it.cfValuedBy == "AE"}.each{ addPNote("MVEHVAE", _case, today, null) }
    _case.ctProbateAssets.findAll{it.cfProbateAssetType == "PTRUST" && it.cfValuedBy == "AE"}.each{ addPNote("PTRUSTVAE", _case, today, null) }
    _case.ctProbateAssets.findAll{it.cfProbateAssetType == "EINT" && it.cfValuedBy == "AE"}.each{ addPNote("EINTVAE", _case, today, null) }
    //_case.ctProbateAssets.findAll{it.cfProbateAssetType == "OTHERPROP" && it.cfValuedBy == "AE"}.each{ addPNote("OPVAE", _case, today, null) }
    _case.ctProbateAssets.findAll{it.cfProbateAssetType == "PERS" && it.cfValuedBy == "AE"}.each{ addPNote("FURN", _case, today, null) }
    _case.ctProbateAssets.findAll{it.cfProbateAssetType == "OTHERA" && it.cfValuedBy == "AE"}.each{ addPNote("OAVAE", _case, today, null) }
    break;

  case "540000": // Probate subpoena
    if (dec.person.cfHasAnotherName == "Y") { addPNote("AKA", _case, today, null) }
    if (dec.person.address.country != "AU") { addPNote("OLT", _case, today, null) }
    break;

  case "550000": // Probate Will Deposit
    for (Document will in caseDocs.findAll{it.docDefNumber == "519902"}) {
      if (will.cAADocuments.cfIsWillDated == "N") { addPNote("WNDAT", _case, today, null) }
      if (will.cAADocuments.cfIsWillDated == 'P') { addPNote("WPDAT", _case, today, null) }
    }
    break;

  case "560000": // Probate renunciation
    if (sc.ctCAAProbates.cfHasOriginalWill == "CHOW") { addPNote("CHOW", _case, today, null) }
    break;

  case "570000": // Probate Miscellaneous Application
    if (dec && dec.person.cfHasAnotherName == "Y") { addPNote("AKA",_case,today, null) }
    if (sc.ctCAAProbates && sc.ctCAAProbates.cfHasOriginalWill == "CHOW") { addPNote("CHOW", _case, today, null) }
    for (Document will in caseDocs.findAll{it.docDefNumber == "519902"}) {
      if (will.cAADocuments.cfIsWillDated == "N") { addPNote("WNDAT", _case, today, null) }
      if (will.cAADocuments.cfIsWillDated == 'P') { addPNote("WPDAT", _case, today, null) }
    }
    break;

} // End switch

// Function for adding Probate Note
private void addPNote(String fType, Case fCase, Date fDate, String fInfo) {
  withTx{
    CtProbateNotes pnote = new CtProbateNotes()
    pnote.cfType = fType
    pnote.cfNoteDate = fDate
    if (fInfo) pnote.cfAdditionalInfo = fInfo
    // Returning Requisition text from LUList Attribute
    String reqText
    attribute = com.sustain.lookuplist.model.LookupItem.getItem("CL_PROBATE_NOTE_TYPE", fType).getAttributes()?.find{it.name == 'PNOTE'}
    String attText = attribute.getValue()
    if (attribute.getAttributeType() == "VTL") {
      StringWriter swOut = new StringWriter()
      String logError = "logError"
      Velocity.init()
      VelocityContext context = new VelocityContext()
      context.put("case", _case)
      context.put("sysPropTool", SystemProperty)
      Velocity.evaluate(context, swOut, logError, attText)
      reqText = swOut.getBuffer().toString()
    } else {
      reqText = attText
    }
    pnote.cfMemo = reqText
    pnote.case = fCase
    pnote.cfStatus = "O"
    fCase.add(pnote)
    pnote.saveOrUpdate()
  }
}



