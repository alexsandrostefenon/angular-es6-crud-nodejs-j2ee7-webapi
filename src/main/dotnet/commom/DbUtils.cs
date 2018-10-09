using System;
using System.Collections;
using System.Collections.Generic;
using System.Data.Common;
using System.Linq;
using System.Reflection;
using System.Text;
using System.Threading.Tasks;
using Microsoft.EntityFrameworkCore;
using Npgsql;

namespace org.domain.commom {
	public static class DbUtils {
		//  Utils
		public class QueryMap : Dictionary<String, Object> {

			public QueryMap (IDictionary<String, Object> parent) : base (parent)
			{
			}

			public QueryMap ()
			{
				//  TODO Auto-generated constructor stub
			}

			public static QueryMap Create ()
			{
				return new QueryMap ();
			}

			public static QueryMap Create (IDictionary<String, Object> parent)
			{
				return new QueryMap (parent);
			}

			public QueryMap AddNext (String name, Object value)
			{
				this.Add (name, value);
				return this;
			}
		}

		private static IQueryable<T> Set<T>(this DbContext context, Type type)
		{
            // Get the generic type definition
            MethodInfo method = typeof(DbContext).GetMethod(nameof(DbContext.Set), BindingFlags.Public | BindingFlags.Instance);
            // Build a method with the specific type argument you're interested in
            method = method.MakeGenericMethod(type);
			return method.Invoke(context, null) as IQueryable<T>;
        }
		// Utils
		static IQueryable<T> BuildQuery<T> (DbContext entityManager, Type resultClass, QueryMap fields, String [] orderBy) where T : class
		{
			String className;

			if (resultClass != null) {
				className = resultClass.Name;
			} else {
				className = typeof(T).Name;
			}

			StringBuilder sql = new StringBuilder(1024);
			sql.Append("SELECT * FROM " + CaseConvert.CamelToUnderscore(className) + " o");
			List<DbParameter> parameters = new List<DbParameter> (256);
		
			if (fields.Count > 0) {
				sql.Append(" WHERE ");
		
				foreach(var item in fields) {
					if ((item.Value is IList) || (item.Value is Array)) {
						sql.Append (String.Format ("o.{0} = ANY (@{1}) and ", item.Key, item.Key));
					} else {
						sql.Append(String.Format("o.{0} = @{1} and ", item.Key, item.Key));
					}

					parameters.Add (new NpgsqlParameter (item.Key, item.Value));
				}
		
				sql.Length = sql.Length -5;
			}
		
			if (orderBy != null && orderBy.Length > 0) {
				sql.Append(" order by ");
		
				foreach (String field in orderBy) {
					sql.Append ("o." + field + ",");
				}
		
				sql.Length = sql.Length - 1;
			}

			IQueryable<T> query;

			if (resultClass != null) {
				query = Set<T>(entityManager, resultClass).FromSql(sql.ToString(), parameters.ToArray());
            } else {
				query = entityManager.Set<T>().FromSql(sql.ToString(), parameters.ToArray());
            }

			return query;
		}
		// Utils
		public static Task<List<T>> Find<T>(DbContext entityManager, Type resultClass, QueryMap fields, String[] orderBy, int? startPosition, int? maxResult) where T : class {
			return Task.Run<List<T>>(() => {
				var query = DbUtils.BuildQuery<T>(entityManager, resultClass, fields, orderBy);

                if (startPosition != null) {
//                    query.setFirstResult(startPosition);
                }

                if (maxResult != null) {
//                    query.setMaxResults(maxResult);
                }

				return query.ToListAsync().ContinueWith(taskResult => taskResult.Result);
            });
        }
		// Utils
		public static Task<T> FindOne<T> (DbContext entityManager, Type resultClass, QueryMap fields) where T : class {
			return BuildQuery<T> (entityManager, resultClass, fields, null).SingleAsync ();
		}
		// Utils
		public static Task<T> Insert<T>(Object userTransaction, DbContext entityManager, T obj) where T : class {
			return Task.Run<T>(() => {
				if (userTransaction != null) entityManager.Database.BeginTransaction ();
				entityManager.Add (obj);
				entityManager.SaveChanges();
				if (userTransaction != null) entityManager.Database.CommitTransaction ();
				return obj;
			});
		}
		// Utils
		public static Task<T> Update<T>(Object userTransaction, DbContext entityManager, T obj) where T : class {
			return Task.Run<T>(() => {
				if (userTransaction != null) entityManager.Database.BeginTransaction ();

				try {
					entityManager.Update (obj);
					entityManager.SaveChanges ();
				} catch (Exception e) {
					Console.WriteLine ("DbUtils.Update : Fail : {0} - obj : {1}", e, obj);
					throw e;
				}

				if (userTransaction != null) entityManager.Database.CommitTransaction ();
				return obj;
			});
        }
		// Utils
		public static Task<T> DeleteOne<T> (DbContext entityManager, T obj) where T : class {
			return Task.Run<T> (() => {
				entityManager.Remove (obj);
				entityManager.SaveChanges();
				return obj;
			});
		}

	}
}
