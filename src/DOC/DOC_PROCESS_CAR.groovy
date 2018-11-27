/*
	Change Management
	Written by: kcsmbf 07/11/18
	Change Log:
*/

org.apache.velocity.tools.generic.SortTool sorter = new org.apache.velocity.tools.generic.SortTool()

Case cas = _doc.case
Date now = new Date()
List<Party> xrefs = _doc.crossReferencedParties
Set<DomainObject> objectsToSave = new HashSet<>()

docTrack = _doc.trackings.find{ it.type == "CAR" }
if (docTrack) {
  DirOrgUnit dou = DirOrgUnit.getByCode(docTrack.memo)
  if (!dou) { addError("ERROR: You have entered an invalid LCODE.") }
  // Create CaseAssignment and Person records
  Person prsn = new Person()
  prsn.with{
    setOrganizationName(dou.orgUnitName)
    setPersonCode(docTrack.memo)
    setPersonId(dou.id)
  }
  CaseAssignment ca = new CaseAssignment()
  cas.assignments.add(ca)
  ca.with{
    setAssignmentRole("LAWFIRM")
    setDateAssigned(now)
    setStatus("CUR")
    setStatusDate(now)
    setCase(cas)
    setPerson(prsn)
  }
  // Add addresses
  List<DirAddress> sortedAddresses = sorter.sort(dou.addresses, ["mailingAddress:desc"])
  for (DirAddress da in sortedAddresses) {
    Address a = new Address()
    a.with{
      setAddressType(da.addressType)
      setAddress1(da.address1)
      setAddress2(da.address2)
      setAddress3(da.address3)
      setCity(da.city)
      setState(da.state)
      setZip(da.zip)
      setAssociatedPerson(prsn)
    }
    a.country = (["AU", "Australia"].contains(da.country)) ? "AU" : da.country
    prsn.addresses.add(a)
  }
  // Add telephone numbers
  List<DirTelephone> sortedPhones = sorter.sort(dou.telephones, "telephoneType:asc")
  for (DirTelephone dt in sortedPhones) {
    Telephone t = new Telephone()
    t.with{
      setTelephoneType(dt.telephoneType)
      setTelephoneNumber(dt.telNumber)
      setAssociatedPerson(prsn)
    }
    prsn.telephones.add(t)
  }
  // Add emails
  CaseContact cc1 = new CaseContact()
  cc1.with{
    setType("E")
    setContact(dou.email)
    setAssociatedPerson(prsn)
  }
  prsn.contacts.add(cc1)
  ca.saveOrUpdate() // Need to save before adding XRefs
  // Add Filed By XRefs
  xrefs.each{ pty ->
    pty.addCrossReference(ca, "REPRESENTEDBY")
    pty.setSelfRepresented("N")
  }
}




