/*
	Change Management
 	Written by: kiseeg 26/10/18
	Change Log: kcsmbf 02/11/18 Added changeName input variable & minor tweaks
				kcsmbf 13/11/18 Changed DPP to copyPlainFields as CopyFrom caused address/telephones to be lost
*/

org.apache.velocity.tools.generic.SortTool sorter = new org.apache.velocity.tools.generic.SortTool()

boolean validAsst
boolean isOrg = false
Date today = new Date()
Person cPerson = _casAssign.person

String changeTel = "N"
String changeEmail = "N"
String changeName = "N"
String changeAdd = "N"
if (_update?.contains("UTEL")) { changeTel = "Y"}
if (_update?.contains("UEML")) { changeEmail = "Y"}
if (_update?.contains("UNME")) { changeName = "Y"}

if (_casAssign.assignmentRole == "LAWFIRM") {
  dou = DirEntry.getByCode(cPerson.personCode)
  validAsst = dou ? true : false
  isOrg = true
} else if (_casAssign.assignmentRole == "LAW") {
  dpp = DirPerson.getByCode(cPerson.personCode)
  validAsst = dpp ? true : false
}
if (validAsst) {
  if (changeName == "Y") {
    if (isOrg) {
      cPerson.copyPlainFieldsFrom(dou)
    } else {
      cPerson.copyPlainFieldsFrom(dpp)
      if (!cPerson.personCode) cPerson.personCode = dpp.code
    }
  }
  dp = (isOrg) ? dou : dpp
  if (changeAdd == "Y") {
    cPerson.addresses.each{ it.effectiveTo = DateUtil.getYesterday() }
    List<DirAddress> sortedAddresses = sorter.sort(dp.addresses, ["mailingAddress:desc", "id:desc"])
    for(DirAddress da in sortedAddresses) {
      Address a = new Address()
      a.setAssociatedPerson(cPerson)
      a.setAddressType(da.getAddressType())
      a.setAddress1(da.getAddress1())
      a.setAddress2(da.getAddress2())
      a.setCity(da.getCity())
      a.setState(da.getState())
      a.setZip(da.getZip())
      cPerson.addresses.add(a)
    }
  }
  if (changeTel == "Y") {
    cPerson.telephones.each{ it.effectiveTo = DateUtil.getYesterday() }
    List<DirTelephone> sortedPhones = sorter.sort(dp.telephones, "telephoneType:asc")
    for (DirTelephone dt in sortedPhones) {
      Telephone t = new Telephone()
      t.setAssociatedPerson(cPerson)
      t.setTelephoneType(dt.getTelephoneType())
      t.setTelephoneNumber(dt.getTelNumber())
      cPerson.getTelephones().add(t)
    }
  }
  if  (changeEmail == "Y") {
    cPerson.contacts.each{ it.effectiveTo = DateUtil.getYesterday() }
    CaseContact cc1 = new CaseContact()
    cc1.type = "E"
    cc1.contact = dp.email
    cc1.setAssociatedPerson(cPerson)
    cPerson.getContacts().add(cc1)
    if (dp.altEmail && dp.altEmail != "") {
      CaseContact cc2 = new CaseContact()
      cc2.type = "E"
      cc2.contact = dp.altEmail
      cc2.setAssociatedPerson(cPerson)
      cPerson.getContacts().add(cc2)
    }
  }
  cPerson.saveOrUpdate()
  _casAssign.updateReason = ""

} else {
  addError("ERROR: Can not retrieve Legal Representative from the Directory - please contact Service Desk")
}






