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
	public static final String Direction = "direction";

	// Records insertion direction
	public static final integer Vertical = new integer(0);
	public static final integer Horizontal = new integer(1);

	public OBJECT.CLASS<? extends OBJECT> source = new OBJECT.CLASS<OBJECT>(this);
	public RCollection<Range.CLASS<Range>> ranges = new RCollection<Range.CLASS<Range>>();
	public RCollection<string> merges = new RCollection<string>();
	public Condition.CLASS<? extends Condition> on = new Condition.CLASS<Condition>(this);

	public int getSheet() {
		String sheet = getAttribute(Sheet);
		return sheet != null ? Integer.parseInt(sheet) : 0;
	}

	public String getAddress() {
		return getAttribute(Address);
	}

	public String getBoundaries() {
		return getAttribute(Boundaries);
	}

	public int getDirection() {
		String attribute = getAttribute(Direction);
		return attribute != null ? Integer.parseInt(attribute) : Vertical.getInt();
	}

	public org.zenframework.z8.server.reports.poi.Range asPoiRange() {
		org.zenframework.z8.server.reports.poi.Range range = new org.zenframework.z8.server.reports.poi.Range()
				.setSource(source.get()).setSheetIndex(getSheet()).setBlock(getAddress()).setBoundaries(getBoundaries())
				.setDirection(getDirection()).setMergesAddress(string.unwrap(merges));

		for (Range.CLASS<Range> subrange : ranges)
			range.addRange(subrange.get().asPoiRange());

		return range;
	}

	public static List<org.zenframework.z8.server.reports.poi.Range> asPoiRanges(RCollection<Range.CLASS<Range>> ranges) {
		List<org.zenframework.z8.server.reports.poi.Range> result = new ArrayList<org.zenframework.z8.server.reports.poi.Range>(ranges.size());
		for (Range.CLASS<Range> range : ranges)
			result.add(range.get().asPoiRange());
		return result;
	}
}
