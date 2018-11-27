/*
	Change Management
	Written by: kcsmbf 18/11/18
	Change Log:
*/

// Main script variables
SubCase sc = _actionDoc.subCase
Document grantDoc, origGrantDoc, willDoc, certTransDoc
boolean amended = (_actionDoc.cAADocuments.cfAnyCodicils == "Y")

// Grant processing
String grantDDN = (amended) ? "519915" : "529804"  // Amended Grant vs Generated Grant
grantDoc = sc.documents.find{it.docDef.number == grantDDN && !it.statuses}
if (!grantDoc || !grantDoc.stored) {
  addError("ERROR: There is something wrong with the draft grant document (not present, no file attached, or it has a status).")
}
if (sc.category == "510400") { // Handling Reseal of Grant applications
  origGrantDoc = sc.documents.find{it.docDef.number == "519908" && it.statuses.find{st -> st.statusType == "CHKD"}}
  if (!origGrantDoc || !origGrantDoc.stored) {
    addError("ERROR: There is something wrong with the Original Grant document (not present, no file attached, or it has a status).")
  }
}
// Will processing
if (_actionDoc.case.category != "510300") { // Checking it's not a LoA without Will
  String willDDN = (!sc.collect("Documents[docDefNumber == '519920']").isEmpty()) ? "519920" : "519902"
  // Engrossed vs normal will
  if (willDDN == "519902") {
    willDoc = sc.documents.find { it.docDef.number == willDDN && it.statuses.find { st -> st.statusType == "CHKD" } }
    if (!willDoc || !willDoc.stored) {
      addError("ERROR: There is something wrong with the Will document (no file attached, or not Checked).")
    }
  } else {
    willDoc = sc.documents.find { it.docDef.number == willDDN }
    if (!willDoc || !willDoc.stored) {
      addError("ERROR: There is something wrong with the Engrossed Will document (not present or no file attached).")
    }
  }
  certTransDoc = sc.documents.find {
    it.docDefNumber == "519904" && it.collect("Statuses[StatusType == 'REJ']").isEmpty()
  }
  if (certTransDoc && !certTransDoc.stored) {
    addError("ERROR: The certified translation of will document does not have a file attached.")
  }
}