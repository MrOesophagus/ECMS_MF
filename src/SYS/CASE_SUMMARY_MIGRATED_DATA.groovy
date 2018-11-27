/* 	
	Change Management
 	Written by: KISBAC 06/09/2018
	Change Log:	kcsmbf 19/11/18
*/

import com.sustain.casenotes.model.CaseNote

Case mCase = _cse

StringBuilder html = new StringBuilder()
html.append("<table width='100%'>")
html.append("<col width='20%'>")
html.append("<col width='20%'>")
html.append("<col width='60%'>")
html.append("<tr><th>Date</th><th>Type</th><th>Info</th></tr>")

List<Document> documents = mCase.collect("documents").orderBy("dateCreated desc")
List<CaseNote> notes = mCase.collect("caseNotes").orderBy("dateCreated desc")

mList = []
for (Document mDoc in documents) {
  mList.add([Date:mDoc.dateCreated, Type:"Document", Info:mDoc.fullName])
}
for (CaseNote mNote in notes) {
  mList.add([Date:mNote.dateCreated, Type:"CaseNote", Info:mNote.content])
}
sortedList = mList.sort{a,b-> a.Date <=> b.Date}

for (item in sortedList) {
  html.append("<tr><td>" + DateUtil.format(item.Date, "dd MMM yy hh:mm:ss a") + "</td><td>" + item.Type + "</td><td>" + item.Info + "</td></tr>")
}
html.append("</table")
_outputHTML = html.toString()