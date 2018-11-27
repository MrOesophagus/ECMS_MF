/*
	Change Management
	Written by: kcsmbf 26/05/18
	Change Log:	kisbac 22/06/18 Add a function 'emailBodyTokenised' which allows you to provide a tokenised string and the values which will be parsed into the message.
				kisbac 14/09/18 Enable sending of emails.
				kcsmbf 29/09/18 Added ignore to parties with IA/DEC status, reworked body as per supplied template, handling for parties with no email address, reworked unique email recipients code
*/

import com.sustain.rule.context.*;
//import org.apache.commons.lang3.StringUtils

// Define PartyTypes to not receive an email
noSend = ["DEC"]

// Define variables
String body // used later
String salutation // used later
String signoff = "" // used later
String tgt // used later
boolean isOrg // used later
HashSet Ptys = new HashSet<Party>()
HashSet ptysNoEmail = new HashSet<Object>()
HashMap recipients = new HashMap<String, Object>()
String portalURL = SystemProperty.getValue("ecourtpublic.url")
String serverEnv = SystemProperty.getValue("general.server.environmentType")
String subject = "In relation to " + _case.caseNumber + " - " + _case.caseShortName
String tgtlbl = com.sustain.lookuplist.model.LookupItem.getLabel("PARTY_TYPE", _partyType)


// Gather the relevant Parties to be emailed
if (!["ALLPTY", "ALLPTYINCJOIN"].contains(_partyType)) {
  tgt = "all " + tgtlbl + "s"
  Ptys = _case.parties.findAll{it.partyType == _partyType && !["IA", "DEC"].contains(it.status)}
} else {
  tgt = tgtlbl
  List<String> applicants = ["PEX", "PAD", "PAPPR", "CAV", "APP", "EXAD", "REX"]
  //Ptys = _case.parties.findAll{applicants.contains(it.partyType) && !["IA", "DEC"].contains(it.status)}
  if (_partyType == "ALLPTYINCJOIN") {
    for (joinedCase in _case.joinedCases) {
      if (joinedCase != _case) {
        joinedCase.parties.findAll{applicants.contains(it.partyType) && !["IA", "DEC"].contains(it.status)}.each{Ptys.add(it)}
      }
    }
  }
}

// Determine unique email recipients from relevant Parties
for (Party pty in Ptys.findAll{it.selfRepresented == "Y"}) {
  srlEmail = pty.person.contacts.find{it.isCurrentEmail}?.contact
  if (srlEmail && !recipients.containsKey(srlEmail)) {
    recipients.put(srlEmail, pty)
  } else if (!srlEmail && !ptysNoEmail.contains(pty)) {
    ptysNoEmail.add(pty)
  }
}
for (pty in Ptys.findAll{it.selfRepresented == "N"}) {
  pty.findByXRef("CaseAssignment", "REPRESENTEDBY").findAll{it.status == 'CUR' && ["LAW", "LAWFIRM"].contains(it.assignmentRole)}.each { asst->
    asstEmail = asst.person.contacts.find{it.isCurrentEmail}?.contact
    if (asstEmail && !recipients.containsKey(asstEmail)) {
      recipients.put(asstEmail, asst)
    } else if (!asstEmail && !ptysNoEmail.contains(asst)) {
      ptysNoEmail.add(asst)
    }
  }
}

// Replace tokens in the body message with variables
_bodyMessage = emailBodyTokenised(_bodyMessage, _tokens);

// Add extraneous body content
StringBuilder sb = new StringBuilder()
switch (_case.caseJurisdiction) {
  case "PC":
    sb.append("Supreme Court of South Australia \r\nProbate Registry")
    signoff = "Registrar of Probates"
    break;

  default:
    sb.append("Courts Administration Authority of South Australia")
    signoff = "Registrar"
    break;
}
sb.append("\r\n\r\n" + "RE: " + _case.caseNumber + " - " + _case.caseShortName + "\r\n\r\n")
sb.append(_bodyMessage)
sb.append("\r\n\r\n" + "Please follow the below link to log into CourtSA and access this case: \r\n" + portalURL)
sb.append("\r\n\r\n" + "Yours faithfully, \r\n" + signoff)
body = sb.toString()

// Generate & send email
if (!recipients.isEmpty()) {
  recipients.each { k, v ->
    isOrg = (v.entityShortName == "Party") ? ((v.partySubType == "CORP") ? true : false) : (v.person.personCode.substring(0, 1).equalsIgnoreCase('L')) ? true : false
    salutation = (isOrg) ? "To: " + v.person.organizationName : "Dear " + v.person.personNameFML
    params = ["emailAddress": k, "subject": subject, "body": body, "salutation": salutation]
    runRule("SEND_EMAIL", params)
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
roa.message2 = roa.subCategoryLabel + ' for ' + tgt
roa.recordEntityName = _case.entityName
roa.recordId = _case.caseid
roa.message = 'System generated email sent to ' + tgt + ' (or their lawyers) with a current email address:\r\n'
recipients.each { k, v->
  roa.message += v.personNameFML + ' (' + k.take(3) + '********' + k.reverse().take(4).reverse() + ')\r\n'
}
roa.message += body
_case.add(roa)
roa.saveOrUpdate()

// FUNCTIONS
private String emailBodyTokenised (String body, String tokenisedString) {
  if (tokenisedString) {
    String[] tokens = tokenisedString.split(';')
    for (int i = 0; i < tokens.length; i++) {
      String toMatch = "%" + (i + 1)
      body = body.replace(toMatch, tokens[i])
    }
  }
  return body
}