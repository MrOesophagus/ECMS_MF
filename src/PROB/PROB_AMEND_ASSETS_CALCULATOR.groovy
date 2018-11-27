/*
	Change Management
 	Written by: kcsmbf 22/08/18
	Change Log:
*/
import com.sustain.financial.*

// Set the Assessment Group code for fee calculation
String agCode = "SCP01_GRANT_APPLICATION"
caz = Case.get(_cse)
_newFeeAmount = 0 // Setting initial null value

// Extracting current Invoices
Set<Invoice> grantInvoices = caz.invoices.findAll{it.assessmentGroup.code == agCode}
_curInvoices = grantInvoices

// Need to write new estate value into cfProbateAssetAmount for fee assessment engine
currentEstateValue = caz.cfProbateAssetAmount
caz.cfProbateAssetAmount = _newEstateAmount

// Determining new fee Amount if not saving new EV (which should only be called on form submit)
if (!_saveNewEstateValue) {
  feeGroup = financialManager.getAssessmentGroup(agCode, "PC")
  fee = assessmentManager.getAssessmentResults(caz, feeGroup)
  _newFeeAmount = FinancialUtils.computeFeeAmount(fee)
  // Reverting the change made for new fee calculation
  caz.cfProbateAssetAmount = currentEstateValue
}
caz.saveOrUpdate()