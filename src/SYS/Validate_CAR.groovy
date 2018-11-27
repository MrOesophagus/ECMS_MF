/*
	Change Management
	Written by: kcsmbf 02/08/18
	Change Log:	kcsmbf 03/09/18 Added county (as State) check for non-AU countries
				kcsmbf 03/11/18 Added usePCode hardstop msg
				kcsmbf 07/11/18 Set Telephone and email to only validate if selfRep
*/

String errorMsg
String hardStop
List<String> errors
SubCase sc = _cse.collect("SubCases").first()

if (sc.ownersResponsibility == "Y") {
  errors = callErrors(sc.collect("Parties"))
} else {
  lrprsn = _cse.collect("Assignments").first().person
  DirOrgUnit dou = DirOrgUnit.getByCode(lrprsn.personCode)
  if (!dou) {
    hardStop = "You have entered an incorrect LCODE (please select from the search results)."
  } else if (!lrprsn.personCode) {
    hardStop = "You must enter your organisation's LCODE."
  } else if (!sc.collect("CAAReusableFields").first().cfChoice01) {
    hardStop = "You must answer the question whether you wish to enter a PCODE or not."
  } else {
    errors = callErrors(sc.collect("Parties[SelfRepresented == 'N']"))
  }
}

private callErrors(Object fPtys) {
  List<String> fErrors = []
  int cntr = 0
  for (Party pty in fPtys) {
    if (_cse.collect("SubCases").first().ownersResponsibility == "Y" && cntr == 1) {
      break;
    } else {
      Person prsn = pty.person
      switch (pty.partySubType) {
        case "INDV":
          if (!prsn.firstName && !fErrors.contains("a first name")) fErrors.add("a first name")
          if (!prsn.lastName && !fErrors.contains("a last name")) fErrors.add("a last name")
          break;

        case "CORP":
          if (!prsn.organizationName && !fErrors.contains("an organisation name")) fErrors.add("an organisation name")
          if (_cse.assignedTrack == "400000" && !prsn.cfOrgType && !fErrors.contains("an organisation type")) fErrors.add("an organisation type")
          break;

        case "REG":
          if (!prsn.personCode && !fErrors.contains("a Regular Party code")) fErrors.add("a Regular Party code")
          break;
      }
      if (!prsn.addresses[0]?.address1 && !fErrors.contains("an address")) fErrors.add("an address")
      if ((!prsn.addresses[0]?.zip || !prsn.addresses[0]?.city) && !fErrors.contains("a Postcode/Suburb")) fErrors.add("a Postcode/Suburb")
      if (!prsn.addresses[0]?.state && prsn.addresses[0]?.country == "AU" && !fErrors.contains("a State")) fErrors.add("a State")
      if (!prsn.addresses[0]?.county && prsn.addresses[0]?.country != "AU" && !fErrors.contains("a State")) fErrors.add("a State")
      if (_cse.collect("SubCases").first().ownersResponsibility == "Y") { // Only check if selfRep
        if (!prsn.telephones[0]?.telephoneNumber && !fErrors.contains("a telephone number")) fErrors.add("a telephone number")
        if (!prsn.contacts[0]?.contact && !fErrors.contains("an email address")) fErrors.add("an email address")
      }
    }
    cntr = cntr + 1
  }
  return fErrors
}

if (hardStop) {
  addError(hardStop)
} else if (errors) {
  errorMsg = (sc.ownersResponsibility == "Y") ? "You must enter " : "One of the parties you represent is missing "
  for (i = 0; i < errors.size() - 1; i++) {
    errorMsg += errors[i]
    if (errors.size() > 2) errorMsg += ", "
  }
  errorMsg += " and " + errors.last()
  addError(errorMsg)
}




