/*
	Change Management
 	Written by: kcsmbf 15/01/2018
	Change Log:	kcsmbf 23/02/18 Rewrote to reduce complexity
				kcsmbf 10/04/18 Added functionality to ignore certain subcase filing types
				kcsmbf 28/05/18 Fixed NPEs generated from when entity is attached to party
				kiseeg 14/08/18 ignore filing type 120000 - Copy of Record
				kcsmbf 15/10/18 Null safed for when a party is deleted and rule is called
				kiseeg 31/10/18 Added Delay and TX
*/

// Set the limits here for the number of reusable fields.  Change if you add extras.
def maxChoice = 12
def maxClob = 1
def maxString = 4
def maxList = 3

// Define subCase.filingTypes for which ignore this process
//def ignore = ["110000","120000"]
//SubCase sc = _this.subCase ?: _this.party?.subCase

//if (sc && !ignore.contains(sc.filingType)) {
CAAReusableFields crf = _this
String fieldToClean

if (crf.memo) crf.memo = null
Thread.sleep(500)
for (i = 1; i <= maxChoice; i++) {
  if (i < 10) {
    fieldToClean = 'cfChoice0' + i
  } else {
    fieldToClean = 'cfChoice' + i
  }
  if (crf."$fieldToClean") crf."$fieldToClean" = null
}
for (i = 1; i <= maxClob; i++) {
  fieldToClean = 'cfClob0' + i
  if (crf."$fieldToClean") crf."$fieldToClean" = null
}
for (i = 1; i <= maxString; i++) {
  fieldToClean = 'cfString0' + i
  if (crf."$fieldToClean") crf."$fieldToClean" = null
}
for (i = 1; i <= maxList; i++) {
  fieldToClean = 'clList0' + i
  if (crf."$fieldToClean") crf."$fieldToClean" = null
}
//}



