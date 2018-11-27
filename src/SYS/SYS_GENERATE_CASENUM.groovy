/*
	Change Management:
 	Written by: KISBAC
	Change Log:	KCSMBF 06/11/2017 Redid SSED to account for final LUList numbering structure guidelines.
				kcsmbf 25/09/18 Added uppercase force for manually entered case numbers
*/

if (StringUtils.isBlank(_case.caseNumber)) {
  String caseType = _case.caseType

  switch (true) {
    case caseType == "100000":
      _case.caseNumber = "ADM" + "-" + DateUtil.format(new Date(), "yy") + "-" + CaseCounter.increment("case.caseNumber.lastEntry.Pool1").toString().padLeft(6,"0")
      break

    case caseType == "311110":
      _case.caseNumber = "YCRIM" + "-" + DateUtil.format(new Date(), "yy") + "-" + CaseCounter.increment("case.caseNumber.lastEntry.Pool3").toString().padLeft(6,"0")
      break

    case caseType == "321110":
      _case.caseNumber = "YCIV" + "-" + DateUtil.format(new Date(), "yy") + "-" + CaseCounter.increment("case.caseNumber.lastEntry.Pool3").toString().padLeft(6,"0")
      break

    case caseType == "411110":
      _case.caseNumber = "CIV" + "-" + DateUtil.format(new Date(), "yy") + "-" + CaseCounter.increment("case.caseNumber.lastEntry.Pool4").toString().padLeft(6,"0")
      break

    case caseType == "400000":
      _case.caseNumber = "CIV" + "-" + DateUtil.format(new Date(), "yy") + "-" + CaseCounter.increment("case.caseNumber.lastEntry.Pool4").toString().padLeft(6,"0")
      break

    case caseType == "500000":
      _case.caseNumber = "PROB" + "-" + DateUtil.format(new Date(), "yy") + "-" + CaseCounter.increment("case.caseNumber.lastEntry.Pool5").toString().padLeft(6,"0")
      break


    case caseType == "700000":
      _case.caseNumber = "SHO-" + DateUtil.format(new Date(), "MMyy") + "-" + CaseCounter.increment("case.caseNumber.lastEntry.Pool7").toString().padLeft(5,"0")
      break

    case caseType == "911110":
      _case.caseNumber = "CRIM" + "-" + DateUtil.format(new Date(), "yy") + "-" + CaseCounter.increment("case.caseNumber.lastEntry.Pool9").toString().padLeft(6,"0")
      break
  }
} else {
  _case.setCaseNumber(_case.caseNumber.toUpperCase())  // Force uppercase if someone manually enters a case number (e.g. Probate Misc Application)
}



