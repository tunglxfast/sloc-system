package funix.sloc_system.dao;


import funix.sloc_system.entity.Role;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class RoleDaoImpl implements RoleDao {

	@Autowired
	private EntityManager entityManager;


	@Override
	public Role findRoleByName(String theRoleName) {
		TypedQuery<Role> theQuery = entityManager.createQuery("SELECT * FROM role WHERE name=?", Role.class);
		theQuery.setParameter(1, theRoleName);
		
		Role theRole = null;
		
		try {
			theRole = theQuery.getSingleResult();
		} catch (Exception e) {
			theRole = null;
		}
		
		return theRole;
	}
}
