/*
	Change Management
 	Written by: kcsmbf 05/04/18, styled on rule from KCDC config
	Change Log:	kisbac 14/09/18, enabled sending of emails.
				      kcsmbf 30/09/18 reworked body, minor tweaks
*/

def invitee
boolean isOrg
String signoff
Date today = new Date()
Ce_Invitation invitation = _invite
String portalURL = SystemProperty.getValue("ecourtpublic.url")

if (invitation.party) {
  invitee = invitation.party
  isOrg = (invitee.partySubType == "CORP")
} else {
  invitee = invitation.caseAssignment
  isOrg = (invitee.person.personCode.substring(0, 1).equalsIgnoreCase('L')) ? true : false
}

// Determining valid email address
String emailAddress = invitee.person.contacts.find{it.isCurrentEmail}?.contact

// Create and send the email
// email salutation
salutation = (isOrg) ? "To: " + invitee.person.organizationName : "Dear " + invitee.person.personNameFML
// email body
StringBuilder sb = new StringBuilder()
switch (invitee.case.caseJurisdiction) {
  case "PC":
    sb.append("Supreme Court of South Australia \r\nProbate Registry")
    signoff = "Registrar of Probates"
    break

  default :
    sb.append("Courts Administration Authority of South Australia")
    signoff = "Registrar"
}
sb.append("\r\n\r\n" + "RE: " + invitee.case.caseNumber + " - " + invitee.case.caseShortName + "\r\n\r\n")
sb.append( "Your request to access the case file in the matter of " + invitee.case.caseShortName + " has been approved.")
sb.append("\r\n\r\n" + "Please follow the below link to access this case on Court SA: \r\n" + portalURL+'/?q=joinCase&token='+invitation.inviteToken)
sb.append("\r\n\r\n" + "The file will then appear in your list of My Cases and you will be able to view the file and lodge documents.")
sb.append("\r\n\r\n" + "Yours faithfully, \r\n" + signoff)
body = sb.toString()
// send email
params = ["emailAddress": emailAddress, "subject": 'A Case Access Invitation on Court SA' , "body": body, "salutation": salutation]
runRule("SEND_EMAIL", params)
invitation.sendEmail = "N"
invitation.lastSent = today

//ROA message to record the log for the invitation
ROAMessage roa = new ROAMessage()
roa.category = 'PORTAL'
roa.subCategory = 'CAR_INVITE'
roa.message2 = 'Portal  Email Resent'
roa.recordEntityName = invitee.entityName
roa.message = 'Invitation for Portal Access resent to ' + invitee.personNameFML + " (" + emailAddress + ")\r\n\r\n" + portalURL+'/?q=joinCase&token='+invitation.inviteToken
invitee.case.add(roa)
roa.saveOrUpdate()




