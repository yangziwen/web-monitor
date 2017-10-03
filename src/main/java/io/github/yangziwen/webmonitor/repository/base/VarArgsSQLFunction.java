package io.github.yangziwen.webmonitor.repository.base;

public class VarArgsSQLFunction {

	private final String begin;
	private final String sep;
	private final String end;

	public VarArgsSQLFunction(String begin, String sep, String end) {
		this.begin = begin;
		this.sep = sep;
		this.end = end;
	}

	public String render(Object... args) {
		StringBuilder buf = new StringBuilder().append(begin);
		for (int i = 0; i < args.length; i++) {
			buf.append(transformArgument(args[i].toString()));
			if (i < args.length - 1) {
				buf.append(sep);
			}
		}
		return buf.append(end).toString();
	}

	protected String transformArgument(String arg) {
		return arg;
	}

}
