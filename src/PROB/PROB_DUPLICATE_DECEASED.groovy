/*
	Change Management
	Written by:	kcsmbf 02/06/18
	Change Log:	kisnum 13/06/18 added new method for checking current joinders, added will maker as party type
				kcsmbf 26/06/18 Adjusted decCases find conditions, and alreadyJoined code
				kcsmbf 28/06/18 Added Examining Officer code
				kcsmbf 10/07/18 Reworked EO code as it was bugging on newJoin
				kcsmbf 24/07/18 Added Caveat init tasks if there is death cert
				kiseeg 26/07/18 Removed the additional Caveat init tasks and added to workflow
				kcsmbf 01/08/18 Reworked EO copying functionality
				kcsmbf 09/08/18 Added css to grant if there's a live caveat functionality
				kcsmbf 28/08/18 Added nullsafe check for decWM
				kcsmbf 26/09/18 Added OA S9 check
*/

_retVal = false
boolean copiedEO = false
Date today = new Date()
List<Case> decCases = []
Person decWM = _case.parties.find{["DEC", "WM"].contains(it.partyType)}?.person
if (decWM) {
  List<Person> decPersons = Person.find('lastName','=', decWM.lastName, 'firstName','=', decWM.firstName, 'middleName', '=', decWM.middleName, 'id', '!=', decWM.id)

  if (!decPersons.isEmpty()) {
    decPersons.each{prsn ->
      prsn.cases.findAll{cse ->
        cse.caseType == "500000" && cse != _case && cse.parties.find{pty ->
          ["DEC", "WM"].contains(pty.partyType) && decPersons.contains(pty.person)
        }
      }
      .each{decCases.add(it)}
    }
  }

  // Loop through matched cases to join them (if necessary) and copy Examining Officer if needed
  if (!decCases.isEmpty()) {
    for (Case cse in decCases) {
      if (cse.joinders) {
        for (joinder in cse.joinders) {
          boolean alreadyJoined = joinder.collect("items").find{ it.case == _case } ? true : false
          if (!alreadyJoined) {
            joinder.addCase(_case)
            _retVal = true
          }
          if (!copiedEO) {
            copiedEO = copyEO(cse, today)
          }
        }
      } else {
        newJoin = CaseJoinder.createJoinder(_joinderName, _joinderType, CollectionUtil.toArray(new Case[0], decCases, _case))
        newJoin.saveOrUpdate()
        if (!copiedEO) {
          copiedEO = copyEO(cse, today)
        }
        _retVal = true
      }
    }
  }
}

// Setting Live status for Caveat
SubCase sc = _case.collect("SubCases").first()
if (_case.filingType == '520000' && sc.ctCAAProbates.cfHasDeathCertificate == 'Y') {
  sc.status = "LIVE"
  sc.statusDate = today
}

// Setting Case Special Status on this case if it's a Grant and there's a Live Caveat that just got joined
if (_retVal && (_case.filingType == "510000" || (_case.filingType == "530000" && _case.collect("SubCases").first().ctCAAProbates.cfApplicationbySummons == "Appl"))) {
  for (joinder in _case.joinders) {
    if (joinder.items.find{it.case.filingType == "520000" && it.case.subCases[0].status == "LIVE"}) {
      CaseSpecialStatus css = new CaseSpecialStatus()
      _case.add(css)
      css.with{
        setStatus("LIVECAVEAT")
        setStartDate(today)
      }
      css.saveOrUpdate()
      _case.saveOrUpdate()
      break
    }
  }
}

private copyEO (Case fCase, Date fDate) {
  boolean copied = false
  Person currentEO = fCase.assignments.find{it.assignmentRole == "EXAMINER" && it.status == "CUR"}?.person
  if (currentEO) {
    Person prsn = currentEO.copy()
    CaseAssignment ca = new CaseAssignment()
    ca.with{
      setCase(_case)
      setAssignmentRole("EXAMINER")
      setPerson(prsn)
      setDateAssigned(fDate)
      setStatus("CUR")
      setStatusDate(fDate)
    }
    _case.assignments.add(ca)
    ca.saveOrUpdate()
    _case.saveOrUpdate()
    copied = true
  }
  return copied
}





