/*
	Change Management
	Written by: kcsmbf 01/08/2018
	Change Log:
*/

SubCase sc = CollectionUtils.first(_cse.subCases)

// Deleting any redundant party/parties created by form complexity
if (sc.ownersResponsibility == "Y" && _cse.assignments) {
  Party pty0 = CollectionUtils.first(sc.parties)
  sc.collect("Parties").minus(pty0).each {
    sc.parties.remove(it)
    it.deleteRemoveFromPeers()
  }
  _cse.assignments.each {
    _cse.assignments.remove(it)
    it.deleteRemoveFromPeers()
  }
} else if (sc.ownersResponsibility == "N") {
  // Checking for Self Rep Party even though Lawyer pathway used
  Party pty0 = sc.parties.find{it.selfRepresented == "Y"}
  if (pty0) {
    sc.parties.remove(pty0)
    pty0.deleteRemoveFromPeers()
  }
}

// Adding caseName to document so it appears on target case FV
Document NoA = sc.documents.find{it.docDefNumber == "110101"}
NoA.cfAdditionalInfo = _cse.caseNumber

sc.saveOrUpdate()
_cse.saveOrUpdate()



