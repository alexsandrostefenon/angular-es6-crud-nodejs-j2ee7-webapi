package org.domain.commom;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.transaction.UserTransaction;

public class DbUtils {

	// Utils
	public static class QueryMap extends HashMap<String, Object> {
		/**
		 *
		 */
		private static final long serialVersionUID = 9045195406428349166L;
	
		public QueryMap(Map<String, Object> parent) {
			super(parent);
		}

		public QueryMap() {
			// TODO Auto-generated constructor stub
		}

		public static QueryMap create() {
			return new QueryMap();
		}
	
		public static QueryMap create(Map<String, Object> parent) {
			return new QueryMap(parent);
		}
	
		public QueryMap add(String name, Object value) {
			this.put(name, value);
			return this;
		}
	}

	// Utils
	private static <T> TypedQuery<T> buildQuery(EntityManager entityManager, Class<T> resultClass, QueryMap fields, String[] orderBy) {
		StringBuilder sql = new StringBuilder(1024);
		sql.append("from " + resultClass.getName() + " o");
	
		if (fields.isEmpty() == false) {
			sql.append(" where ");
	
			fields.forEach((name, value) -> {
				if (value instanceof List) {
					if (((List<?>) value).size() == 1) {
						sql.append(String.format("o.%s = :%s and ", name, name));
					} else if (((List<?>) value).size() > 1) {
						sql.append(String.format("o.%s IN (:%s) and ", name, name));
					}
				} else {
					sql.append(String.format("o.%s = :%s and ", name, name));
				}
			});
	
			sql.setLength(sql.length()-5);
		}
	
		if (orderBy != null && orderBy.length > 0) {
			sql.append(" order by ");
	
			for (String field : orderBy) {
				sql.append("o." + CaseConvert.convertCaseUnderscoreToCamel(field, false) + ",");
			}
	
			sql.setLength(sql.length()-1);
		}
	
		TypedQuery<T> query = entityManager.createQuery(sql.toString(), resultClass);
		fields.forEach((name, value) -> query.setParameter(name, value));
		return query;
	}

	// Utils
	public static <T> CompletableFuture<List<T>> find(EntityManager entityManager, Class<T> resultClass, QueryMap fields, String[] orderBy, Integer startPosition, Integer maxResult) {
		return CompletableFuture.supplyAsync(() -> {
			TypedQuery<T> query = DbUtils.buildQuery(entityManager, resultClass, fields, orderBy);
	
			if (startPosition != null) {
				query.setFirstResult(startPosition);
			}
	
			if (maxResult != null) {
				query.setMaxResults(maxResult);
			}
	
			return query.getResultList();
		});
	}

	// Utils
	public static <T> CompletableFuture<T> findOne(EntityManager entityManager, Class<T> resultClass, QueryMap fields) {
		return CompletableFuture.supplyAsync(() -> buildQuery(entityManager, resultClass, fields, null).getSingleResult());
	}

	// Utils
	public static <T> CompletableFuture<T> insert(UserTransaction userTransaction, EntityManager entityManager, T obj) {
		return CompletableFuture.supplyAsync(() -> {
			try {
				if (userTransaction != null) userTransaction.begin();
				entityManager.persist(obj);
				if (userTransaction != null) userTransaction.commit();
				return obj;
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		});
	}

	// Utils
	public static <T> CompletableFuture<T> update(UserTransaction userTransaction, EntityManager entityManager, T obj) {
		return CompletableFuture.supplyAsync(() -> {
			try {
				if (userTransaction != null) userTransaction.begin();
				entityManager.merge(obj);
				if (userTransaction != null) userTransaction.commit();
				return obj;
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		});
	}

	// Utils
	public static <T> CompletableFuture<T> deleteOne(UserTransaction userTransaction, EntityManager entityManager, T obj) {
		return CompletableFuture.supplyAsync(() -> {
			try {
				if (userTransaction != null) userTransaction.begin();
				
				T ret;
				
				if (entityManager.contains(obj) == false) {
					ret = entityManager.merge(obj);
				} else {
					ret = obj;
				}
				
				entityManager.remove(ret);
				
				if (userTransaction != null) userTransaction.commit();
				return ret;
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		});
	}

}
