package org.zenframework.z8.server.base.query;

public enum ViewMode {
	Table(Names.Table), Form(Names.Form), Chart(Names.Chart), TableForm(Names.TableForm), TableChart(Names.TableChart);

	class Names {
		static protected final String Table = "table";
		static protected final String Form = "form";
		static protected final String Chart = "chart";
		static protected final String TableForm = "table/form";
		static protected final String TableChart = "table/chart";
	}

	private String fName = null;

	ViewMode(String name) {
		fName = name;
	}

	@Override
	public String toString() {
		return fName;
	}

	static public ViewMode fromString(String string) {
		if(Names.Table.equals(string)) {
			return ViewMode.Table;
		} else if(Names.Form.equals(string)) {
			return ViewMode.Form;
		} else if(Names.Chart.equals(string)) {
			return ViewMode.Chart;
		} else if(Names.TableForm.equals(string)) {
			return ViewMode.TableForm;
		} else if(Names.TableChart.equals(string)) {
			return ViewMode.TableChart;
		} else {
			throw new RuntimeException("Unknown view mode: '" + string + "'");
		}
	}
}
