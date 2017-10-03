package io.github.yangziwen.webmonitor.repository.base;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.sql.DataSource;

import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.sql2o.Connection;
import org.sql2o.Query;
import org.sql2o.Sql2o;

import io.github.yangziwen.webmonitor.util.ReflectionUtil;

public class ReadOnlyBaseRepository<E> {

	protected static final Map<String, Object> EMPTY_PARAMS = Collections.emptyMap();

	protected ModelMapping<E> modelMapping = ModelMapping
			.newInstance(ReflectionUtil.<E> getSuperClassGenericType(this.getClass(), 0));

	protected Sql2o sql2o;

	protected ReadOnlyBaseRepository(DataSource dataSource) {
		sql2o = new Sql2o(dataSource);
	}

	public E first() {
		return first(EMPTY_PARAMS);
	}

	public E first(Map<String, Object> params) {
		List<E> list = list(params);
		return list != null && list.size() > 0 ? list.get(0) : null;
	}

	public List<E> list() {
		return list(EMPTY_PARAMS);
	}

	public List<E> list(Map<String, Object> params) {
		return list(0, Integer.MAX_VALUE, params);
	}

	public List<E> list(int offset, int limit, Map<String, Object> params) {
		params = new HashMap<String, Object>(params);
		StringBuilder buff = new StringBuilder();
		appendSelect(params, buff);
		appendFrom(params, buff);
		appendWhere(params, buff);
		appendGroupBy(params, buff);
		appendHaving(params, buff);
		appendOrderBy(params, buff);
		appendLimit(offset, limit, buff);
		return doList(processSqlAndParams(buff.toString(), params), params);
	}

	protected List<E> doList(String sql, Map<String, Object> params) {
		try (Connection conn = sql2o.open()) {
			Query query = conn.createQuery(sql);
			for (Entry<String, Object> entry : params.entrySet()) {
				query.addParameter(entry.getKey(), entry.getValue());
			}
			return query.executeAndFetch(modelMapping.clazz);
		}
	}

	public Integer count() {
		return count(EMPTY_PARAMS);
	}

	public Integer count(Map<String, Object> params) {
		params = new HashMap<String, Object>(params);
		StringBuilder buff = new StringBuilder();
		buff.append("SELECT COUNT(1) ");
		if (params.containsKey(RepoKeys.GROUP_BY)) {
			buff.append(" FROM ( SELECT 1 ");
			appendFrom(params, buff);
			appendWhere(params, buff);
			buff.append(" ) result ");
		} else {
			appendFrom(params, buff);
			appendWhere(params, buff);
		}
		return doCount(processSqlAndParams(buff.toString(), params), params);
	}

	protected Integer doCount(String sql, Map<String, Object> params) {
		try (Connection conn = sql2o.open()) {
			Query query = conn.createQuery(sql);
			for (Entry<String, Object> entry : params.entrySet()) {
				query.addParameter(entry.getKey(), entry.getValue());
			}
			return query.executeScalar(Integer.class);
		}
	}

	// ------- 内部方法 -------- //

	protected String processSqlAndParams(String sql, Map<String, Object> params) {
		List<String> keysToDelete = new ArrayList<String>();
		for (Entry<String, Object> entry : params.entrySet()) {
			if (!(entry.getValue() instanceof Collection)) {
				continue;
			}
			Collection<?> coll = (Collection<?>) entry.getValue();
			StringBuilder keys = new StringBuilder();
			int idx = 0;
			for (Object obj : coll) {
				String k = entry.getKey() + "__" + idx;
				keys.append(idx > 0 ? ", :" : "").append(k);
				params.put(k, obj);
				idx ++;
			}
			sql = sql.replace(entry.getKey(), keys.toString());
			keysToDelete.add(entry.getKey());
		}
		for (String key : keysToDelete) {
			params.remove(key);
		}
		return sql;
	}

	protected void appendSelect(Map<String, Object> params, StringBuilder buff) {
		buff.append("SELECT ");
		String[] fields = (String[]) params.remove(RepoKeys.SELECT);
		if (fields != null && fields.length > 0) {
		    buff.append(fields[0]);
		    for (int i = 1; i < fields.length; i++) {
		        buff.append(", ").append(fields[i]);
		    }
		    return;
		}
		int i = 0;
		for (String stmt : modelMapping.getSelectStmts()) {
			if (i++ > 0) {
				buff.append(", ");
			}
			buff.append(stmt);
		}
	}

	protected void appendFrom(Map<String, Object> params, StringBuilder buff) {
		buff.append(" FROM ").append(modelMapping.getTable(params));
	}

	@SuppressWarnings("unchecked")
	protected void appendWhere(Map<String, Object> params, StringBuilder buff) {
		List<Map<String, Object>> orParamsList = new ArrayList<Map<String, Object>>();
		List<String> keys = new ArrayList<String>(params.keySet());
		for (String key : keys) {
			if (key != null && key.toLowerCase().endsWith(RepoKeys.OR)) {
				Map<String, Object> orParams = (Map<String, Object>) params.remove(key);
				if (MapUtils.isNotEmpty(orParams)) {
					orParamsList.add(orParams);
				}
			}
		}
		buff.append(" WHERE ");
		appendAndConditions(params, buff);
		for (Map<String, Object> orParams : orParamsList) {
			buff.append(" AND (");
			appendOrConditions(orParams, buff);
			buff.append(")");
			params.putAll(orParams);
		}
	}

	protected void appendGroupBy(Map<String, Object> params, StringBuilder buff) {
		Object groupBy = params.remove(RepoKeys.GROUP_BY);
		if (groupBy == null) {
			return;
		}
		if (groupBy instanceof Collection) {
			buff.append(" GROUP BY ");
			int i = 0;
			for (Object obj : (Collection<?>) groupBy) {
				if (i++ > 0) {
					buff.append(", ");
				}
				buff.append(obj);
			}
			return;
		}
		buff.append(" GROUP BY ").append(groupBy);
	}

	@SuppressWarnings("unchecked")
    protected void appendHaving(Map<String, Object> params, StringBuilder buff) {
	    Object having = params.remove(RepoKeys.HAVING);
	    if (having == null) {
	        return;
	    }
	    if (having instanceof String) {
	        String str = (String) having;
	        if (StringUtils.isNotBlank(str)) {
	            buff.append(" HAVING ").append(str);
	        }
	        return;
	    }
	    if (having instanceof Map) {
	        Map<String, Object> havingMap = (Map<String, Object>) having;
	        if (havingMap.size() > 0) {
	            buff.append(" HAVING ");
	            int i = 0;
	            for (Entry<String, Object> entry : havingMap.entrySet()) {
	                Condition condition = Condition.parse(entry.getKey(), modelMapping);
	                if (condition == null) {
	                    continue;
	                }
	                if (i++ > 0) {
	                    buff.append(" AND ");
	                }
	                condition.setPlaceholder(condition.getPlaceholder() + RepoKeys.HAVING);
	                buff.append(condition.toSql());
	                params.put(entry.getKey() + RepoKeys.HAVING, entry.getValue());
	            }
	        }
	    }
	}

	@SuppressWarnings("unchecked")
	protected void appendOrderBy(Map<String, Object> params, StringBuilder buff) {
		Object orderBy = params.remove(RepoKeys.ORDER_BY);
		if (orderBy == null) {
			return;
		}
		if (orderBy instanceof String) {
			String str = (String) orderBy;
			if (StringUtils.isNotBlank(str)) {
				buff.append(" ORDER BY ").append(str);
			}
			return;
		}
		if (orderBy instanceof Map) {
			Map<String, Object> orderByMap = (Map<String, Object>) orderBy;
			if (MapUtils.isEmpty(orderByMap)) {
				return;
			}
			int i = 0;
			for (Entry<String, Object> entry : orderByMap.entrySet()) {
				String key = modelMapping.getColumnByField(entry.getKey());
				if (StringUtils.isBlank(key)) {
					key = entry.getKey();
				}
				String value = entry.getValue() != null ? entry.getValue().toString() : "";
				if (StringUtils.isBlank(key)) {
					continue;
				}
				if (i++ > 0) {
					buff.append(", ");
				} else {
					buff.append(" ORDER BY ");
				}
				buff.append(key).append(" ").append(value);
			}
		}
	}

	protected void appendLimit(int offset, int limit, StringBuilder buff) {
		if (limit <= 0) {
			return;
		}
		if (offset < 0) {
			offset = 0;
		}
		buff.append(" LIMIT ").append(limit).append(" OFFSET ").append(offset);
	}

	protected void appendAndConditions(Map<String, Object> params, StringBuilder buff) {
		buff.append(" 1 = 1 ");
		for (Entry<String, Object> entry : params.entrySet()) {
			if (RepoKeys.isRepoKey(entry.getKey())) {
				continue;
			}
			if (entry.getValue() instanceof Object[]) {
				entry.setValue(Arrays.asList((Object[]) entry.getValue()));
			}
			Condition condition = Condition.parse(entry.getKey(), modelMapping);
			if (condition == null) {
				continue;
			}
			buff.append(" AND ").append(condition.toSql());
		}
	}

	protected void appendOrConditions(Map<String, Object> params, StringBuilder buff) {
		buff.append(" 1 = 2 ");
		for (Entry<String, Object> entry : params.entrySet()) {
			if (RepoKeys.isRepoKey(entry.getKey())) {
				continue;
			}
			if (entry.getValue() instanceof Object[]) {
				entry.setValue(Arrays.asList((Object[]) entry.getValue()));
			}
			Condition condition = Condition.parse(entry.getKey(), modelMapping);
			if (condition == null) {
				continue;
			}
			buff.append(" OR ").append(condition.toSql());
		}
	}

}
