/*
	Change Management
	Written by: kcsmbf 21/08/18
	Change Log:	kcsmbf 27/09/18 Finished the rule

A4 size: 595 x 842 points
*/

import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.pdmodel.PDPage
import org.apache.pdfbox.pdmodel.common.PDRectangle
boolean hasChanged = false

// Allowing for (totally arbitrary) 3% under-sized variation
float w97 = 595 * 0.97
float h97 = 842 * 0.97

PDDocument source = PDDocument.load(_input.file)
for (i = 1; i <= source.getNumberOfPages(); i++) {
  PDPage page = source.getPage(i-1)
  height = page.getMediaBox().getHeight()
  width = page.getMediaBox().getWidth()
  if ((height < h97 || width < w97) && height > width) { // Checks page is undersized & Portrait
    page.setMediaBox(PDRectangle.A4)
    page.setCropBox(PDRectangle.A4)
    if (!hasChanged) { hasChanged = true }
  }
}

// Save & store resized file
if (hasChanged) {
  tmpFile = File.createTempFile("tmp", ".pdf")
  source.save(tmpFile)
  source.close()
  _input.store(tmpFile)
  _input.saveOrUpdate()
  tmpFile.delete()
}



