/*
	Change Management
	Written by: kcsmbf 10/08/2018
	Change Log:	kcsmbf 13/08/2018 Minor tweaks as rule wasn't working correctly.
				kcsmbf 28/09/18 Removed sender username functionality due to Portal security implications

Usage details
  Call this as an action on a Manual Workflow result with the following inputs:
  reason:		 	@{activityInst.senderResultCode}
  note: 			@{activityInst.notes.size() > 0 ? activityInst.notes[0].message :''}
  senderUserName:	@{activityInst.senderUsername}
*/

// Add Initiating Documents to this list to force Rejection notes into subCase.cfReasonForRejection
List<String> initDocs = ["529802", "529807", "529808"]

import com.sustain.security.model.User
Object inputEntity = _docOrSubcase

if (_note.length() > 0) {
  String now = new Date().format("dd MMM yyyy hh:mm a").toString()
  //User usr = User.get(_senderUserName)
  //String usrFML = usr.dirPerson?.personNameFML ?: usr.username
  //String fullNote = now + ": " + _reason + " by " + usrFML + " (" + usr.username + ") - " + _note + System.getProperty("line.separator")
  String fullNote = _note

  if (inputEntity.entityShortName == "Document") {
    if (_reason.equalsIgnoreCase("Rejected")) {
      inputEntity.cfReasonForRejection = _note
      if (initDocs.contains(inputEntity.docDefNumber)) {
        inputEntity.subCase.cfReasonForRejection = _note
      }
    }
    if (!inputEntity.cfWorkFlowNotes) { // Avoids "null" first line if creating field
      inputEntity.cfWorkFlowNotes = fullNote
    } else {
      inputEntity.cfWorkFlowNotes += fullNote
    }
  } else if (inputEntity.entityShortName == "SubCase") {
    if (_reason.equalsIgnoreCase("Rejected")) {
      inputEntity.cfReasonForRejection = _note
    }
    if (!inputEntity.cfWorkFlowNotes) { // Avoids "null" first line if creating field
      inputEntity.cfWorkFlowNotes = fullNote
    } else {
      inputEntity.cfWorkFlowNotes += fullNote
    }
  }
}
inputEntity.saveOrUpdate()



