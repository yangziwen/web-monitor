package io.github.yangziwen.webmonitor.repository.base;

public class RepoKeys {

	private RepoKeys() {
	}

	public static final String OR = "__or";

	public static final String ORDER_ASC = "asc";

	public static final String ORDER_DESC = "desc";

	public static final String SELECT = "__select";

	public static final String ORDER_BY = "__order_by";

	public static final String GROUP_BY = "__group_by";

	public static final String HAVING = "__having";

	public static final String OFFSET = "__offset";

	public static final String LIMIT = "__limit";

	public static boolean isRepoKey(String key) {
	    if (SELECT.equalsIgnoreCase(key)) {
	        return true;
	    }
		if (OR.equalsIgnoreCase(key)) {
			return true;
		}
		if (ORDER_BY.equalsIgnoreCase(key)) {
			return true;
		}
		if (GROUP_BY.equalsIgnoreCase(key)) {
			return true;
		}
		if (HAVING.equalsIgnoreCase(key)) {
		    return true;
		}
		if (OFFSET.equalsIgnoreCase(key)) {
			return true;
		}
		if (LIMIT.equalsIgnoreCase(key)) {
			return true;
		}
		return false;

	}

}
