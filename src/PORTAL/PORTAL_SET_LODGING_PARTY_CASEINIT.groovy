/*
	Change Management
 	Written by: kcsmbf 05/05/18
	Change Log:	kcsmbf 28/08/18 Rewritten for official implementation
*/

// Define the PartyTypes to ignore as potential lodgers
List<String> canNotLodge = ["DEC", "WM"]

if (_case.cfPortalLodgingPartyId) {  // case was lodged via Portal
  if (_case.assignments.isEmpty()) {
    _case.parties.findAll{!canNotLodge.contains(it.partyType)}.first().person.cf_PortalUserId = _case.cfPortalLodgingPartyId
  } else {
    _case.assignments.first().person.cf_PortalUserId = _case.cfPortalLodgingPartyId
  }
}