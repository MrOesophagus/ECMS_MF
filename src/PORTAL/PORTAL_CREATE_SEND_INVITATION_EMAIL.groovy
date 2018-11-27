/*
	Change Management
 	Written by:	kcsmbf 04/04/18, styled on rule from KCDC config
	Change Log:	kcsmbf 05/04/18 Broke apart into two rules: one for new Inv, one for resending existing Inv
				kcsmbf 07/08/18 Changed bodyMessage as per spec requirement
				kisbac 14/09/18 Enable sending of emails.
				kcsmbf 30/09/18 Reworked body as per template, minor tweaks
*/

import com.sustain.rule.context.*

// Define variables
boolean isOrg
String signoff
Date today = new Date()
Object invitee
Ce_Invitation invitation = _newInvite
String portalURL = SystemProperty.getValue("ecourtpublic.url")

// Determining recipient (invitee)
if (invitation.party) {
  invitee = invitation.party
  isOrg = (invitee.partySubType == "CORP")
} else {
  invitee = invitation.caseAssignment
  isOrg = (invitee.person.personCode.substring(0, 1).equalsIgnoreCase('L')) ? true : false
}
// Determining valid email address
String emailAddress = invitee.person.contacts.find{it.isCurrentEmail}?.contact

// Expiring all current, open Invites
// (for when someone accidentally adds a new one instead of resending existing open invite)
if (invitee.hasOpenPortalInvite) {
  invitee.collect("Ce_Invitations").minus(invitation).each{it.expireDate = DateUtil.getYesterday()}
}

invitation.case = invitee.case
/*
if (invitee.entityShortName == "Party") {
  invitation.party = invitee
  isOrg = (invitee.partySubType == "CORP")
} else {
  invitation.caseAssignment = invitee
  isOrg = (invitee.person.personCode.substring(0, 1).equalsIgnoreCase('L')) ? true : false
}
*/
invitation.email = emailAddress
invitation.sendEmail = "N"
invitation.lastSent = today
invitation.inviteToken = StringUtils.encodeNumberAlpha(invitee.id)
invitation.expireDate = DateUtil.addBusinessDays(today, 20)
invitation.saveOrUpdate()

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

// ROA message to record the log for the invitation
ROAMessage roa = new ROAMessage()
roa.category = 'PORTAL'
roa.subCategory = 'CAR_INVITE'
roa.message2 = 'Court SA Invitation sent'
roa.recordEntityName = invitee.entityName
roa.message = 'Invitation for Case Access sent to ' + invitee.personNameFML + " (" + emailAddress + ")\r\n\r\n" + portalURL+'/?q=joinCase&token='+invitation.inviteToken
invitee.case.add(roa)
roa.saveOrUpdate()