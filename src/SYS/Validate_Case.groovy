/*
 ***  DO NOT TOUCH THIS BR - MATT  ***
Change Log:
	Written by: kcsmbf 11/10/18
	Change Log:
*/

// imports for email
import java.io.File
import com.sustain.document.model.*;
import com.sustain.entities.custom.*;
import org.xml.sax.SAXException;
import javax.xml.parsers.ParserConfigurationException;

// Defining variables
String body
String emailDomain
// Testing
//List<String> newEmails = []
//newEmails.add("matt.fitzpatrick@curts.sa.gov.au")
List<String> newEmails = _emailAddresses
String systemURL = SystemProperty.getValue("general.serverUrl")
String serverEnv = SystemProperty.getValue("general.server.environmentType")
String sysErrorsTo = SystemProperty.getValue("mail.errors.sysadmin.emailTo")
boolean isProd = (serverEnv.equalsIgnoreCase("prod")) ?: false

// Redirecting external email addresses for non Prod environments
for (i = 0; i < newEmails.size(); i++) {
  emailDomain = newEmails[i].substring(newEmails[i].lastIndexOf("@") + 1)
  if (!isProd) {
    if (!emailDomain.equalsIgnoreCase("courts.sa.gov.au")) {
      if (serverEnv.equalsIgnoreCase("TRAINING 1") || serverEnv.equalsIgnoreCase("TRAINING 2") || serverEnv.equalsIgnoreCase("UAT")) {
        newEmails[i] = "ecmstraining@courts.sa.gov.au"
      } else {
        newEmails[i] = "ecmsdev@courts.sa.gov.au"
      }
    }
    subject = serverEnv + ": " + _subject
  }
}

// add the salutation to the body with line breaks
if (_salutation) {
  body = _salutation + ', \r\n\r\n' + _body
} else {
  body =  _body + " " + newEmails[0]
}

// Construct attachment list
int attachCount = (_attachment1) ? ((_attachment2) ? ((_attachment3) ? 3 : 2) : 1) : 0
File[] filesList = new File[attachCount]
if (_attachment1) { filesList[0] = _attachment1.file }
if (_attachment2) { filesList[1] = _attachment2.file }
if (_attachment3) { filesList[2] = _attachment3.file }

/*try {
  new com.sustain.mail.Email(mailManager)
  .addToEmails(newEmails)
  .setSubject(subject)
  .setMessageBody(body)
  .addAttachment(new com.sustain.mail.Attachment(_attachment1.file))
  .addAttachment(new com.sustain.mail.Attachment(_attachment2.file))
  .addAttachment(new com.sustain.mail.Attachment(_attachment3.file))
  .sendToAll();
} catch (Exception e) {
  failedBody = (!serverEnv.equalsIgnoreCase("prod")) ? serverEnv : ""
  failedBody += "\r\n\r\n Send mail failed for \r\n\r\n" + body + "\r\n\r\n" + e
  runRule("SEND_EMAIL", ["emailAddress": sysErrorsTo, "subject": "SEND_EMAIL_WITH_ATTACH failure", "body": failedBody])
}
*/
try {
  mailManager.sendMail(newEmails, subject, body, filesList)
} catch (ParserConfigurationException e) {
  e.printStackTrace()
} catch (SAXException e) {
  e.printStackTrace()
} catch (IOException e) {
  e.printStackTrace()
}




