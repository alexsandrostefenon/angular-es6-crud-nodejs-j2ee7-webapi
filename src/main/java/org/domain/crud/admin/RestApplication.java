package org.domain.crud.admin;

import javax.ws.rs.core.Application;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.UserTransaction;
import javax.ws.rs.ApplicationPath;

@ApplicationPath("/rest")
public class RestApplication extends Application {
	@PersistenceContext(unitName = "primary")
	private EntityManager entityManager;
	
	@Resource
	private UserTransaction userTransaction;
	
    @PostConstruct
    public void initialize() {
    	try {
			RequestFilter.updateCrudServices(userTransaction, this.entityManager);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }

}
