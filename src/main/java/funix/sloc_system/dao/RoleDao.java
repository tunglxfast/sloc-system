package funix.sloc_system.dao;


import funix.sloc_system.entity.Role;

public interface RoleDao {

	public Role findRoleByName(String theRoleName);
	
}
