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
		public CLASS() {
			this(null);
		}

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

	// Records insertion direction
	public static final integer Vertical = new integer(0);
	public static final integer Horizontal = new integer(1);

	public OBJECT.CLASS<? extends OBJECT> source = new OBJECT.CLASS<OBJECT>(null);
	public string address = new string();
	public integer direction = Vertical;
	public RCollection<Range.CLASS<Range>> ranges = new RCollection<Range.CLASS<Range>>();

	public org.zenframework.z8.server.reports.poi.Range asPoiRange(OBJECT context) {
		return new org.zenframework.z8.server.reports.poi.Range().setContext(context).setSource(source.get()).setAddress(address.get())
				.setDirection(direction.getInt()).setRanges(asPoiRanges(ranges, context));
	}

	public static List<org.zenframework.z8.server.reports.poi.Range> asPoiRanges(RCollection<Range.CLASS<Range>> ranges, OBJECT context) {
		List<org.zenframework.z8.server.reports.poi.Range> result = new ArrayList<org.zenframework.z8.server.reports.poi.Range>(ranges.size());
		for (Range.CLASS<Range> range : ranges)
			result.add(range.get().asPoiRange(context));
		return result;
	}

	@SuppressWarnings("unchecked")
	public Range.CLASS<Range> z8_addRange(Range.CLASS<Range> range) {
		ranges.add(range);
		return (Range.CLASS<Range>) this.getCLASS();
	}

	public Range.CLASS<Range> z8_addRange(OBJECT.CLASS<? extends OBJECT> source, string address) {
		return z8_addRange(z8_create(source, address));
	}

	public Range.CLASS<Range> z8_addRange(OBJECT.CLASS<? extends OBJECT> source, string address, integer direction) {
		return z8_addRange(z8_create(source, address, direction));
	}

	public static Range.CLASS<Range> z8_create(OBJECT.CLASS<? extends OBJECT> source, string address) {
		return z8_create(source, address, Vertical);
	}

	public static Range.CLASS<Range> z8_create(OBJECT.CLASS<? extends OBJECT> source, string address, integer direction) {
		return z8_create(source, address, direction, null);
	}

	public static Range.CLASS<Range> z8_create(OBJECT.CLASS<? extends OBJECT> source, string address, integer direction, RCollection<Range.CLASS<Range>> ranges) {
		Range.CLASS<Range> range = new Range.CLASS<Range>(null);
		range.get().source = source;
		range.get().address = address;
		range.get().direction = direction;

		if (ranges != null)
			range.get().ranges = ranges;

		return range;
	}

}
