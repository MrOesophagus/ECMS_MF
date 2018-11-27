/*
	Change Management
 	Written by: 28/08/17 kcsmbf
	Change Log:	28/08/17 kcsmbf Original saved into Objective for reference.
				24/11/17 kisnum Added check for numeral in Individual person name.
				26/04/18 kcsmbf Rewrote to allow for LAW & LAWFIRM, XRef only on Party, and set Self Rep if empty.
				09/08/18 kcsmbf Added ignoreList functionality to handle CAR
*/


// Validate if there is a number in Individual Party Name
if(_party.personNameFML.matches(".*\\d+.*") && _party.partySubType == "INDV") {
  addWarning("Individuals name " + _party.personNameFML +  " contains numbers, please check that this is correct before submitting")
}

// Validate Self Rep flag
List<String> ignoreList = ["110000"]  // Filing Types to ignore this validation
if (!ignoreList.contains(_party.subCase.filingType)) {
  List<String> assignments = _party.findByXRef("CaseAssignment", "REPRESENTEDBY")
  if (!assignments.isEmpty()) {
    boolean foundLR = false
    for (CaseAssignment ca in assignments) {
      if (["LAW", "LAWFIRM"].contains(ca.assignmentRole) && ca.status == "CUR") {
        foundLR = true
        break
      }
    }
    if (foundLR) {
      _party.selfRepresented = "N"
    } else {
      _party.selfRepresented = "Y"
    }
  }  else {
    _party.selfRepresented = "Y"
  }
}



