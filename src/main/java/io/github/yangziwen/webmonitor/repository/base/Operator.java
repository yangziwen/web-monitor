package io.github.yangziwen.webmonitor.repository.base;

public enum Operator {

	eq {
		@Override
		public Condition buildCondition(String stmt, String fullKey, String placeholderPrefix,
				String placeholderSuffix) {
			return new Condition(stmt, " = ", wrapPlaceholder(fullKey, placeholderPrefix, placeholderSuffix), fullKey);
		}
	},
	ne {
		@Override
		public Condition buildCondition(String stmt, String fullKey, String placeholderPrefix,
				String placeholderSuffix) {
			return new Condition(stmt, " != ", wrapPlaceholder(fullKey, placeholderPrefix, placeholderSuffix), fullKey);
		}
	},
	gt {
		@Override
		public Condition buildCondition(String stmt, String fullKey, String placeholderPrefix,
				String placeholderSuffix) {
			return new Condition(stmt, " > ", wrapPlaceholder(fullKey, placeholderPrefix, placeholderSuffix), fullKey);
		}
	},
	ge {
		@Override
		public Condition buildCondition(String stmt, String fullKey, String placeholderPrefix,
				String placeholderSuffix) {
			return new Condition(stmt, " >= ", wrapPlaceholder(fullKey, placeholderPrefix, placeholderSuffix), fullKey);
		}
	},
	lt {
		@Override
		public Condition buildCondition(String stmt, String fullKey, String placeholderPrefix,
				String placeholderSuffix) {
			return new Condition(stmt, " < ", wrapPlaceholder(fullKey, placeholderPrefix, placeholderSuffix), fullKey);
		}
	},
	le {
		@Override
		public Condition buildCondition(String stmt, String fullKey, String placeholderPrefix,
				String placeholderSuffix) {
			return new Condition(stmt, " <= ", wrapPlaceholder(fullKey, placeholderPrefix, placeholderSuffix), fullKey);
		}
	},
	contain {
		@Override
		public Condition buildCondition(String stmt, String fullKey, String placeholderPrefix,
				String placeholderSuffix) {
			String placeholder = concatFunc.render("'%'",
					wrapPlaceholder(fullKey, placeholderPrefix, placeholderSuffix), "'%'");
			return new Condition(stmt, " like ", placeholder, fullKey);
		}
	},
	not_contain {
		@Override
		public Condition buildCondition(String stmt, String fullKey, String placeholderPrefix,
				String placeholderSuffix) {
			String placeholder = concatFunc.render("'%'",
					wrapPlaceholder(fullKey, placeholderPrefix, placeholderSuffix), "'%'");
			return new Condition(stmt, " not like ", placeholder, fullKey);

		}
	},
	start_with {
		@Override
		public Condition buildCondition(String stmt, String fullKey, String placeholderPrefix,
				String placeholderSuffix) {
			String placeholder = concatFunc.render(wrapPlaceholder(fullKey, placeholderPrefix, placeholderSuffix),
					"'%'");
			return new Condition(stmt, " like ", placeholder, fullKey);
		}
	},
	not_start_with {
		@Override
		public Condition buildCondition(String stmt, String fullKey, String placeholderPrefix,
				String placeholderSuffix) {
			String placeholder = concatFunc.render(wrapPlaceholder(fullKey, placeholderPrefix, placeholderSuffix),
					"'%'");
			return new Condition(stmt, " not like ", placeholder, fullKey);
		}
	},
	end_with {
		@Override
		public Condition buildCondition(String stmt, String fullKey, String placeholderPrefix,
				String placeholderSuffix) {
			String placeholder = concatFunc.render("'%'",
					wrapPlaceholder(fullKey, placeholderPrefix, placeholderSuffix));
			return new Condition(stmt, " like ", placeholder, fullKey);
		}
	},
	not_end_with {
		@Override
		public Condition buildCondition(String stmt, String fullKey, String placeholderPrefix,
				String placeholderSuffix) {
			String placeholder = concatFunc.render("'%'",
					wrapPlaceholder(fullKey, placeholderPrefix, placeholderSuffix));
			return new Condition(stmt, " not like ", placeholder, fullKey);
		}
	},
	in {
		@Override
		public Condition buildCondition(String stmt, String fullKey, String placeholderPrefix,
				String placeholderSuffix) {
			return new Condition(stmt, " in ",
					"(" + wrapPlaceholder(fullKey, placeholderPrefix, placeholderSuffix) + ")", fullKey);
		}
	},
	not_in {
		@Override
		public Condition buildCondition(String stmt, String fullKey, String placeholderPrefix,
				String placeholderSuffix) {
			return new Condition(stmt, " not in ",
					"(" + wrapPlaceholder(fullKey, placeholderPrefix, placeholderSuffix) + ")", fullKey);
		}
	},
	is_null {
		@Override
		public Condition buildCondition(String stmt, String fullKey, String placeholderPrefix,
				String placeholderSuffix) {
			return new Condition(stmt, " is null ", "", wrapPlaceholder(fullKey, placeholderPrefix, placeholderSuffix));
		}
	},
	is_not_null {
		@Override
		public Condition buildCondition(String stmt, String fullKey, String placeholderPrefix,
				String placeholderSuffix) {
			return new Condition(stmt, " is not null ", "",
					wrapPlaceholder(fullKey, placeholderPrefix, placeholderSuffix));
		}
	};

	static final String __ = "__";

	private static VarArgsSQLFunction concatFunc = new VarArgsSQLFunction("concat(", ", ", ")");

	private static final String DEFAULT_PLACEHOLDER_PREFIX = ":";

	private static final String DEFAULT_PLACEHOLDER_SUFFIX = "";

	protected String wrapPlaceholder(String placeholder, String prefix, String suffix) {
		return prefix + placeholder + suffix;
	}

	public Condition buildCondition(String stmt, String fullKey) {
		return buildCondition(stmt, fullKey, DEFAULT_PLACEHOLDER_PREFIX, DEFAULT_PLACEHOLDER_SUFFIX);
	}

	public abstract Condition buildCondition(String stmt, String fullKey, String placeholderPrefix,
			String placeholderSuffix);

}
