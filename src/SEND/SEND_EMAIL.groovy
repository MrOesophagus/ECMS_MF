/* send email
DO NOT TOUCH THIS BR - MATT
Change Log: kisbac Added server environment handling
			kcsmbf 29/09/18 Tweaked server env handling to deal with changes to other send email rules
*/

// imports for email
import com.sustain.document.model.*;
import com.sustain.entities.custom.*;
import org.xml.sax.SAXException;
import javax.xml.parsers.ParserConfigurationException;

// for email
String serverEnv = SystemProperty.getValue("general.server.environmentType")
//logger.debug("Environment: "+serverEnv)
String emailDomain = _emailAddress.substring(_emailAddress.lastIndexOf("@")+1);
//logger.debug("Domain: "+emailDomain)

boolean isProd = (serverEnv.equalsIgnoreCase("prod")) ?: false
String emailAddress = _emailAddress
String subject = _subject
if (!isProd) {
  if (!emailDomain.equalsIgnoreCase("courts.sa.gov.au")) {
    if (serverEnv.equalsIgnoreCase("TRAINING 1") || serverEnv.equalsIgnoreCase("TRAINING 2") || serverEnv.equalsIgnoreCase("UAT")) {
      emailAddress = "ecmstraining@courts.sa.gov.au"
    } else {
      emailAddress = "ecmsdev@courts.sa.gov.au"
    }
  }
  subject = serverEnv + ": " + _subject
}

//logger.debug("Email: "+emailAddress)
//logger.debug("Subject: "+subject)
String body = null;  // constructed later after checking whether there is a salutation
String salutation = _salutation;
String systemURL = SystemProperty.getValue("general.serverUrl");

// formatting note: line return is \r\n

// add the salutation to the body with line breaks
if(salutation) {
  body = _salutation + ', \r\n\r\n' + _body;
} else {
  body =  _body;
}

// send email - uncomment all the below

try {
  mailManager.sendMail(emailAddress, subject, body, null);
  //logger.debug("Email Sent to " + toEmail);
} catch (ParserConfigurationException e) {
  e.printStackTrace();
} catch (SAXException e) {
  e.printStackTrace();
} catch (IOException e) {
  e.printStackTrace();
}




