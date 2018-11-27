/*
Change Management
	Written by: kcsmbf 27/08/18
	Change Log:	kcsmbf 04/10/18 Changed to use Document Stamp instead of Resuable Fields
				kcsmbf 15/10/18 Added addSeal functionality
*/

import com.sustain.rule.context.*;

Integer pageNum = _stamp.pageNumber?.toInteger() ?: null
addSeal = (_stamp.cfAddSeal == "Y") ? "Y" : "N"
addText = (_stamp.cfAddText == "Y") ? "Y" : "N"
boolean issueDate = (_stamp.cfUseIssueDate == "Y") ? true : false

runRule("StampDocument_v2", ["document": _stamp.document, "stampPosition": _stamp.cfStampPosition, "addSeal": addSeal, "addText": addText, "pageNumBlankForAll": pageNum, "stampChildDocuments": "N", "useIssueDate": issueDate])

_stamp.document.saveOrUpdate()



