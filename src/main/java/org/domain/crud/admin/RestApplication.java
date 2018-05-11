package org.domain.crud.admin;

import javax.ws.rs.core.Application;

import java.util.Set;

import javax.annotation.PostConstruct;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.ws.rs.ApplicationPath;

@ApplicationPath("/rest")
public class RestApplication extends Application {
	@PersistenceContext(unitName = "primary")
	private EntityManager entityManager;
	
    @PostConstruct
    public void initialize() {
    	Set<javax.persistence.metamodel.EntityType<?>> entityTypes = entityManager.getEntityManagerFactory().getMetamodel().getEntities();
    	
        for (javax.persistence.metamodel.EntityType entityType : entityTypes) {
        	System.out.println(entityType.getJavaType().getCanonicalName()); // org.domain.nfe.entity.NfeTaxGroup
        	System.out.println(entityType.getName()); // NfeTaxGroup
//        	ServiceGenerator.generate(className);
        }
    	
    }

}
