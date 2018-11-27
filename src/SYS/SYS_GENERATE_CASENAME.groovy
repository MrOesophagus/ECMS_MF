/*
Change Management:
Written by: kcsmbf 05/09/17
Change Log: kcsmbf 23/10/17 Added Youth Civil & tried optimising Civil code (not fully tested)
							Updated number for Probate matters
			kisbac 13/02/18 Updated to used new transient field personDisplayCaseName to have the last name in uppercase
							Rewrote Probate
			kisnum 26/02/18 Added Application type 420000
			kcsmbf 22/03/18 Rewrote rule to include all case.parties, not just on subCase[0]
			kcsmbf 29/03/18 Added "otherwise" code into Probate caption
			kistah 15/06/18 Added Case Name for Will Deposit
			kisduh 15/06/18 Added Case Name for Renunciation
			kistah 25/06/18 Change naming for Miscellaneous Application to have if deceased "In the estate of "
			kcsmbf 25/07/18 Reworked CAR title
			kiseeg 15/08/18 Added Copy of Record Title / 22 Aug changed to Referred Case Name / 22/8/18 kcsmbf tweaked COR code
*/

String caption = "BUG: No caption set for " + _case.caseTypeLabel + " " + _case.filingTypeLabel + "!"
String shortName
List<Party> LHS = new ArrayList()
List<Party> RHS = new ArrayList()

switch (_case.caseType) {
  case ["100000"]:
    if (_case.filingType == "110000") {
      requestor = (_case.collect("SubCases").first().ownersResponsibility == "N") ? _case.collect("Assignments").first().person.personCaseDisplayName : _case.collect("Parties").first().person.personCaseDisplayName
      caption = "Case Access Request for " + requestor
    }
    else if (_case.filingType == "120000") {
      SubCase scin = _case.collect("subCases").first()
      requested = Case.get(scin.collect("cAAReusableFields").first().cfString01)?.caseName
      caption = "Copy Of Record Request for " + requested
    }
    else if (_case.filingType == "130000") {
      requested = _case.memo
      caption = "Grant Download for " + requested
    }
    break;

  case ["400000"]:	// Civil: PLAIN(s) + " v. " + DEF(s)
    LHS = _case.parties.findAll{ ["PLAIN","APP","APLNT"].contains(it.partyType) }.person.personCaseDisplayName
    RHS = _case.parties.findAll{ ["DEF","RES"].contains(it.partyType) }.person.personCaseDisplayName
    caption = LHS[0]
    if (LHS.size() > 1) caption += " and others"
    caption += " v. " + RHS[0]
    if (RHS.size() > 1) caption += " and others"
    break;

  case "431110":	// FINAL Notice of Claim tesing KISNUM
    LHS = _case.collect("Parties[PartyType == 'PLAIN']").person.personCaseDisplayName
    RHS = _case.collect("Parties[PartyType == 'DEF']").person.personCaseDisplayName
    caption = "FINAL Notice of Claim for " + LHS[0]
    if (LHS.size() > 1) caption += " and others"
    caption += " v. " + RHS[0]
    if (RHS.size() > 1) caption += " and others"
    break;

  case "500000":  // Probate: In the Estate of
    if (["550000", "560000"].contains(_case.filingType)) {
      Party wm = _case.collect("Parties[PartyType == 'WM']").first()
      caption = wm.person.personCaseDisplayName
      shortName = caption
      for (PersonAKA other in wm.person.personAKAs.findAll{it.akaType == 'AKA'}) {
        caption += ' otherwise '
        if (other.firstName) caption += other.firstName + ' '
        if (other.middleName) caption += other.middleName + ' '
        if (other.lastName) caption += other.lastName
      }
    } else if (_case.filingType == "570000") {
      List<Party> names = _case.collect("Parties[PartyType == 'DEC']")?:_case.collect("Parties[PartyType == 'APP' || PartyType == 'PP']")
      caption = (names[0].partyType == "DEC") ? "In the estate of " : ""
      caption += names[0].person.personCaseDisplayName
      shortName = caption
      for (PersonAKA other in names[0].person.personAKAs.findAll{it.akaType == 'AKA'}) {
        caption += ' otherwise '
        if (other.firstName) caption += other.firstName + ' '
        if (other.middleName) caption += other.middleName + ' '
        if (other.lastName) caption += other.lastName
      }
    } else {
      Party dec = _case.collect("Parties[PartyType == 'DEC']").first()
      caption = 'In the Estate of ' + dec.person.personCaseDisplayName
      shortName = caption
      for (PersonAKA other in dec.person.personAKAs.findAll{it.akaType == 'AKA'}) {
        caption += ' otherwise '
        if (other.firstName) caption += other.firstName + ' '
        if (other.middleName) caption += other.middleName + ' '
        if (other.lastName) caption += other.lastName
      }
    }
    break;

  case "700000":	// Sheriff's Office
    if (_case.filingType == "710000") { // Sheriff's Office External Document Service
      RHS = _case.collect("Parties[PartyType == 'REC']").person.personCaseDisplayName
      caption = "Service Against " + RHS[0]
      if (RHS.size() > 1) caption += " and others"
    } else if (_case.filingType == "720000") { // Warrant of Possession & Warrant of Sale
      LHS = _case.collect("Parties[PartyType == 'PLAIN']").person.personCaseDisplayName
      RHS = _case.collect("Parties[PartyType == 'DEF']").person.personCaseDisplayName
      caption = LHS[0]
      if (LHS.size() > 1) caption += " and others"
      caption += " vs. " + RHS[0]
      if (RHS.size() > 1) caption += " and others"
    }
    break;
}  // Closes switch

_case.caseShortName = (shortName) ?: caption
if (!_case.caseName || _update == true) {
  _case.caseName = caption
}