package org.zenframework.z8.server.base.form.report;

import java.util.ArrayList;
import java.util.List;

import org.zenframework.z8.server.runtime.IObject;
import org.zenframework.z8.server.runtime.OBJECT;
import org.zenframework.z8.server.runtime.RCollection;
import org.zenframework.z8.server.types.integer;
import org.zenframework.z8.server.types.string;

public class Range extends OBJECT {
	static public class CLASS<T extends Range> extends OBJECT.CLASS<T> {
		public CLASS(IObject container) {
			super(container);
			setJavaClass(Range.class);
		}

		@Override
		public Object newObject(IObject container) {
			return new Range(container);
		}
	}

	public Range(IObject container) {
		super(container);
	}

	// Attributes
	public static final String Sheet = "sheet";
	public static final String Address = "address";
	public static final String Boundaries = "boundaries";
	public static final String Axis = "axis";
	public static final String Totals = "totals";
	public static final String Subtotal = "subtotal";
	public static final String SubtotalsBy = "subtotalsBy";

	// Records insertion axis
	public static final integer Vertical = new integer(0);
	public static final integer Horizontal = new integer(1);

	public OBJECT.CLASS<? extends OBJECT> source = new OBJECT.CLASS<OBJECT>(this);
	public RCollection<Range.CLASS<Range>> ranges = new RCollection<Range.CLASS<Range>>();
	public RCollection<string> merges = new RCollection<string>();

	public String getSubtotalsBy() {
		String subtotalsb = getAttribute(SubtotalsBy);
		return subtotalsb;
	}

	public int getSheet() {
		String sheet = getAttribute(Sheet);
		return sheet != null ? integer.parse(sheet).getInt() : 0;
	}

	public String getAddress() {
		return getAttribute(Address);
	}

	public String getBoundaries() {
		return getAttribute(Boundaries);
	}

	public int getAxis() {
		String attribute = getAttribute(Axis);
		return attribute != null ? Integer.parseInt(attribute) : Vertical.getInt();
	}

	public boolean isTotals() {
		return hasAttribute(Totals);
	}

	public String getSubtotalBlock() {
		return getAttribute(Subtotal);
	}

	public org.zenframework.z8.server.reports.poi.Range asPoiRange() {
		org.zenframework.z8.server.reports.poi.Range range = new org.zenframework.z8.server.reports.poi.Range()
				.setName(index()).setSource(source.get()).setBlock(getAddress()).setBoundaries(getBoundaries())
				.setAxis(getAxis()).setAggregation(isTotals()).setSubtotalsBy(getSubtotalsBy())
				.setSubtotalBlock(getSubtotalBlock()).setMergesAddress(string.unwrap(merges));

		for (Range.CLASS<Range> subrange : ranges)
			range.addRange(subrange.get().asPoiRange());

		return range;
	}
}
