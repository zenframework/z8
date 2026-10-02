package org.zenframework.z8.server.reports.poi;

import org.zenframework.z8.server.base.table.value.Aggregation;
import org.zenframework.z8.server.types.decimal;
import org.zenframework.z8.server.types.integer;
import org.zenframework.z8.server.types.primary;
import org.zenframework.z8.server.types.string;

public abstract class Aggregator {

	private static final String Separator = ",";

	public static Aggregator newInstance(Aggregation aggregation) {
		switch (aggregation) {
		case Sum:
			return new Sum();
		case Average:
			return new Average();
		case Count:
			return new Count();
		case Min:
			return new Min();
		case Max:
			return new Max();
		case Concat:
			return new Concat(Separator);
		default:
			return new Default();
		}
	}

	public abstract void accumulate(Object value);

	public abstract Object get();

	private static class Sum extends Aggregator {
		private double sum;

		@Override
		public void accumulate(Object value) {
			Double number = toDouble(value);

			if (number != null)
				sum += number;
		}

		@Override
		public Object get() {
			return new decimal(sum);
		}
	}

	private static class Average extends Aggregator {
		private double sum;
		private int numbers;

		@Override
		public void accumulate(Object value) {
			Double number = toDouble(value);

			if (number != null) {
				sum += number;
				numbers++;
			}
		}

		@Override
		public Object get() {
			return new decimal(numbers > 0 ? sum / numbers : 0);
		}
	}

	private static class Count extends Aggregator {
		private int count;

		@Override
		public void accumulate(Object value) {
			count++;
		}

		@Override
		public Object get() {
			return new integer(count);
		}
	}

	private static abstract class Extremum extends Aggregator {
		private Object value;

		@Override
		public void accumulate(Object item) {
			if (value == null || isBetter(compare(item, value)))
				value = item;
		}

		@Override
		public Object get() {
			return value;
		}

		protected abstract boolean isBetter(int comparison);

		@SuppressWarnings("unchecked")
		private static int compare(Object item, Object value) {
			return item instanceof Comparable && value instanceof Comparable
					? ((Comparable<Object>) item).compareTo(value) : 0;
		}
	}

	private static class Min extends Extremum {
		@Override
		protected boolean isBetter(int comparison) {
			return comparison < 0;
		}
	}

	private static class Max extends Extremum {
		@Override
		protected boolean isBetter(int comparison) {
			return comparison > 0;
		}
	}

	private static class Concat extends Aggregator {
		private final String separator;

		private StringBuilder concat;

		Concat(String separator) {
			this.separator = separator;
		}

		@Override
		public void accumulate(Object value) {
			if (concat == null)
				concat = new StringBuilder();

			if (concat.length() != 0)
				concat.append(separator);

			concat.append(value.toString());
		}

		@Override
		public Object get() {
			return new string(concat != null ? concat.toString() : "");
		}
	}

	private static class Default extends Aggregator {
		private Object first;
		private double sum;
		private int numbers;

		@Override
		public void accumulate(Object value) {
			if (first == null)
				first = value;

			Double number = toDouble(value);

			if (number != null) {
				sum += number;
				numbers++;
			}
		}

		@Override
		public Object get() {
			return numbers > 0 ? new decimal(sum) : first;
		}
	}

	private static Double toDouble(Object value) {
		if (value instanceof Number)
			return ((Number) value).doubleValue();

		if (value instanceof integer)
			return (double) ((integer) value).getInt();

		if (value instanceof decimal)
			return ((decimal) value).getDouble();

		if (value instanceof primary)
			return null;

		try {
			return Double.parseDouble(value.toString().trim().replace(',', '.'));
		} catch (Exception e) {
			return null;
		}
	}
}
