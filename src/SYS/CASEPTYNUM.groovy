/*
	Change Management
 	Written by: kcsmbf 03/08/17
	Change Log:	kcsmbf 18/12/17 Fixed code to retain Party Number if they're an existing party (e.g. appeal)
				kcsmbf 25/07/18 Added code to ignore CAR (110000) cases
				kcsmbf 09/10/18 Reworked ptynumsmax to allow for > 10 as it's a string field
*/

if (_case.filingType != "110000") {
  List<String> typearray = _case.collect("Parties.partyType").distinct()
  for (item in typearray) {
    ptys = _case.collect("Parties[PartyType=='${item}']")
    //int ptynumsmax = (ptys.partyNumber.max()?:0).toInteger()
    List<Integer> ptyNums = []
    ptys.each{ptyNums.add(it.partyNumber?.toInteger()?:0)}
    java.lang.Integer ptynumsmax = ptyNums.max()
    for (pty in ptys) {
      if (!pty.partyNumber) {
        currentpty = CollectionUtil.first(ptys.findAll{it.personIdentifier.id == pty.personIdentifier.id})
        if (currentpty && currentpty != pty) {  // Need the second bit for CaseInit
          pty.partyNumber = currentpty.partyNumber
        } else {
          pty.partyNumber = ++ptynumsmax
        }
      }
    }
  }
}

/* 	The below code is to replace the rule above to allow for forcing a party
	to become number 1 (e.g. force Defendant 2 to become Defendant 1) and
	reshuffle the rest of the party numbers.
	NB: Not fully tested.
	Currently not in use because of potential (untested) issues with documents
	generated before forcing the number change.  If a previous doc refers to
	Defendant 2 and we force them to be Defendant 1, not sure if the document
	will update correctly.
	Maybe we just don't give them the ability to change it?
	Need to add in the currentpty bits 'n' pieces.
	KCSMBF 18/09/17

typearray = _case.collect("Parties.partyType").distinct()
typearray.each {item->
  ptys = _case.collect("Parties[PartyType=='${item}']")
  primary = (ptys.findAll {it.cfHasResponded == true})?:null  // Replace with new field e.g. cfForcePrimary
  if (primary.size() > 1) addError("You can not set more than one party as the primary " + item)
  int ptynum = 2
  int ptynumsmax = (ptys.partyNumber.max()?:0).toInteger()
  ptys.each {pty->
    if (!primary) {
      if (!pty.partyNumber) pty.partyNumber = ++ptynumsmax /* Will this overwrite existing No 1 though? */ /*
    }
    else {
      if (pty == primary) pty.partyNumber = 1
      else pty.partyNumber = ptynum++
    }
  }
}
*/





