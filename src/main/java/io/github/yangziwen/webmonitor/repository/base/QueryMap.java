package io.github.yangziwen.webmonitor.repository.base;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class QueryMap extends HashMap<String, Object> {

	private static final long serialVersionUID = 1L;

	private AtomicInteger sequence = new AtomicInteger(101);

	/**
	 * 表达or的queryMap中不能再嵌套表达or的queryMap
	 */
	private boolean isOrMap = false;

	public QueryMap() {
		super();
	}

	public QueryMap(int initialCapacity) {
		super(initialCapacity);
	}

	public QueryMap param(String key, Object value) {
		put(key, value);
		return this;
	}

	public QueryMap param(boolean isValid, String key, Object value) {
	    if (!isValid) {
	        return this;
	    }
	    return param(key, value);
	}

	public QueryMap select(String...fields) {
	    return param(RepoKeys.SELECT, fields);
	}

	private String generateOrMapKey() {
		return sequence.getAndIncrement() + RepoKeys.OR;
	}

	public QueryMap or(Map<String, Object> orMap) {
		return or(generateOrMapKey(), orMap);
	}

	public QueryMap or(String orMapKey, Map<String, Object> orMap) {
		ensureOrMap(orMapKey).putAll(orMap);
		return this;
	}

	public QueryMap or(String orMapKey, String key, String value) {
		ensureOrMap(orMapKey).param(key, value);
		return this;
	}

	public QueryMap orderBy(String key, String direct) {
		ensureOrderByMap().put(key, direct);
		return this;
	}

	public QueryMap orderByAsc(String key) {
		return orderBy(key, RepoKeys.ORDER_ASC);
	}

	public QueryMap orderByDesc(String key) {
		return orderBy(key, RepoKeys.ORDER_DESC);
	}

	public QueryMap groupBy(String value) {
		ensureGroupByList().add(value);
		return this;
	}

	public QueryMap having(boolean isValid, String key, Object value) {
	    if (!isValid) {
	        return this;
	    }
	    return having(key, value);
	}

	public QueryMap having(String key, Object value) {
	    ensureHavingMap().put(key, value);
	    return this;
	}

	@SuppressWarnings("unchecked")
    private Map<String, Object> ensureHavingMap() {
	    Map<String, Object> havingMap = (Map<String, Object>) get(RepoKeys.HAVING);
        if (havingMap == null) {
            havingMap = new LinkedHashMap<String, Object>();
            put(RepoKeys.HAVING, havingMap);
        }
        return havingMap;
	}

	private QueryMap ensureOrMap(String orMapKey) {
		if (this.isOrMap) {
			throw new UnsupportedOperationException("Nested orMap is not supported!");
		}
		QueryMap orMap = (QueryMap) get(orMapKey);
		if (orMap == null) {
			orMap = new QueryMap();
			put(orMapKey, orMap);
		}
		orMap.isOrMap = true;
		return this;
	}

	@SuppressWarnings("unchecked")
	private Map<String, String> ensureOrderByMap() {
		Map<String, String> orderByMap = (Map<String, String>) get(RepoKeys.ORDER_BY);
		if (orderByMap == null) {
			orderByMap = new LinkedHashMap<String, String>();
			put(RepoKeys.ORDER_BY, orderByMap);
		}
		return orderByMap;
	}

	@SuppressWarnings("unchecked")
	private List<String> ensureGroupByList() {
		List<String> groupByList = (List<String>) get(RepoKeys.GROUP_BY);
		if (groupByList == null) {
			groupByList = new ArrayList<String>();
			put(RepoKeys.GROUP_BY, groupByList);
		}
		return groupByList;
	}

	public static QueryMap emptyMap() {
		return EMPTY_MAP;
	}

	private static final QueryMap EMPTY_MAP = new QueryMap(0) {

		private static final long serialVersionUID = 1L;

		@Override
		public Object put(String key, Object value) {
			throw new UnsupportedOperationException();
		}

		@Override
		public Object remove(Object key) {
			return null;
		}

		@Override
		public void putAll(Map<? extends String, ? extends Object> m) {
			throw new UnsupportedOperationException();
		}

		@Override
		public void clear() {
		}
	};

}
