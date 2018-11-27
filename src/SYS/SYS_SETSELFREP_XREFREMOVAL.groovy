/*
	Adjusted by KCSMBF 10/08/17 after discussion with dsmith regarding this BR not working
	as intended.  For reference, I reversed the lentity and rentity checks on Lines 1 & 2,
	and also changed _ccr.lid -> _ccr.rid on Line 4 to account for this reversal. Now works.
	Update: 25/08 Maybe not.  Checking with Amy.
	Update: 08/09 Reinstated original for testing Case Init XREF issue.
	Update: 20/09 Activated both as Add Case Assignment doesn't work with original
*/
if (_ccr.lentity.toString() == "com.sustain.cases.model.CaseAssignment" && _ccr.type=="REPRESENTEDBY") {
  if (_ccr.rentity.toString() == "com.sustain.person.model.Person") {
    for (Party p in _ccr.associatedCase.parties) {
      if (p.person.id == _ccr.rid) {
        p.selfRepresented = "N";
      }
    }
  }
}

// Original
if (_ccr.rentity.toString() == "com.sustain.cases.model.CaseAssignment" && _ccr.type=="REPRESENTEDBY") {
  if (_ccr.lentity.toString() == "com.sustain.person.model.Person") {
    for (Party p in _ccr.associatedCase.parties) {
      if (p.person.id == _ccr.lid) {
        p.selfRepresented = "N";
      }
    }
  }
}


