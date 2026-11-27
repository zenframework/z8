package org.zenframework.z8.server.types;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

import org.zenframework.z8.server.base.json.parser.JsonArray;
import org.zenframework.z8.server.db.DatabaseVendor;
import org.zenframework.z8.server.db.FieldType;
import org.zenframework.z8.server.runtime.META;
import org.zenframework.z8.server.runtime.RCollection;
import org.zenframework.z8.server.types.sql.sql_vector;

public class vector extends primary {
	private static final long serialVersionUID = 5911484277015457142L;

	static private final META Meta = META.meta(vector.class);

	private static final Random RANDOM = new Random();

	public static final vector Zero = new vector(.0);

	private double[] components;

	public vector() {
		this(1);
	}

	public vector(String str) {
		init(str);
	}

	public vector(org.zenframework.z8.server.json.parser.JsonArray array) {
		init(array);
	}

	public vector(int size) {
		this(size, .0);
	}

	public vector(double... components) {
		this.components = new double[components.length];
		System.arraycopy(components, 0, this.components, 0, components.length);
	}

	public vector(List<primary> array) {
		init(array);
	}

	private vector(int size, double value) {
		if (size < 0)
			throw new IllegalArgumentException("Vector negative size");

		components = new double[size];
		Arrays.fill(components, value);
	}

	private void init(String str) {
		init(new org.zenframework.z8.server.json.parser.JsonArray(str.charAt(0) != '[' ? '[' + str.substring(1, str.length() - 1) + ']' : str));
	}

	private void init(org.zenframework.z8.server.json.parser.JsonArray array) {
		this.components = new double[array.size()];
		for (int i = 0; i < components.length; i++)
			this.components[i] = array.getDouble(i);
	}

	private void init(List<primary> array) {
		this.components = new double[array.size()];
		for (int i = 0; i < components.length; i++) {
			primary el = array.get(i);
			if (el instanceof decimal)
				this.components[i] = ((decimal) el).getDouble();
			else if (el instanceof integer)
				this.components[i] = ((integer) el).get();
			else
				throw new RuntimeException("Incorrect vector component type: " + el.getClass().getSimpleName());
		}
	}

	@Override
	public META getMeta() {
		return Meta;
	}

	@Override
	public int compareTo(primary o) {
		if (!(o instanceof vector))
			return -1;

		vector v = (vector) o;
		int size1 = size();
		int size2 = v.size();

		if (size1 == 0 && size2 == 0)
			return 0;
		if (size1 == 0)
			return -1;
		if (size2 == 0)
			return 1;

		return Double.compare(cosine(new vector(size(), 1.0)), v.cosine(new vector(v.size(), 1.0)));
	}

	@Override
	public int hashCode() {
		double sum = .0;
		for (double v : components)
			sum += v;
		return Double.hashCode(sum);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!(obj instanceof vector))
			return false;
		vector x = (vector) obj;
		if (size() != x.size())
			return false;
		for (int i = 0; i < size(); i++)
			if (get(i) != x.get(i))
				return false;
		return true;
	}

	@Override
	public String toString() {
		int n = components.length - 1;
		if (n == -1)
			return "[]";

		StringBuilder b = new StringBuilder();
		b.append('[');
		for (int i = 0; ; i++) {
			b.append(components[i]);
			if (i == n)
				return b.append(']').toString();
			b.append(", ");
		}
	}

	public double[] get() {
		return components;
	}

	public double get(int i) {
		return components[i];
	}

	@Override
	public FieldType type() {
		return FieldType.Vector;
	}

	@Override
	public String toDbConstant(DatabaseVendor dbtype) {
		switch(dbtype) {
		case Postgres:
			return "'" + toString() + "'";
		default:
			throw new UnsupportedOperationException("Unsupported vendor: " + dbtype);
		}
	}

	public int size() {
		return components.length;
	}

	public long hash() {
		long hash = 0L;
		for (int i = 0; i < components.length; i++) {
			if (components[i] > .0)
				hash = hash ^ (1L << (i % 64));
		}
		return hash;
	}

	public bitset bitHash() {
		return bitHash(components.length);
	}

	public bitset bitHash(int size) {
		bitset hash = new bitset(size);
		for (int i = 0; i < components.length; i++) {
			if (components[i] > .0)
				hash.flip(i % size);
		}
		return hash;
	}

	public vector negate() {
		double negative[] = new double[components.length];
		for (int i = 0; i < components.length; i++)
			negative[i] = -components[i];
		return new vector(negative);
	}

	public double scalar() {
		double scalar = .0;
		for (double v : components)
			scalar += v * v;
		return Math.sqrt(scalar);
	}

	public vector normalize() {
		return normalize(1.0);
	}

	public vector normalize(double scalar) {
		return multiply(scalar / scalar());
	}

	public vector add(vector x) {
		int size = checkSize(x);
		double sum[] = new double[size];
		for (int i = 0; i < size; i++)
			sum[i] = get(i) + x.get(i);
		return new vector(sum);
	}

	public vector sub(vector x) {
		int size = checkSize(x);
		double sum[] = new double[size];
		for (int i = 0; i < size; i++)
			sum[i] = get(i) - x.get(i);
		return new vector(sum);
	}

	public vector multiply(double x) {
		double mult[] = new double[components.length];
		for (int i = 0; i < components.length; i++)
			mult[i] = get(i) * x;
		return new vector(mult);
	}

	public double scalarProduct(vector x) {
		int size = checkSize(x);
		double product = .0;
		for (int i = 0; i < size; i++)
			product += get(i) * x.get(i);
		return product;
	}

	public double cosine(vector x) {
		int size = checkSize(x);
		if (size == 0)
			return 1.0;
		double product = .0, scalar1 = .0, scalar2 = .0;
		for (int i = 0; i < size; i++) {
			double a = get(i), b = x.get(i);
			product += a * b;
			scalar1 += a * a;
			scalar2 += b * b;
		}
		return product / (Math.sqrt(scalar1) * Math.sqrt(scalar2));
	}

	public vector projection(List<vector> basis) {
		double[] projection = new double[basis.size()];
		for (int i = 0; i < basis.size(); i++) {
			double cosine = cosine(basis.get(i));
			projection[i] = cosine > .0 ? 1.0 : cosine < .0 ? -1.0 : .0;
		}
		return new vector(projection);
	}

	public sql_vector sql_vector() {
		return new sql_vector(this);
	}

	public void operatorAssign(RCollection<primary> array) {
		init(array);
	}

	public void operatorAssign(string text) {
		init(text.get());
	}

	public void operatorAssign(JsonArray.CLASS<? extends JsonArray> array) {
		init(array.get().get());
	}

	public vector operatorAdd() {
		return this;
	}

	public vector operatorSub() {
		return negate();
	}

	public vector operatorAdd(vector x) {
		return add(x);
	}

	public vector operatorSub(vector x) {
		return sub(x);
	}

	public vector operatorMul(decimal x) {
		return multiply(x.getDouble());
	}

	public vector operatorDiv(decimal x) {
		return multiply(1.0 / x.getDouble());
	}

	public decimal operatorMul(vector x) {
		return new decimal(scalarProduct(x));
	}

	public bool operatorEqu(vector x) {
		return new bool(equals(x));
	}

	public bool operatorNotEqu(vector x) {
		return new bool(!equals(x));
	}

	public bool operatorLess(vector x) {
		return new bool(compareTo(x) < 0);
	}

	public bool operatorMore(vector x) {
		return new bool(compareTo(x) > 0);
	}

	public bool operatorLessEqu(vector x) {
		return new bool(compareTo(x) <= 0);
	}

	public bool operatorMoreEqu(vector x) {
		return new bool(compareTo(x) >= 0);
	}

	public integer z8_size() {
		return new integer(size());
	}

	public integer z8_hash() {
		return new integer(hash());
	}

	public bitset z8_bitHash(integer size) {
		return bitHash(size.getInt());
	}

	public bitset z8_bitHash() {
		return bitHash();
	}

	public decimal z8_scalar() {
		return new decimal(scalar());
	}

	public vector z8_normalize() {
		return normalize();
	}

	public vector z8_normalize(decimal scalar) {
		return normalize(scalar.getDouble());
	}

	public decimal z8_cosine(vector v) {
		return new decimal(cosine(v));
	}

	public vector z8_projection(RCollection<vector> basis) {
		return projection(basis);
	}

	private int checkSize(vector x) {
		int size = size();
		if (size != x.size())
			throw new exception("Illegal vector size " + size + " != " + x.size());
		return size;
	}

	public static vector fill(int size, double value) {
		return new vector(size, value);
	}

	public static vector random(int size) {
		double[] components = new double[size];
		for (int i = 0; i < size; i++)
			components[i] = RANDOM.nextDouble() - .5;
		return new vector(components);
	}

	public static vector parse(String str) {
		return new vector(str);
	}

	public static vector z8_fill(integer size, decimal value) {
		return new vector(size.getInt(), value.getDouble());
	}

	public static vector z8_random(integer size) {
		return random(size.getInt());
	}

	public static vector z8_parse(string string) {
		return parse(string.get());
	}

	public static vector z8_fromArray(RCollection<primary> array) {
		return new vector(array);
	}

	public static vector z8_fromArray(JsonArray.CLASS<? extends JsonArray> array) {
		return new vector(array.get().get());
	}

	public static RCollection<vector> z8_vectorArray(JsonArray.CLASS<? extends JsonArray> json) {
		org.zenframework.z8.server.json.parser.JsonArray arr = json.get().get();
		RCollection<vector> basis = new RCollection<vector>(arr.size(), false);
		for (int i = 0; i < arr.size(); i++)
			basis.add(new vector(arr.getJsonArray(i)));
		return basis;
	}
}
