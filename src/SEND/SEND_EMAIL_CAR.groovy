/*
	Change Management
	Written by: kcsmbf 25/07/18
	Change Log:	kisbac 14/09/18, Enable sending of emails
				kcsmbf 30/09/18 Significant rework for body msg, handling no email parties and other tweaks
*/

// Define other variables
boolean isOrg
Date today = new Date()
HashSet ptysNoEmail = new HashSet<Object>()
SubCase sc = _case.collect("SubCases").first()
String portalURL = SystemProperty.getValue("ecourtpublic.url")
String serverEnv = SystemProperty.getValue("general.server.environmentType")

if (_case.assignments) {
  recipient = _case.collect("Assignments").first()
  isOrg = (recipient.person.personCode.substring(0, 1).equalsIgnoreCase('L')) ? true : false
} else {
  recipient = _case.collect("Parties").first()
  isOrg = (recipient.partySubType == "CORP") ? true : false
}
emailAddress = recipient.person.contacts.find{it.isCurrentEmail}?.contact
if (!emailAddress) { ptysNoEmail.add(recipient) }

// Create and send the email
salutation = (isOrg) ? "To: " + recipient.person.organizationName : "Dear " + recipient.person.personNameFML
String subject = "In relation to " + _case.caseNumber + " - " + _case.caseShortName
StringBuilder sb = new StringBuilder()
switch (_case.caseJurisdiction) {
  case "PC":
    sb.append("Supreme Court of South Australia \r\nProbate Registry")
    signoff = "Registrar of Probates"
    break;

  default :
    sb.append("Courts Administration Authority of South Australia")
    signoff = "Registrar"
}
sb.append("\r\n\r\n" + "RE: " + _case.caseNumber + " - " + _case.caseShortName + "\r\n\r\n")
if (_emailVariant == "INIT") {
  sb.append("Your request has been sent to the registry for review.\r\n\r\n")
  sb.append("If approved, an email will be sent to you with a link to access the case file on Court SA.\r\n")
  sb.append("Once access is granted you will be able to view the file and lodge documents.")
} else if (_emailVariant == "REJ") {
  sb.append("Your Case Access Request has been rejected for the following reason:\r\n\r\n" + sc.cfReasonForRejection)
}
sb.append("\r\n\r\n" + "Please follow the below link to access Court SA: \r\n" + portalURL)
sb.append("\r\n\r\n" + "Yours faithfully, \r\n" + signoff)
body = sb.toString()
if (emailAddress) { // Just in case there's no email address for whatever reason
  params = ["emailAddress": emailAddress, "subject": subject , "body": body, "salutation": salutation]
  runRule("SEND_EMAIL", params)
}

// Handling Parties/Case Assignments without an email address
if (!ptysNoEmail.isEmpty()) {
  StringBuilder sb2 = new StringBuilder()
  if (!serverEnv.equalsIgnoreCase("prod")) { sb2.append(serverEnv + "\r\n\r\n") }
  sb2.append("The below message could not be delivered to the following recipients due to lack of a valid email address: \r\n\r\n")
  sb2.append("*** FAILED RECIPIENTS ***\r\n")
  for (pty in ptysNoEmail) {
    (pty.entityShortName == "Party") ? sb2.append(pty.partyTypePartyNum) : sb2.append("Legal Representative:")
    sb2.append(" " + pty.person.personNameFML + "\r\n")
  }
  sb2.append("\r\n" + "*** MESSAGE *** \r\n" + body)
  msg = sb2.toString()
  // Building in future expansion capability as we get other registries
  p2email = SystemProperty.getValue("mail.errors.probate.emailTo")
  p2subject = "Failed to send email message"
  params2 = ["emailAddress": p2email, "subject": p2subject, "body": msg]
  runRule("SEND_EMAIL", params2)
}

// Create ROA message
if (emailAddress) {
  ROAMessage roa = new ROAMessage()
  roa.category = 'WORKFLOW'
  roa.subCategory = 'SYSGENEMAIL'
  roa.message2 = roa.subCategoryLabel + ' for ' + recipient.personNameFML
  roa.recordEntityName = _case.entityName
  roa.recordId = _case.caseid
  roa.message = 'System generated email sent to ' + recipient.personNameFML + ' (' + emailAddress.take(3) + '********' + emailAddress.reverse().take(4).reverse() + ')\r\n'
  roa.message += body
  _case.add(roa)
  roa.saveOrUpdate()
}




