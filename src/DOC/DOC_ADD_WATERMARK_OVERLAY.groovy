/*
	Change Management
	Written by: kcsmbf 24/07/18
	Change Log:
*/

import java.io.File
import org.apache.pdfbox.multipdf.Overlay
import org.apache.pdfbox.pdmodel.PDDocument

tmpFile = File.createTempFile("tmp", ".pdf")
PDDocument source = PDDocument.load(_doc.file)
DirOrgUnit org = DirOrgUnit.get(8339)
DirAttachment da = org.attachments.find{it.attachmentType == 'Stamp' && it.caption == _seal}
String wm = da.getAttachmentFile().absolutePath

HashMap<Integer, String> overlayGuide = new HashMap()
if (!_pageNumBlankForAll) {
  for (i = 1; i <= source.getNumberOfPages(); i++) {
    overlayGuide.put(i, wm)
  }
} else {
  overlayGuide.put(_pageNumBlankForAll, wm)
}


Overlay overlay = new Overlay()
overlay.setInputPDF(source)
overlay.setOverlayPosition(Overlay.Position.FOREGROUND)
overlay.overlay(overlayGuide)
source.save(tmpFile)
_doc.store(tmpFile)
_doc.saveOrUpdate()
tmpFile.delete()



