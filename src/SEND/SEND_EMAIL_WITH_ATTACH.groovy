/*
 ***  DO NOT TOUCH THIS BR - MATT  ***
Change Log:
	Written by: kcsmbf 11/10/18
	Change Log:
*/

// imports for email
import org.xml.sax.SAXException;
import javax.xml.parsers.ParserConfigurationException;

// Defining variables
String body
String emailDomain
// Testing
//List<String> newEmails = []
//newEmails.add("matt.fitzpatrick@curts.sa.gov.au")
List<String> newEmails = _emailAddresses
String serverEnv = SystemProperty.getValue("general.server.environmentType")
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

try {
  mailManager.sendMail(newEmails, subject, body, filesList)
} catch (ParserConfigurationException e) {
  e.printStackTrace()
} catch (SAXException e) {
  e.printStackTrace()
} catch (IOException e) {
  e.printStackTrace()
}




