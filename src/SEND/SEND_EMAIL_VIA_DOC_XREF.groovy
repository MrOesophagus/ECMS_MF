/*
	Change Management
	Written by: kcsmbf 25/05/18
	Change Log:	kcsmbf 26/05/18 Added XRef Type picker functionality & ROA message
				kisbac 14/09/18 Enabled sending of emails.
				kcsmbf 29/09/18 Added ignore to parties with IA/DEC status, reworked body as per supplied template, handling for parties with no email address, reworked unique email recipients code
*/

// Define PartyTypes to not receive an email
noSend = ["DEC"]

// Define other variables
String body // used later
String salutation // used later
String signoff = "" // used later
boolean isOrg // used later
Document doc = _document
HashSet ptysNoEmail = new HashSet<Object>()
HashMap recipients = new HashMap<String, Object>()
String portalURL = SystemProperty.getValue("ecourtpublic.url")
String serverEnv = SystemProperty.getValue("general.server.environmentType")
String subject = "In relation to " + doc.case.caseNumber + " - " + doc.case.caseShortName
List<Party> partiesXRef = doc.findByXRef("Party", _xrefType).findAll{!noSend.contains(it.partyType) && !["IA", "DEC"].contains(it.status)}

// Determine unique email recipients
for (Party pty in partiesXRef.findAll{it.selfRepresented == "Y"}) {
  if (pty.person.profiles.findAll{it.isDeceased}.size() > 1) continue;
  else {
    srlEmail = pty.person.contacts.find{it.isCurrentEmail}?.contact
    if (srlEmail && !recipients.containsKey(srlEmail)) {
      recipients.put(srlEmail, pty)
    } else if (!srlEmail && !ptysNoEmail.contains(pty)) {
      ptysNoEmail.add(pty)
    }
  }
}
for (Party pty in partiesXRef.findAll{it.selfRepresented == "N"}) {
  pty.findByXRef("CaseAssignment", "REPRESENTEDBY").findAll{it.status == 'CUR' && ["LAW", "LAWFIRM"].contains(it.assignmentRole)}.each { asst->
    asstEmail = asst.person.contacts.find{it.isCurrentEmail}?.contact
    if (asstEmail && !recipients.containsKey(asstEmail)) {
      recipients.put(asstEmail, asst)
    } else if (!asstEmail && !ptysNoEmail.contains(asst)) {
      ptysNoEmail.add(asst)
    }
  }
}

//Replace tokens in the body message with variables
_bodyMessage = emailBodyTokenised(_bodyMessage, _tokens)

// Add extraneous body content
StringBuilder sb = new StringBuilder()
switch (doc.case.caseJurisdiction) {
  case "PC":
    sb.append("Supreme Court of South Australia \r\nProbate Registry")
    signoff = "Registrar of Probates"
    break;

  default :
    sb.append("Courts Administration Authority of South Australia")
    signoff = "Registrar"
}
sb.append("\r\n\r\n" + "RE: " + doc.case.caseNumber + " - " + doc.case.caseShortName + "\r\n\r\n")
sb.append(_bodyMessage)
sb.append("\r\n\r\n" + "Please follow the below link to log into CourtSA and access this case: \r\n" + portalURL)
sb.append("\r\n\r\n" + "Yours faithfully, \r\n" + signoff)
body = sb.toString()

// Generate & send email
if (!recipients.isEmpty()) {
  recipients.each { k, v ->
    isOrg = (v.entityShortName == "Party") ? ((v.partySubType == "CORP") ? true : false) : (v.person.personCode.substring(0, 1).equalsIgnoreCase('L')) ? true : false
    salutation = (isOrg) ? "To: " + v.person.organizationName : "Dear " + v.person.personNameFML
    params1 = ["emailAddress": k, "subject": subject, "body": body, "salutation": salutation]
    runRule("SEND_EMAIL", params1)
  }
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

// logger.debug(recipients)
// logger.debug(ptysNoEmail)

// Create ROA message
ROAMessage roa = new ROAMessage()
roa.category = 'WORKFLOW'
roa.subCategory = 'SYSGENEMAIL'
roa.message2 = roa.subCategoryLabel + ' for ' + doc.fullName
roa.recordEntityName = doc.entityName
roa.recordId = doc.id
roa.message = 'System generated email sent to all ' + com.sustain.lookuplist.model.LookupItem.getLabel("CASE_XREF_TYPE", _xrefType) + ' parties (or their lawyers) with current email address:\r\n'
recipients.each { k, v->
  roa.message += v.personNameFML + ' (' + k.take(3) + '********' + k.reverse().take(4).reverse() + ')\r\n'
}
roa.message += body
doc.case.add(roa)
roa.saveOrUpdate()

//FUNCTIONS
private String emailBodyTokenised (String body, String tokenisedString) {
  if (tokenisedString) {
    String[] tokens = tokenisedString.split(';')
    for (int i = 0; i < tokens.length; i++) {
      String toMatch = "%" + (i+1)
      body = body.replace(toMatch, tokens[i])
    }
  }
  return body
}





