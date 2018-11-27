/*
	Change Management
 	Written by: kcsmbf 06/04/18
	Change Log:	kcsmbf 07/11/18 Added NoA PCODE processing
*/

Date now = new Date()

if (_invite.party) {
  _invite.party.person.cf_PortalUserId = _invite.appUserId
} else {
  caprsn = _invite.caseAssignment.person
  caprsn.cf_PortalUserId = _invite.appUserId
  docTrack = _invite.case.collect("Documents[docDefNumber == '110101']").collect("Trackings[memo == '${caprsn.personCode}' && status == 'PEND']").first()
  if (docTrack?.name) { // Doc Tracking has PCODE value
    DirPerson dirPrac = DirPerson.getByCode(docTrack.name)
    if (dirPrac) {
      caprsn.with{
        setOrganizationName(dirPrac.parentOrgUnit?.orgUnitName)
        setFirstName(dirPrac.firstName)
        setLastName(dirPrac.lastName)
        setPersonCode(docTrack.name)
        setPersonId(dirPrac.id)
      }
      caprsn.middleName = (dirPrac.middleName) ?: null
      if (dirPrac.email) {
        caprsn.contacts.findAll{ it.isCurrentEmail }.each{ it.setEffectiveTo(now) }
        CaseContact cc = new CaseContact()
        cc.with{
          setType("E")
          setContact(dirPrac.email)
          setAssociatedPerson(caprsn)
        }
        caprsn.contacts.add(cc)
      }
      caprsn.saveOrUpdate()
      docTrack.setStatus("COMP") // Completing the Tracking record so I don't use it again.
    }
  }
}