/*
	Change Management
	Written by: kcsmbf 23/10/18 Styled on CLOSEOPENPORTALTILL by Amy Watrous
	Change Log:	kcsmbf 29/10/18 Reworked otherTills section as find statement was breaking
*/

import com.sustain.expression.clause.OrderBy.Direction;
import com.sustain.financial.enums.TillStatus;
import com.sustain.financial.FinancialManager;
import com.sustain.financial.PaymentManager;
import com.sustain.security.model.User;

FinancialManager financialManager = (FinancialManager) getBean(FinancialManager.BEAN_NAME)
PaymentManager paymentManager = (PaymentManager) getBean(PaymentManager.BEAN_NAME)
Date now = new Date()
def Till till
def User user

user = User.get("eCourtPortalUser") // eCourt user that handles all Portal transactions
portalTill = DomainObject.findUnique(Till.class, "=operator.id", user.getId(), "status", TillStatus.OPEN, Finder.orderBy("tillDate", Direction.DESC), maxResult(1))
if (!_closePortalTill) {
  otherTills = Till.find(Till.class, "status", TillStatus.OPEN).minus(portalTill)
}

withTx {
  if (_closePortalTill) {
    if (portalTill) paymentManager.processTillClose(portalTill)
    financialManager.createUserTill(user, TillParticipant.findDefaultTillDef(user), now, now, false, null)
  } else if (!otherTills.isEmpty()) {
    otherTills.each{ t->
      paymentManager.processTillClose(t)
    }
  }
}
DomainObject.clearSession()



