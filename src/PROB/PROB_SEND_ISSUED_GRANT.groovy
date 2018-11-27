/*
	Change Management
	Written by: kiseeg 12/11/18
	Change Log:	kcsmbf 13/11/18 Tweaked after slight changes to submit form
				kcsmbf 14/11/18 Spent 40 million confounding hours fixing this thing :(
				kcsmbf 24/11/18 Retweaked code to handle migrated cases

NOTE: This rule CANNOT sit on a form - for some reason the sendMail section runs thrice.  Calling it in a WF resolves this issue.
*/

import com.sustain.mail.MailManager

Document grantDoc
String caseNum = runRule("SYS_ENCODE_DECODE_STRING", ["input": _casein.sourceCaseNumber, "encode": false]).getOutputValue("result")
Case grant = Case.get(caseNum)
if (!grant) {
  addError("This is an invalid case - please select the correct case from the search results")
} else {
  SubCase scGrant = grant.collect("SubCases").first()
  Party pty = _casein.collect("Parties").first()
  String ptyEmail = pty.person.email

  // Checking for Amended Grant first
  if (grant.caseNumber.substring(0,5) == "PROB-") {
    if (scGrant.documents.find{it.docDef.number == '519915' && it.statuses.find{st-> st.statusType == 'FILED' && st.isActive}}) {
      grantDoc = scGrant.documents.find{it.docDef.number == '519915' && it.statuses.find{st-> st.statusType == 'FILED' && st.isActive}}
    } else {
      grantDoc = scGrant.documents.find{it.docDef.number == '529804' && it.statuses.find{st-> st.statusType == 'FILED' && st.isActive}}
    }
  } else {
    grantDoc = scGrant.documents.find{it.docDef.number == "529804"}
  }
  grantDocFile = grantDoc.getFile()

  Set<String> recipient = []
  recipient.add(ptyEmail)
  String subject = "In reference to " + grant.caseNumber + " - " + grant.caseShortName
  StringBuilder sb = new StringBuilder()
  sb.append("Supreme Court of South Australia \r\n")
  sb.append("Probate Registry \r\n\r\n")
  sb.append("RE: " + grant.caseNumber + " - " + grant.caseShortName + "\r\n\r\n")
  sb.append("Dear " + pty.fml + ", \r\n\r\n")
  sb.append("Please find attached your requested Grant File. \r\n\r\n")
  sb.append("Yours faithfully, \r\n" + "Registrar of Probates")
  String body = sb.toString()

  MailManager mailManager = (MailManager) getBean(MailManager.BEAN_NAME)
  mailManager.sendMail(recipient.toList(), subject, body, grantDocFile)

  // Create ROA msg on target case for auditing purposes
  ROAMessage roa = new ROAMessage()
  roa.category = 'PORTAL'
  roa.subCategory = 'ISSUED'
  roa.message = "Grant Request email:" + ptyEmail + " Body: " + body
  roa.message2 = "IP address " + pty.person.memo + " File sent: " + grantDoc.docName + " FDN: " + grantDoc.cfDocumentNumber + " Case: " + _casein.caseNumber
  roa.recordEntityName = grantDoc.entityShortName
  roa.recordId = grantDoc.id
  roa.timestamp = new Date()
  grant.add(roa)
  roa.saveOrUpdate()
}


