package org.zenframework.z8.server.reports;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.eclipse.birt.report.engine.api.EngineException;
import org.eclipse.birt.report.engine.api.IExcelRenderOption;
import org.eclipse.birt.report.engine.api.IRenderOption;
import org.eclipse.birt.report.engine.api.IReportRunnable;
import org.eclipse.birt.report.engine.api.IRunAndRenderTask;
import org.eclipse.birt.report.engine.api.RenderOption;
import org.eclipse.birt.report.model.api.CellHandle;
import org.eclipse.birt.report.model.api.DataItemHandle;
import org.eclipse.birt.report.model.api.DesignElementHandle;
import org.eclipse.birt.report.model.api.DesignFileException;
import org.eclipse.birt.report.model.api.ElementFactory;
import org.eclipse.birt.report.model.api.GridHandle;
import org.eclipse.birt.report.model.api.LabelHandle;
import org.eclipse.birt.report.model.api.MasterPageHandle;
import org.eclipse.birt.report.model.api.OdaDataSetHandle;
import org.eclipse.birt.report.model.api.OdaDataSourceHandle;
import org.eclipse.birt.report.model.api.PropertyHandle;
import org.eclipse.birt.report.model.api.ReportDesignHandle;
import org.eclipse.birt.report.model.api.ReportElementHandle;
import org.eclipse.birt.report.model.api.RowHandle;
import org.eclipse.birt.report.model.api.SessionHandle;
import org.eclipse.birt.report.model.api.SlotHandle;
import org.eclipse.birt.report.model.api.StructureFactory;
import org.eclipse.birt.report.model.api.StyleHandle;
import org.eclipse.birt.report.model.api.TableGroupHandle;
import org.eclipse.birt.report.model.api.TableHandle;
import org.eclipse.birt.report.model.api.activity.SemanticException;
import org.eclipse.birt.report.model.api.command.StyleException;
import org.eclipse.birt.report.model.api.elements.DesignChoiceConstants;
import org.eclipse.birt.report.model.api.elements.structures.ComputedColumn;
import org.eclipse.birt.report.model.api.elements.structures.OdaResultSetColumn;
import org.eclipse.birt.report.model.api.metadata.DimensionValue;
import org.eclipse.birt.report.model.elements.OdaDataSet;
import org.eclipse.birt.report.model.elements.interfaces.IGroupElementModel;
import org.eclipse.birt.report.model.elements.interfaces.IMasterPageModel;
import org.eclipse.birt.report.model.elements.interfaces.IStyleModel;
import org.eclipse.birt.report.model.elements.interfaces.ITableColumnModel;
import org.zenframework.z8.server.base.query.Query;
import org.zenframework.z8.server.base.query.QueryUtils;
import org.zenframework.z8.server.base.table.value.Field;
import org.zenframework.z8.server.config.ServerConfig;
import org.zenframework.z8.server.db.Connection;
import org.zenframework.z8.server.db.ConnectionManager;
import org.zenframework.z8.server.json.Json;
import org.zenframework.z8.server.json.JsonWriter;
import org.zenframework.z8.server.json.parser.JsonObject;
import org.zenframework.z8.server.request.actions.ActionConfig;
import org.zenframework.z8.server.request.actions.ReadAction;
import org.zenframework.z8.server.resources.Resources;
import org.zenframework.z8.server.types.date;
import org.zenframework.z8.server.types.file;
import org.zenframework.z8.server.types.guid;
import org.zenframework.z8.server.types.string;
import org.zenframework.z8.server.utils.IOUtils;

public class BirtReport {
	public static final String DataSourceExtensionId = "org.zenframework.z8.oda.driver";
	public static final String DataSetExtensionId = "org.zenframework.z8.oda.driver.DataSet";

	public static final String TableStyle = "ReportBody";

	public static final String HeaderCellStyle = "HeaderCell";
	public static final String DetailCellStyle = "DetailCell";

	public static final String GroupHeaderCellStyle = "GroupHeaderCell";
	public static final String GroupFooterCellStyle = "GroupFooterCell";
	public static final String GroupDetailCellStyle = "DetailCell";
	public static final String GrandTotalCellStyle = "GrandTotalsCell";

	public static final String DateFormatStyle = "DateFormat";
	public static final String DateTimeFormatStyle = "DateTimeFormat";

	private ReportDesignHandle reportDesignHandle;
	private ElementFactory elementFactory;

	private Column rootColumn;
	private List<Column> columns;
	private List<Column> indentationColumns;
	private Collection<Field> fields;
	private Collection<Field> groups;

	private ReportOptions options;

	private boolean initialized = false;

	public BirtReport() {
		this(new ReportOptions());
	}

	public BirtReport(ReportOptions options) {
		this.options = options;
	}

	private String format() {
		return options.format;
	}

	private boolean dropGroupDetail() {
		return options.dropGroupDetail;
	}

	private boolean indentGroups() {
		return options.indentGroupsBy > 0;
	}

	private File reportDesignFile() {
		return options.getReportDesign();
	}

	@SuppressWarnings({ "unchecked" })
	private boolean checkNewName(SlotHandle slotHandle, String name) {
		Iterator<DesignElementHandle> iterator = slotHandle.iterator();

		while(iterator.hasNext()) {
			DesignElementHandle element = iterator.next();

			if(name.equals(element.getName()))
				return false;
		}

		return true;
	}

	private String getNewName(SlotHandle slotHandle, String pattern) {
		String newName = pattern;

		int index = 1;

		while(!checkNewName(slotHandle, pattern))
			newName = pattern + ' ' + index;

		return newName;
	}

	private void createComputedColumns(TableHandle table, OdaResultSetColumn[] resultSetColumns) {
		try {
			PropertyHandle columnBindings = table.getColumnBindings();
			for(OdaResultSetColumn column : resultSetColumns)
				columnBindings.addItem(createComputedColumn(column));
		} catch(SemanticException e) {
			throw new RuntimeException(e);
		}
	}

	private ComputedColumn createComputedColumn(OdaResultSetColumn resultSetColumn) {
		ComputedColumn column = StructureFactory.createComputedColumn();

		String name = resultSetColumn.getColumnName();

		column.setName(name);
		column.setDisplayName(resultSetColumn.getNativeName());
		column.setDataType(resultSetColumn.getDataType());
		column.setExpression("dataSetRow[\"" + name + "\"]");

		return column;
	}

	private String getOdaDataType(Column column) {
		return getOdaDataType(column.getField());
	}

	private String getOdaDataType(Field value) {
		switch(value.type()) {
		case Guid:
		case Boolean:
			return DesignChoiceConstants.COLUMN_DATA_TYPE_STRING;
		// return DesignChoiceConstants.COLUMN_DATA_TYPE_BOOLEAN;
		case Integer:
			return DesignChoiceConstants.COLUMN_DATA_TYPE_INTEGER;
		case String:
			return DesignChoiceConstants.COLUMN_DATA_TYPE_STRING;
		case Date:
			return DesignChoiceConstants.COLUMN_DATA_TYPE_DATE;
		case Datetime:
			return DesignChoiceConstants.COLUMN_DATA_TYPE_DATETIME;
		case Decimal:
			return DesignChoiceConstants.COLUMN_DATA_TYPE_DECIMAL;
		default:
			return DesignChoiceConstants.COLUMN_DATA_TYPE_STRING;
		}
	}

	private OdaResultSetColumn[] createResultSetColumns(OdaDataSetHandle dataSetHandle) {
		OdaDataSet dataSet = (OdaDataSet)dataSetHandle.getElement();

		Collection<Field> groups = getGroups();

		List<OdaResultSetColumn> result = new ArrayList<OdaResultSetColumn>();
		Set<String> names = new HashSet<String>();

		int position = 1;

		for(Column column : getColumns()) {
			if(!column.isIndentation()) {
				String name = column.getName();

				if(!names.contains(name)) {
					String displayName = column.getDisplayName();
					String type = getOdaDataType(column);
					result.add(createResultSetColumn(dataSet, name, displayName, type, position));
					position++;
				}

				names.add(name);
			}
		}

		for(Field field : groups) {
			String name = field.id();

			if(!names.contains(name)) {
				String displayName = field.displayName();
				String type = getOdaDataType(field);
				result.add(createResultSetColumn(dataSet, name, displayName, type, position));
				position++;
			}
		}

		dataSet.setProperty("resultSet", result);

		return result.toArray(new OdaResultSetColumn[0]);
	}

	private OdaResultSetColumn createResultSetColumn(OdaDataSet dataSet, String name, String displayName, String type, int position) {
		OdaResultSetColumn column = StructureFactory.createOdaResultSetColumn();
		column.setColumnName(name);
		column.setNativeName(displayName);
		column.setDataType(type);
		column.setPosition(position);
		return column;
	}

	private boolean createReportHeader(TableHandle table) {
		Column rootColumn = getRootColumn();

		int headerDepth = rootColumn.getDepth();

		for(int depth = 1; depth < headerDepth; depth++) {
			Column[] layer = rootColumn.getLayer(depth).toArray(new Column[0]);

			try {
				RowHandle headerRow = elementFactory.newTableRow();

				int indent = getIndentationColumns().size();

				if(depth == 1 && indent != 0)
					headerRow.getCells().add(createIndentCell(indent, headerDepth - 1, HeaderCellStyle, -1));

				for(Column column : layer) {
					if(column.getHeight() == depth && !column.isIndentation()) {
						CellHandle cell = createCell(column.getColspan(), 1, HeaderCellStyle, -1);
						cell.getContent().add(createLabel(column.getDisplayName()));

						if(!column.hasSubcolumns()) {
							cell.setRowSpan(headerDepth - depth);
							setCellFormat(cell, column);
						}

						headerRow.getCells().add(cell);
					}
				}

				table.getHeader().add(headerRow);
			} catch(SemanticException e) {
				throw new RuntimeException(e);
			}
		}

		return true;
	}

	private void createReportDetail(TableHandle table) {
		try {
			RowHandle detailRow = elementFactory.newTableRow();

			for(Column column : getColumns()) {
				CellHandle cell = null;

				if(!column.isIndentation()) {
					cell = createCell(1, 1, DetailCellStyle, -1);
					setCellFormat(cell, column);

					DataItemHandle data = elementFactory.newDataItem(null);
					data.setResultSetColumn(column.getName());

					cell.getContent().add(data);
				} else
					cell = createIndentCell(1, 1, DetailCellStyle, -1);

				detailRow.getCells().add(cell);
			}

			table.getDetail().add(detailRow);
		} catch(SemanticException e) {
			throw new RuntimeException(e);
		}
	}

	private DesignElementHandle getElement(ReportDesignHandle reportDesignHandle, String name) {
		return reportDesignHandle.getModuleHandle().findElement(name);
	}

	private void setAutoText(ReportDesignHandle reportDesignHandle, String name) throws SemanticException {
		boolean drop = true; /* !options.headers.containsKey(name); */

		DesignElementHandle text = getElement(reportDesignHandle, name);

		if(text != null && drop && isAutoText(name))
			text.drop();
	}

	private boolean isAutoText(String name) {
		return name.equals(Reports.PageNumber) || name.equals(Reports.PageTimestamp);
	}

	private void setReportCaption(ReportDesignHandle reportDesignHandle) throws SemanticException {
		setAutoText(reportDesignHandle, Reports.PageNumber);
		setAutoText(reportDesignHandle, Reports.PageTimestamp);

		DesignElementHandle element = getElement(reportDesignHandle, Reports.FirstPageHeader);

		if(element == null)
			return;

		String header = options.header();

		try {
			element.setProperty("content", header);
		} catch(SemanticException e) {
			try {
				element.setProperty("text", header);
			} catch(SemanticException e1) {
			}
		}
	}

	private Collection<Column> scaleColumns() {
		Collection<Column> columns = getColumns();

		if(Reports.Excel.equals(format()))
			return columns;

		float pageWidth = options.pagesWide * (options.pageWidth() - options.horizontalMargins());
		float scaleFactor = pageWidth / getTotalWidth();

		float totalWidth = 0;
		float fixedWidth = 0;

		for(Column column : columns) {
			float width = Math.max(column.getWidth() * scaleFactor, column.getMinWidth());
			column.setWidth(width);
			totalWidth += width;
			fixedWidth += column.getWidth() == column.getMinWidth() ? width : 0;
		}

		if(totalWidth <= pageWidth)
			return columns;

		scaleFactor = (pageWidth - fixedWidth) / (totalWidth - fixedWidth);

		totalWidth = 0;
		for(Column column : columns) {
			float width = column.getWidth();
			if(width != column.getMinWidth()) {
				width = Math.max(width * scaleFactor, column.getMinWidth());
				column.setWidth(width);
			}
			totalWidth += width;
		}

		if(totalWidth <= pageWidth)
			return columns;

		scaleFactor = pageWidth / totalWidth;

		for(Column column : columns)
			column.setWidth(column.getWidth() * scaleFactor);

		return columns;
	}

	private TableHandle createTable(ReportDesignHandle reportDesignHandle, String dataSetName) {
		try {
			Collection<Column> columns = scaleColumns();

			TableHandle table = elementFactory.newTableItem(null, columns.size(), 0, 0, 0);
			table.setProperty(TableHandle.DATA_SET_PROP, dataSetName);
			setStyle(table, TableStyle);

			int index = 0;

			for(Column column : columns) {
				float width = column.getWidth();
				DesignElementHandle tableColumn = table.getColumns().get(index++);
				tableColumn.getElement().setProperty(ITableColumnModel.WIDTH_PROP, new DimensionValue(width, DesignChoiceConstants.UNITS_PT));
			}

			setReportCaption(reportDesignHandle);

			DesignElementHandle reportBody = getElement(reportDesignHandle, Reports.ReportBody);

			if(reportBody instanceof GridHandle) {
				GridHandle gridHandle = (GridHandle)reportBody;
				gridHandle.getCell(0, 0).getContent().add(table);
			} else
				reportDesignHandle.getBody().add(table);

			table.setPageBreakInterval(0);

			return table;
		} catch(SemanticException e) {
			throw new RuntimeException(e);
		}
	}

	private String getAggregation(Column column) {
		switch(column.getAggregation()) {
		case Sum:
			return DesignChoiceConstants.AGGREGATION_FUNCTION_SUM;
		case Average:
			return DesignChoiceConstants.AGGREGATION_FUNCTION_AVERAGE;
		case Count:
			return DesignChoiceConstants.AGGREGATION_FUNCTION_COUNT;
		case Max:
		case Min:
		default:
			return null;
		}
	}

	private void setStyle(ReportElementHandle element, String style, int styleIndex) throws SemanticException {
		if(styleIndex < 0 || !setStyle(element, style + (styleIndex + 1)))
			setStyle(element, style);
	}

	private boolean setStyle(ReportElementHandle element, String styleName) {
		try {
			StyleHandle style = element.getPrivateStyle();
			style.setStyleName(styleName);

			try {
				float fontSize = UnitsConverter.convertToPoints(style.getFontSize());
				style.setProperty(IStyleModel.FONT_SIZE_PROP, "" + Math.max(fontSize, Reports.MinimalFontSize) + "pt");
			} catch(SemanticException e) {
			}

			if(options.useBlackWhiteColors) {
				try {
					style.setProperty(IStyleModel.COLOR_PROP, "black");
					style.setProperty(IStyleModel.BACKGROUND_COLOR_PROP, "white");
					style.setProperty(IStyleModel.BORDER_LEFT_COLOR_PROP, "black");
					style.setProperty(IStyleModel.BORDER_RIGHT_COLOR_PROP, "black");
					style.setProperty(IStyleModel.BORDER_TOP_COLOR_PROP, "black");
					style.setProperty(IStyleModel.BORDER_BOTTOM_COLOR_PROP, "black");
				} catch(SemanticException e) {
				}
			}

			return true;

		} catch(StyleException e) {
			return false;
		}
	}

	private LabelHandle createLabel(String text) throws SemanticException {
		LabelHandle label = elementFactory.newLabel(null);
		label.setText(text);
		return label;
	}

	private CellHandle createCell(int columnSpan, int rowSpan, String styleName, int styleIndex) throws SemanticException {
		CellHandle cell = elementFactory.newCell();
		cell.setColumnSpan(columnSpan);
		cell.setRowSpan(rowSpan);
		setStyle(cell, styleName, styleIndex);
		return cell;
	}

	private CellHandle createCell(int columnSpan, int rowSpan, String styleName, int styleIndex, String text) throws SemanticException {
		CellHandle cell = createCell(columnSpan, rowSpan, styleName, styleIndex);
		cell.getContent().add(createLabel(text));
		return cell;
	}

	private CellHandle createIndentCell(int columnSpan, int rowSpan, String styleName, int styleIndex, String text) throws SemanticException {
		CellHandle cell = createCell(columnSpan, rowSpan, styleName, styleIndex);
		cell.getContent().add(createLabel(text));
		return cell;
	}

	@SuppressWarnings("unchecked")
	private StyleHandle getStyle(String name) {
		List<StyleHandle> styles = reportDesignHandle.getAllStyles();

		for(StyleHandle style : styles) {
			if(style.getName().equalsIgnoreCase(name))
				return style;
		}

		return null;
	}

	private CellHandle createIndentCell(int columnSpan, int rowSpan, String styleName, int styleIndex) throws SemanticException {
		return createIndentCell(columnSpan, rowSpan, styleName, styleIndex, "");
	}

	private String getColumnFormat(Column column) {
		Field field = column.getField();
		string format = field.format;

		if(format == null || format.isEmpty())
			return null;

		switch(field.type()) {
		case Date:
		case Datetime:
			return format.get().replace("d", "dd").replace("m", "MM").replace("F", "MMM").replace("Y", "yyyy").replace("H", "HH").replace("i", "mm").replace("s", "ss").replace("S", "SSS");
		case Integer:
			return format.get().replace("0", "#") + "0";
		case Decimal:
			return format.get().replace("0", "#");
		default:
			return format.get();
		}
	}

	private void setCellFormat(CellHandle cell, Column column) throws SemanticException {
		cell.setProperty(DesignChoiceConstants.CHOICE_TEXT_ALIGN, column.getHorizontalAlignment());

		String type = getOdaDataType(column);

		if(type.equals(DesignChoiceConstants.COLUMN_DATA_TYPE_DATE)) {
			StyleHandle styleHandle = cell.getPrivateStyle();
			StyleHandle dateFormatStyle = getStyle(DateFormatStyle);

			String format = getColumnFormat(column);

			if(styleHandle != null) {
				if(format != null)
					styleHandle.setDateFormat(format);
				else if(dateFormatStyle != null)
					styleHandle.setDateFormat(dateFormatStyle.getDateTimeFormat());
			}
		} else if(type.equals(DesignChoiceConstants.COLUMN_DATA_TYPE_DATETIME)) {
			StyleHandle styleHandle = cell.getPrivateStyle();
			StyleHandle dateFormatStyle = getStyle(DateTimeFormatStyle);

			if(styleHandle != null) {
				String format = getColumnFormat(column);
				if(format != null)
					styleHandle.setDateTimeFormat(format);
				else if(dateFormatStyle != null)
					styleHandle.setDateTimeFormat(dateFormatStyle.getDateTimeFormat());
			}
		} else if(type.equals(DesignChoiceConstants.COLUMN_DATA_TYPE_DECIMAL) || type.equals(DesignChoiceConstants.COLUMN_DATA_TYPE_FLOAT)) {
			StyleHandle styleHandle = cell.getPrivateStyle();
			if(styleHandle != null) {
				String format = getColumnFormat(column);
				styleHandle.setNumberFormatCategory("Fixed");
				styleHandle.setNumberFormat(format != null ? format : "#,##0.00");
			}
		} else if(type.equals(DesignChoiceConstants.COLUMN_DATA_TYPE_INTEGER)) {
			StyleHandle styleHandle = cell.getPrivateStyle();
			if(styleHandle != null) {
				String format = getColumnFormat(column);
				styleHandle.setNumberFormatCategory("Fixed");
				styleHandle.setNumberFormat(format != null ? format : "#,##0");
			}
		}
	}

	private void createGroup(TableHandle table, Field groupField, int depth) {
		if(groupField == null && !hasAggregation())
			return;

		int indentationColumnsCount = getIndentationColumns().size();
		int currentIndent = Math.min(indentationColumnsCount, Math.max(depth, 0));
		int colCount_beforeAggregation = 0;

		List<Column> columns = getColumns();

		for(Column column : columns) {
			if(column.hasAggregation())
				break;
			colCount_beforeAggregation++;
		}

		try {
			String expression = "";
			String groupName = "";

			if(groupField != null)
				expression += "row[" + '"' + groupField.id() + '"' + "]";
			else
				expression = "1";

			groupName = "__group__" + (depth + 1);

			TableGroupHandle group = elementFactory.newTableGroup();

			group.setKeyExpr(expression);
			group.setName(groupName);

			if(groupField != null) {
				RowHandle groupHeader = elementFactory.newTableRow();

				for(int index = 0; index < currentIndent; index++) {
					CellHandle cell = createIndentCell(1, 1, GroupHeaderCellStyle, -1);
					groupHeader.getCells().add(cell);
				}

				CellHandle cell = createCell(columns.size() - currentIndent, 1, GroupHeaderCellStyle, depth);
				groupHeader.getCells().add(cell);

				ComputedColumn computedColumn = StructureFactory.createComputedColumn();
				computedColumn.setName(groupName + "__header__");
				computedColumn.setExpression(expression);
				computedColumn.setDataType(DesignChoiceConstants.COLUMN_DATA_TYPE_STRING);
				table.getColumnBindings().addItem(computedColumn);

				DataItemHandle dataHandle = elementFactory.newDataItem(null);
				dataHandle.setResultSetColumn(computedColumn.getName());
				cell.getContent().add(dataHandle);

				group.getHeader().add(groupHeader);

				if(dropGroupDetail() && depth == getGroups().size() - 1)
					group.setProperty(IGroupElementModel.HIDE_DETAIL_PROP, true);
			}

			if(hasAggregation()) {
				String style = (depth == -1 ? GrandTotalCellStyle : GroupFooterCellStyle);

				RowHandle groupFooter = elementFactory.newTableRow();

				for(int index = 0; index < currentIndent; index++) {
					CellHandle cell = createIndentCell(1, 1, style, -1);
					groupFooter.getCells().add(cell);
				}

				if(indentationColumnsCount > 0) {
					String text = "";
					CellHandle cell = null;

					int colspan_text_groupTotal = colCount_beforeAggregation - currentIndent;
					if(depth != -1) {
						text = options.markTotals ? Resources.get(Reports.GroupTotalText) : " ";
						cell = createCell(colspan_text_groupTotal, 1, GroupFooterCellStyle, depth);

						ComputedColumn computedColumn = StructureFactory.createComputedColumn();
						computedColumn.setName(groupName + "__total__");

						computedColumn.setExpression("\"" + text + "\"+" + expression);
						computedColumn.setDataType(DesignChoiceConstants.COLUMN_DATA_TYPE_STRING);
						table.getColumnBindings().addItem(computedColumn);

						DataItemHandle dataHandle = elementFactory.newDataItem(null);
						dataHandle.setResultSetColumn(computedColumn.getName());
						cell.getContent().add(dataHandle);
					} else {
						text = options.markGrandTotals ? Resources.get(Reports.GroupGrandTotalText) : " ";
						cell = createCell(colspan_text_groupTotal, 1, GrandTotalCellStyle, depth, text);
					}

					cell.setProperty(DesignChoiceConstants.CHOICE_TEXT_ALIGN, DesignChoiceConstants.TEXT_ALIGN_LEFT);
					groupFooter.getCells().add(cell);
				} else {
					if(colCount_beforeAggregation > 0) {
						CellHandle cell = createIndentCell(colCount_beforeAggregation, 1, GroupDetailCellStyle, -1);
						groupFooter.getCells().add(cell);
					}
				}

				for(int index = colCount_beforeAggregation; index < columns.size(); index++) {
					Column column = columns.get(index);

					if(!column.isIndentation()) {
						CellHandle cell = createCell(1, 1, GroupDetailCellStyle, depth);
						setCellFormat(cell, column);

						String aggregation = getAggregation(column);

						if(aggregation != null) {
							String name = column.getName();
							ComputedColumn computedColumn = StructureFactory.createComputedColumn();
							computedColumn.setName(groupName + "__footer__column__" + index);
							computedColumn.setAggregateFunction(aggregation);
							computedColumn.setAggregateOn(groupName);
							computedColumn.setDataType(getOdaDataType(column.getField()));
							computedColumn.setExpression("dataSetRow[\"" + name + "\"]");

							table.getColumnBindings().addItem(computedColumn);

							DataItemHandle dataHandle = elementFactory.newDataItem(null);
							dataHandle.setResultSetColumn(computedColumn.getName());
							cell.getContent().add(dataHandle);
						}
						cell.setProperty(DesignChoiceConstants.CHOICE_FONT_WEIGHT, DesignChoiceConstants.FONT_WEIGHT_BOLD);
						groupFooter.getCells().add(cell);
					}
				}
				group.getFooter().add(groupFooter);
			}
			table.getGroups().add(group);
		} catch(SemanticException e) {
			throw new RuntimeException(e);
		}
	}

	private void createGroups(TableHandle table) {
		if(hasAggregation())
			createGroup(table, null, -1);

		int index = 0;
		for(Field field : getGroups()) {
			createGroup(table, field, index);
			index++;
		}
	}

	private Column getRootColumn() {
		return rootColumn;
	}

	private List<Column> getColumns() {
		if(columns == null)
			columns = rootColumn.getColumns();
		return columns;
	}

	private Collection<Column> getIndentationColumns() {
		return indentationColumns;
	}

	private Collection<Field> getGroups() {
		return groups;
	}

	private Collection<Field> getFields() {
		if(fields != null)
			return fields;

		fields = new ArrayList<Field>();

		for(Column column : getColumns())
			fields.add(column.getField());

		return fields;
	}

	private float getTotalWidth() {
		return getRootColumn().getTotalWidth();
	}

	private boolean hasAggregation() {
		for(Column column : getColumns()) {
			if(column.hasAggregation())
				return true;
		}
		return false;
	}

	private boolean isSplitNeeded() {
		return options.splitContent && format().equalsIgnoreCase(Reports.Pdf);
	}

	private ReportDesignHandle createReportDesign(SessionHandle session) {
		try {
			reportDesignHandle = session.openDesign(reportDesignFile().getAbsolutePath());
		} catch(DesignFileException e) {
			reportDesignHandle = session.createDesign();
		}

		return reportDesignHandle;
	}

	private void initialize(Column rootColumn, Collection<Field> groups) {
		if(initialized)
			return;

		initialized = true;

		if(rootColumn.getName() == null && rootColumn.getSubcolumns().size() == 1) {
			rootColumn = rootColumn.getSubcolumns().get(0);
			rootColumn.setParent(null);
		}

		for(Column column : rootColumn.getSubcolumns()) {
			if(column.isIndentation())
				rootColumn.removeColumn(column);
		}

		indentationColumns = new ArrayList<Column>();

		if(indentGroups()) {
			for(int i = groups.size() - 1; i >= 0; i--) {
				Column column = Column.createIndentationColumn(options.indentGroupsBy);
				rootColumn.addColumn(column, 0);
				indentationColumns.add(i, column);
			}
		}

		this.groups = groups;
		this.rootColumn = rootColumn;
	}

	private void createMasterPage(ReportDesignHandle reportDesignHandle) {
		try {
			MasterPageHandle masterPage = (MasterPageHandle)reportDesignHandle.getMasterPages().get(0);

			if(masterPage == null) {
				masterPage = elementFactory.newSimpleMasterPage("Master Page");
				reportDesignHandle.getMasterPages().add(masterPage);
			}

			float pageWidth = (options.pagesWide != 0 && format().equalsIgnoreCase(Reports.Pdf)) ? 
					options.pagesWide * options.pageWidth() :
					(getTotalWidth() + options.horizontalMargins());

			masterPage.setProperty(IMasterPageModel.TYPE_PROP, DesignChoiceConstants.PAGE_SIZE_CUSTOM);

			masterPage.setProperty(IMasterPageModel.WIDTH_PROP, "" + pageWidth + "pt");
			masterPage.setProperty(IMasterPageModel.HEIGHT_PROP, "" + options.pageHeight() + "pt");

			masterPage.setProperty(IMasterPageModel.LEFT_MARGIN_PROP, "" + options.leftMargin() + "pt");
			masterPage.setProperty(IMasterPageModel.RIGHT_MARGIN_PROP, "" + options.rightMargin() + "pt");
			masterPage.setProperty(IMasterPageModel.TOP_MARGIN_PROP, "" + options.topMargin() + "pt");
			masterPage.setProperty(IMasterPageModel.BOTTOM_MARGIN_PROP, "" + options.bottomMargin() + "pt");
		} catch(Exception e) {
			throw new RuntimeException(e);
		}
	}

	private OdaDataSourceHandle createDataSource(ReportDesignHandle reportDesignHandle) {
		try {
			String dataSourceName = getNewName(reportDesignHandle.getDataSources(), "Z8 Data Source");
			OdaDataSourceHandle dataSourceHandle = elementFactory.newOdaDataSource(dataSourceName, DataSourceExtensionId);
			reportDesignHandle.getDataSources().add(dataSourceHandle);
			return dataSourceHandle;
		} catch(Exception e) {
			throw new RuntimeException(e);
		}
	}

	private OdaDataSetHandle createDataSet(ReportDesignHandle reportDesignHandle, OdaDataSourceHandle dataSourceHandle) {
		try {
			String dataSetName = getNewName(reportDesignHandle.getDataSources(), "Z8 Data Set");
			OdaDataSetHandle dataSetHandle = elementFactory.newOdaDataSet(dataSetName, DataSetExtensionId);
			reportDesignHandle.getDataSets().add(dataSetHandle);
			dataSetHandle.setDataSource(dataSourceHandle.getName());

			ReadAction action = options.action;

			JsonWriter writer = new JsonWriter();
			action.getQuery().writeReportMeta(writer, "", getFields());
			dataSetHandle.setProperty("queryText", writer.toString());
			return dataSetHandle;
		} catch(Exception e) {
			throw new RuntimeException(e);
		}
	}

	public File execute(Collection<Column> columns, Collection<Field> groupFields) {
		Column column = new Column();

		for(Column c : columns)
			column.addColumn(c);

		return execute(column, groupFields);
	}

	public File execute() {
		try {
			IReportRunnable runnable = options.reportEngine().openReportDesign(reportDesignFile().getAbsolutePath());
			return generateAndSplit(runnable);
		} catch(EngineException e) {
			throw new RuntimeException(e);
		}
	}

	private File execute(Column rootColumn, Collection<Field> groups) {
		initialize(rootColumn, groups);

		SessionHandle session = options.designEngine().newSessionHandle(null);

		ReportDesignHandle reportDesignHandle = createReportDesign(session);

		elementFactory = reportDesignHandle.getElementFactory();

		createMasterPage(reportDesignHandle);

		OdaDataSourceHandle dataSourceHandle = createDataSource(reportDesignHandle);
		OdaDataSetHandle dataSetHandle = createDataSet(reportDesignHandle, dataSourceHandle);

		OdaResultSetColumn[] resultSetColumns = createResultSetColumns(dataSetHandle);

		TableHandle table = createTable(reportDesignHandle, dataSetHandle.getName());

		createComputedColumns(table, resultSetColumns);

		createReportHeader(table);
		createReportDetail(table);
		createGroups(table);

		try {
			IReportRunnable runnable = options.reportEngine().openReportDesign(reportDesignHandle);
			return generateAndSplit(runnable);
		} catch(EngineException e) {
			throw new RuntimeException(e);
		}
	}

	private File getUniqueFileName(File folder, String name, String extension) {
		name = name.replace('/', '-').replace('\\', '-').replace(':', '-').replace('\n', ' ');

		if(name.endsWith("."))
			name = name.substring(0, name.length() - 1);

		date time = new date();
		File file = FileUtils.getFile(folder, time.format("yyyy.MM.dd"), guid.create().toString(), name + "." + extension);
		file.getParentFile().mkdirs();
		return file;
	}

	private File generateAndSplit(IReportRunnable reportRunnable) {
		File outputFile = null;
		File splittedFile = null;

		File folder = ReportOptions.getReportOutputFolder();

		String documentName = options.name();

		outputFile = getUniqueFileName(folder, documentName, format());

		Connection connection = ConnectionManager.get();
		connection.beginTransaction(); // for large cursors

		try {
			run(reportRunnable, outputFile);
		} finally {
			connection.rollback();
		}

		if(isSplitNeeded()) {
			splittedFile = getUniqueFileName(folder, documentName, format());

			FileOutputStream output = null;
			FileInputStream input = null;

			try {
				output = new FileOutputStream(splittedFile);
				input = new FileInputStream(outputFile);
				PdfSplitter.split(options, input, output);
			} catch(Throwable e) {
				throw new RuntimeException(e);
			} finally {
				IOUtils.closeQuietly(output);
				IOUtils.closeQuietly(input);
			}

			return new File(file.getRelativePath(splittedFile));
		} else
			return new File(file.getRelativePath(outputFile));
	}

	private ReadAction parseReportMeta(String json, Collection<Query> queries) {
		JsonObject meta = new JsonObject(json);

		String name = meta.getString(Json.name);
		String id = meta.has(Json.id) ? meta.getString(Json.id) : meta.getString(Json.request);

		Query query = QueryUtils.findByIndex(queries, name);

		if(query == null)
			query = QueryUtils.findByClassId(queries, id);

		if(query == null)
			throw new RuntimeException("Reports: query '" + name + "' ('" + id + "') is missing");

		Collection<Field> fields = QueryUtils.parseFields(query, meta.getJsonArray(Json.fields), query.id());

		ActionConfig config = new ActionConfig(query, fields);
		config.sortFields = query.sortFields();
		return new ReadAction(config);
	}

	private void run(IReportRunnable runnable, File outputFile) {
		String lang = ServerConfig.language();

		IRunAndRenderTask task = null;
		task = options.reportEngine().createRunAndRenderTask(runnable);
		task.setLocale(new Locale(lang, lang.toUpperCase()));

		HashMap<String, Object> contextMap = new HashMap<String, Object>();

		if(options.action != null)
			contextMap.put(options.action.getQuery().classId(), options.action);

		if(options.queries != null) {
			ReportDesignHandle design = (ReportDesignHandle)runnable.getDesignHandle();
			SlotHandle dataSets = design.getDataSets();

			for(int i = 0; i < dataSets.getCount(); i++) {
				OdaDataSetHandle dataSet = (OdaDataSetHandle)dataSets.get(i);
				ReadAction action = parseReportMeta(dataSet.getQueryText(), options.queries);
				contextMap.put(action.getQuery().classId(), action);
				contextMap.put(action.getQuery().index(), action);
			}
		}

		task.setAppContext(contextMap);

		IRenderOption options = new RenderOption();
		options.setOutputFormat(format()); // pdf, doc, ppt, html, xls_spudsoft
		options.setOutputFileName(outputFile.getAbsolutePath());


		if(format().equalsIgnoreCase(Reports.Excel)) {
			options.setOption(IExcelRenderOption.OFFICE_VERSION, "office2007");
			options.setOption(IRenderOption.EMITTER_ID, "uk.co.spudsoft.birt.emitters.excel.XlsEmitter");
			options.setOutputFormat("xls_spudsoft"); // pdf, doc, ppt, html, xls_spudsoft
		}

		task.setRenderOption(options);

		try {
			task.run();
		} catch(EngineException e) {
			throw new RuntimeException(e);
		} finally {
			task.close();
		}
	}
}
