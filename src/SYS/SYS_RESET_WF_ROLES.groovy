/*
Change Management
	Written by: kcsmbf 13/06/18
	Change Log:	kiseeg 13/09/18 Ignore administrators Added FIN Test
				kcsmbf 25/09/18 Removed Test
*/
// if (_userProfile.id == "710") _userProfile.theme = "Barbie World" //  Mwahahahaha

//User usr = DomainObject.findUnique(User.class, "id", _user)
//User usr = User.get(_user)
User usr = _user
up = usr.userProfile
logger.debug(up.workflowRoles)

//if (usr.securityGroup.collect("grantedAuthorities[authority==#p1]",'Administrator').size() == 0) {
up.workflowRoles.clear()
for (authority in usr.collect("SecurityGroup.grantedAuthorities.authority")) {
  if (authority == "Administrator") {
    up.workflowRoles.add("ITA")
  }
  if (authority == "Probate Client Services Officer") {
    up.workflowRoles.add("PRS")
  }
  if (authority == "Probate Examining Officer") {
    up.workflowRoles.add("PE")
  }
  if (authority == "Finance(BFS)") {
    up.workflowRoles.add("FIN")
  }
}
up.saveOrUpdate()
//}
logger.debug(up.workflowRoles)





