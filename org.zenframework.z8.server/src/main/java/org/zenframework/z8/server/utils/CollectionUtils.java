package org.zenframework.z8.server.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class CollectionUtils {

	private static final Log LOG = LogFactory.getLog(CollectionUtils.class);

	private CollectionUtils() {}

	public static boolean equals(Object o1, Object o2, String compareKey) {
		return equals(o1, o2, compareKey, 0);
	}

	public static boolean equals(Map<?, ?> o1, Map<?, ?> o2, String compareKey) {
		return equals(o1, o2, compareKey, 0);
	}

	public static boolean equals(List<?> o1, List<?> o2, String compareKey) {
		return equals(o1, o2, compareKey, 0);
	}

	public static boolean contains(List<?> arr, Object obj, String compareKey) {
		for (Object o : arr) {
			if (equals(o, obj, compareKey))
				return true;
		}
		return false;
	}

	public static List<?> diff(List<?> a1, List<?> a2, String compareKey) {
		List<Object> diff = new ArrayList<Object>(a1.size());
		for (Object o : a1) {
			if (!contains(a2, o, compareKey))
				diff.add(o);
		}
		return diff;
	}

	private static boolean equals(Object o1, Object o2, String compareKey, int indent) {
		boolean eq;
		if ((o1 == null || o1.toString().isEmpty()) && (o2 == null || o2.toString().isEmpty()))
			eq = true;
		else if (o1 != null && (o2 == null || o2.toString().isEmpty()) || (o2 == null || o2.toString().isEmpty())
				&& o2 != null)
			eq = false;
		else if (o1 instanceof Map && o2 instanceof Map && !equals((Map<?, ?>) o1, (Map<?, ?>) o2, compareKey, indent + 1))
			eq = false;
		else if (o1 instanceof Collection && o2 instanceof Collection
				&& !equals((List<?>) o1, (List<?>) o2, compareKey, indent + 1))
			eq = false;
		else
			eq = o1.equals(o2);
		if (!eq && LOG.isDebugEnabled())
			LOG.debug(tab(indent) + o1 + " != " + o2);
		return eq;
	}

	private static boolean equals(Map<?, ?> o1, Map<?, ?> o2, String compareKey, int indent) {
		boolean eq = true;
		if (o1 != null && o2 == null || o1 == null && o2 != null || o1.size() != o2.size()) {
			eq = false;
		} else if (o1 != null && !o1.isEmpty() || o2 != null && !o2.isEmpty()) {
			for (Map.Entry<?, ?> entry : o1.entrySet()) {
				if (!equals(entry.getValue(), o2.get(entry.getKey()), compareKey, indent + 1)) {
					eq = false;
					break;
				}
			}
		}
		if (!eq && LOG.isDebugEnabled())
			LOG.debug(tab(indent) + o1 + " != " + o2);
		return eq;
	}

	private static boolean equals(List<?> o1, List<?> o2, String compareKey, int indent) {
		boolean eq = true;
		if (o1 != null && o2 == null || o1 == null && o2 != null || o1.size() != o2.size()) {
			LOG.debug(o1.toString() + " != " + o2.toString());
			return false;
		} else if (o1 != null && !o1.isEmpty() || o2 != null && !o2.isEmpty()) {
			Collections.sort(o1, new ElementComparator(compareKey));
			Collections.sort(o2, new ElementComparator(compareKey));
			for (int i = 0; i < o1.size(); i++) {
				if (!equals(o1.get(i), o2.get(i), compareKey, indent + 1)) {
					eq = false;
					break;
				}
			}
		}
		if (!eq && LOG.isDebugEnabled())
			LOG.debug(tab(indent) + o1 + " != " + o2);
		return eq;
	}

	private static String tab(int n) {
		char c[] = new char[n * 2];
		Arrays.fill(c, ' ');
		return new String(c);
	}

	public static class ElementComparator implements Comparator<Object> {

		private final String compareKey;

		public ElementComparator(String compareKey) {
			this.compareKey = compareKey;
		}

		@Override
		public int compare(Object o1, Object o2) {
			if (o1 == null && o2 == null)
				return 0;
			if (o1 != null && o2 == null)
				return 1;
			if (o1 == null && o2 != null)
				return -1;
			return toString(o1).compareTo(toString(o2));
		}

		private String toString(Object o) {
			if (o instanceof Map) {
				try {
					return "2" + toString(((Map<?, ?>) o).get(compareKey));
				} catch (NullPointerException e) {
					throw new NullPointerException("Map " + o + " doesn't contain key '" + compareKey + "'");
				}
			}
			if (o instanceof List) {
				List<?> l = (List<?>) o;
				if (l.isEmpty())
					return "3[]";
				Collections.sort(l, new ElementComparator(compareKey));
				return "3" + toString(l.get(0));
			}
			return "1" + o.toString();
		}

	}

}
