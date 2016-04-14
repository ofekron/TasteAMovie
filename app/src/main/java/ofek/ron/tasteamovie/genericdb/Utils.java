package ofek.ron.tasteamovie.genericdb;

public class Utils {
	public static String[] toStrings(final Object[] args) {
		final String[] strings = new String[args.length];

		for (int i = 0; i < args.length; i++) {
			strings[i] = args[i].toString();
		}
		return strings;
	}

}
