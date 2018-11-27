/*
	Change Management
 	Written by: kcsmbf 23/03/18
	Change Log:	kcsmbf 08/06/18 Added disabling case Access code
				kcsmbf 26/04/18 Added SelfRep = N to Represented XRef'd parties
				kcsmbf 03/10/18 Reworked error messages for legal reps
				kcsmbf 10/10/18 Added check for parties with no lawyer but still have SelfRep = N
				kiseeg 19/10/18 Added check to ensure Legal reps are xref to a party
				kiseeg 08/11/18 Added check to ensure P/L code is valid
*/


// Ensure Practitioners and Law Firms are valid Directory entries and not free text
List<String> MustBeInDir = ["LAW", "LAWFIRM"]
if (MustBeInDir.contains(_ca.assignmentRole)) {
  if (_ca?.person?.personId == null) {
    addError("Legal Representative must be selected from search results")
  } else { // Ensure Practitioners have an LCODE
    if (!_ca.person.personCode.substring(0, 1).equalsIgnoreCase("L")) {
      DirPerson dp = DirPerson.getByCode(_ca.person.personCode)
      if (dp && !dp.parentOrganization) {
        addError("The PCODE you have have entered is not attached to an LCODE. You must contact the Law Society and get this resolved before you can lodge a case.")
      }
    }
  }
  DirEntry dp = DirEntry.getByCode(_ca.person.personCode)
  if (!dp) {
    addError("You have entered an invalid P/L code")
  }
  else {
    if (dp.id != _ca.person.personId) {
      addError("Please select the P/L code from the search list")
    }
  }
  if ((_ca.case.filingType != '110000') && (_ca.case.filingType != '120000')) {
    if (_ca.findByXRef("Party").size() == 0) {
      addError("Please select the party or parties that this relates to")
    }
  }
}

// Remove Case Access when not Current
if (["LAW","LAWFIRM"].contains(_ca.assignmentRole) && _ca.status != "CUR") {
  _ca.ce_allowPublicAccess = "N"
}

// Validate Self Rep flag
if (["LAW","LAWFIRM"].contains(_ca.assignmentRole) && _ca.status == "CUR") {
  for (Party pty in _ca.findByXRef("Party", "REPRESENTEDBY")) {
    pty.selfRepresented = "N"
  }
}
if (["LAW","LAWFIRM"].contains(_ca.assignmentRole) && _ca.status != "CUR") {
  for (Party pty in _ca.findByXRef("Party", "REPRESENTEDBY")) {
    hasCurrentLawyer = false
    for (CaseAssignment cass in pty.findByXRef("CaseAssignment", "REPRESENTEDBY")) {
      if (!hasCurrentLawyer && cass.status == "CUR") {
        hasCurrentLawyer = true
      }
    }
    if (!hasCurrentLawyer) pty.selfRepresented  = "Y"
  }
}
_ca.case.parties.findAll{it.subCase.filingType != "110000" && it.selfRepresented == "N" && !it.findByXRef("CaseAssignment", "REPRESENTEDBY")}.each{it.selfRepresented = "Y"}




