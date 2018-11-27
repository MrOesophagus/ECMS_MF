/*
	Change Management
	Written by: kcsmbf 12/11/18
	Change Log:
*/
import groovy.transform.Field

Case cas = _case
SubCase sc = cas.collect("SubCases").first()
@Field int count = 0

List<String> caseFields
List<String> subCaseFields
List<String> cpFields
List<String> docFields
List<String> docStatFields
List<String> ptyFields
List<String> prsnFields
List<String> prsnProfFields
List<String> prsnAddFields
List<String> prsnAKAFields
List<String> prsnIDFields
List<String> prsnTelFields
List<String> prsnConFields
List<String> asstFields
List<String> caseAddFields

caseFields = ["cfPortalLodgingPartyId", "caseType", "filingType", "category", "caseJurisdiction", "subJurisdiction"]
caseFields += ["filingDate", "status", "statusDate", "location","cfAgreeToAccuracy", "cfAgreeToTerms"]
subCaseFields = ["filingType", "category", "cfJurisdiction", "filingDate", "status", "statusDate"]
cpFields = ["cfHasDeathCertificate", "cfNoDeathCertificate", "cfPlaceOfDeathList", "cfPlaceOfDeathOther"]
docFields = ["dateFiled", "cfPortalLodgingPartyId"]
docStatFields = ["beginDate", "statusType"]
ptyFields = ["partyType", "partySubType"]
prsnFields = ["personId", "personCode", "firstName", "middleName", "lastName", "organizationName", "cfHasAnotherName", "nameExact"]
prsnProfFields = ["cfKnownDOD", "cfDODOnOrAbout", "dateOfDeath"]
prsnAddFields = ["addressType", "address1", "address2", "address3", "city", "zip", "state", "county", "country", "effectiveFrom", "forNotification"]
prsnAKAFields = ["akaType", "firstName", "middleName", "lastName", "organizationName"]
prsnIDFields = ["identificationType", "identificationNumber"]
prsnTelFields = ["preferred", "telephoneType", "telephoneNumber", "effectiveFrom"]
prsnConFields = ["type", "contact", "effectiveFrom"]
asstFields = ["assignmentRole", "dateAssigned", "status", "statusDate"]
caseAddFields = ["addressType", "zip", "city"]

checker(cas, caseFields)
checker(sc, subCaseFields)
checker(cas.collect("CtCAAProbates").first(), cpFields)
for (Document doc in sc.documents) {
  checker(doc, docFields)
  for (DocumentStatus docStatus in doc.statuses) {
    checker(docStatus, docStatFields)
  }
}
for (Party pty in sc.parties) {
  checker(pty, ptyFields)
  Person prsn = pty.person
  checker(prsn, prsnFields)
  for (PersonProfile prsnProf in prsn.profiles) {
    checker(prsnProf, prsnProfFields)
  }
  for (Address prsnAdd in prsn.addresses) {
    checker(prsnAdd, prsnAddFields)
  }
  for (PersonAKA prsnAKA in prsn.personAKAs) {
    checker(prsnAKA, prsnAKAFields)
  }
  for (Identification prsnID in prsn.identifications) {
    checker(prsnID, prsnIDFields)
  }
  for (Telephone prsnTel in prsn.telephones) {
    checker(prsnTel, prsnTelFields)
  }
  for (CaseContact prsnCon in prsn.contacts) {
    checker(prsnCon, prsnConFields)
  }
}
for (CaseAssignment ca in cas.assignments) {
  checker(ca, asstFields)
  Person caprsn = ca.person
  checker(caprsn, prsnFields)
  for (PersonProfile prsnProf in caprsn.profiles) {
    checker(prsnProf, prsnProfFields)
  }
  for (Address prsnAdd in caprsn.addresses) {
    checker(prsnAdd, prsnAddFields)
  }
  for (PersonAKA prsnAKA in caprsn.personAKAs) {
    checker(prsnAKA, prsnAKAFields)
  }
  for (Identification prsnID in caprsn.identifications) {
    checker(prsnID, prsnIDFields)
  }
  for (Telephone prsnTel in caprsn.telephones) {
    checker(prsnTel, prsnTelFields)
  }
  for (CaseContact prsnCon in caprsn.contacts) {
    checker(prsnCon, prsnConFields)
  }
}
for (CaseAddress casAdd in cas.addresses) {
  checker(casAdd, caseAddFields)
}

private checker(Object fEntity, List fList) {
  for (i = 0; i < fList.size(); i++) {
    field = fEntity.${fList[i]}
    count += 1
    if (field && (field.contains("<") || field.contains("&lt;"))) {
      addError("One of the fields on this form contains a forbidden character ('<'). Please remove this character before re-submitting.")
      break
    }
  }
}
logger.debug("Fields checked: " + count)