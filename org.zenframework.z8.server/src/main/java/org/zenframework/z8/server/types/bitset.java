package org.zenframework.z8.server.types;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.zenframework.z8.server.db.DatabaseVendor;
import org.zenframework.z8.server.db.FieldType;
import org.zenframework.z8.server.json.parser.JsonArray;
import org.zenframework.z8.server.runtime.META;
import org.zenframework.z8.server.runtime.RCollection;
import org.zenframework.z8.server.types.sql.sql_bitset;

public class bitset extends primary {
	private static final long serialVersionUID = 1L;

	static private final META Meta = META.meta(bitset.class);

	private static final int LongSize = 64;

	private static final Comparator<BitSet> BitSetComparator = new Comparator<BitSet>() {
		@Override
		public int compare(BitSet o1, BitSet o2) {
			int i = 0, j = 0;
			do {
				i = o1.nextSetBit(i + 1);
				j = o2.nextSetBit(j + 1);
				if (i < j)
					return -1;
				if (i > j)
					return 1;
			} while (i == j && i != -1);
			return 0;
		}
	};

	private BitSet value;

	public bitset() {
		this(0);
	}

	public bitset(int size) {
		this(new BitSet(size));
	}

	public bitset(BitSet value) {
		this.value = value;
	}

	public bitset(String str) {
		init(str);
	}

	public bitset(List<Integer> bits) {
		this(bits, 0);
	}

	public bitset(List<Integer> bits, int offset) {
		init(bits, offset);
	}

	private void init(List<Integer> bits, int offset) {
		value = new BitSet(Collections.max(bits) + offset);
		for (int bit : bits)
			value.set(bit + offset);
	}

	private void init(String str) {
		value = new BitSet(str.length());
		for (int i = 0, n = str.length(); i < n; i++) {
			char c = str.charAt(i);
			if (c == '1')
				value.set(i);
			else if (c != '0')
				throw new RuntimeException("Incorrect bitset");
		}
	}

	@Override
	public META getMeta() {
		return Meta;
	}

	@Override
	public int compareTo(primary o) {
		if (!(o instanceof bitset))
			throw new RuntimeException(o.getClass() + " is not bitset");

		return BitSetComparator.compare(value, ((bitset) o).get());
	}

	@Override
	public int hashCode() {
		return value.hashCode();
	}

	@Override
	public boolean equals(Object o) {
		return o != null && (o instanceof bitset) && value.equals(((bitset) o).get());
	}

	@Override
	public String toString() {
		StringBuilder str = new StringBuilder(value.size());
		for (int i = 0, n = value.size(); i < n; i++)
			str.append(value.get(i) ? '1' : '0');
		return str.toString();
	}

	@Override
	public FieldType type() {
		return FieldType.Bitset;
	}

	@Override
	public String toDbConstant(DatabaseVendor dbtype) {
		return "B'" + toString() + "'";
	}

	public BitSet get() {
		return value;
	}

	public int size() {
		return value.size();
	}

	public int cardinality() {
		return value.cardinality();
	}

	public boolean get(int index) {
		return value.get(index);
	}

	public void set(int index) {
		set(index, true);
	}

	public void set(int index, boolean value) {
		this.value.set(index, value);
	}

	public void flip(int index) {
		value.flip(index);
	}

	public bitset not() {
		bitset result = one(value.size() / LongSize);
		result.get().andNot(value);
		return result;
	}

	public bitset and(bitset x) {
		bitset result = new bitset(BitSet.valueOf(value.toLongArray()));
		result.get().and(x.get());
		return result;
	}

	public bitset or(bitset x) {
		bitset result = new bitset(BitSet.valueOf(value.toLongArray()));
		result.get().or(x.get());
		return result;
	}

	public bitset xor(bitset x) {
		bitset result = new bitset(BitSet.valueOf(value.toLongArray()));
		result.get().xor(x.get());
		return result;
	}

	public sql_bitset sql_bitset() {
		return new sql_bitset(this);
	}

	public void operatorAssign(string value) {
		init(value.get());
	}

	public void operatorAssign(RCollection<integer> bits) {
		List<Integer> ints = new ArrayList<Integer>(bits.size());
		for (integer bit : bits)
			ints.add(bit.getInt());
		init(ints, 0);
	}

	public bitset operatorNot() {
		return not();
	}

	public bitset operatorAnd(bitset x) {
		return and(x);
	}

	public bitset operatorOr(bitset x) {
		return or(x);
	}

	public bitset operatorXor(bitset x) {
		return xor(x);
	}

	public bool operatorEqu(bitset x) {
		return new bool(equals(x));
	}

	public bool operatorNotEqu(bitset x) {
		return new bool(!equals(x));
	}

	public bool operatorLess(bitset x) {
		return new bool(compareTo(x) < 0);
	}

	public bool operatorMore(bitset x) {
		return new bool(compareTo(x) > 0);
	}

	public bool operatorLessEqu(bitset x) {
		return new bool(compareTo(x) <= 0);
	}

	public bool operatorMoreEqu(bitset x) {
		return new bool(compareTo(x) >= 0);
	}

	public integer z8_size() {
		return new integer(size());
	}

	public integer z8_cardinality() {
		return new integer(cardinality());
	}

	public bool z8_get(integer index) {
		return new bool(get(index.getInt()));
	}

	public void z8_set(integer index) {
		set(index.getInt(), true);
	}

	public void z8_set(integer index, bool value) {
		set(index.getInt(), value.get());
	}

	public void z8_flip(integer index) {
		flip(index.getInt());
	}

	private static bitset one(int words) {
		long values[] = new long[words];
		Arrays.fill(values, -1L);
		return new bitset(BitSet.valueOf(values));
	}

	public static bitset parse(String str) {
		return new bitset(str);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static bitset parseArray(String str, int offset) {
		if (str.charAt(0) != '{' || str.charAt(str.length() - 1) != '}')
			throw new RuntimeException("Incorrect bitmap array: " + str);
		JsonArray array = new JsonArray('[' + str.substring(1, str.length() - 1) + ']');
		return new bitset((List) array, offset);
	}

	public static bitset z8_bitset(integer size) {
		return new bitset(size.getInt());
	}

	public static bitset z8_parse(string string) {
		return parse(string.get());
	}

	public static bitset z8_fromArray(RCollection<integer> bits) {
		bitset result = new bitset();
		result.operatorAssign(bits);
		return result;
	}
}
