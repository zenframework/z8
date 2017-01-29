package org.zenframework.z8.server.reports;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.birt.report.engine.api.EngineException;
import org.eclipse.birt.report.engine.api.HTMLRenderOption;
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
import org.zenframework.z8.server.base.file.Folders;
import org.zenframework.z8.server.base.model.actions.ReadAction;
import org.zenframework.z8.server.base.table.value.Field;
import org.zenframework.z8.server.resources.Resources;
import org.zenframework.z8.server.types.string;
import org.zenframework.z8.server.utils.IOUtils;

public class BirtReportRunner {
	public static final String Url = "home";
	public static final String Password = "password";
	public static final String User = "user";
	public static final String DataSource = "dataSource";

	public static final String DataSourceExtensionId = "org.zenframework.z8.oda.driver";
	public static final String DataSetExtensionId = "org.zenframework.z8.oda.driver.DataSet";

	public static final String DataProperty = "org.zenframework.z8.server.reports.Data";

	public static final String TableStyle = "ReportBody";

	public static final String HeaderCellStyle = "HeaderCell";
	public static final String DetailCellStyle = "DetailCell";

	public static final String GroupHeaderCellStyle = "GroupHeaderCell";
	public static final String GroupFooterCellStyle = "GroupFooterCell";
	public static final String GroupDetailCellStyle = "DetailCell";
	public static final String GranTotalCellStyle = "GrandTotalsCell";

	public static final String DateFormatStyle = "DateFormat";
	public static final String DateTimeFormatStyle = "DateTimeFormat";

	private ReportDesignHandle reportDesignHandle = null;
	private ElementFactory elementFactory = null;

	private Column rootColumn = null;
	private Column[] indentationColumns = null;
	private Field[] groups = null;

	float scaleFactor = 1;

	private ReportOptions options;

	private boolean m_initialized = false;

	public BirtReportRunner() {
		this(new ReportOptions());
	}

	public BirtReportRunner(ReportOptions options) {
		this.options = options;
	}

	private String format() {
		return options.getFormat();
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

		while(!checkNewName(slotHandle, pattern)) {
			newName = pattern + ' ' + index;
		}

		return newName;
	}

	private void createComputedColumns(TableHandle table, OdaResultSetColumn[] resultSetColumns) {
		try {
			PropertyHandle columnBindings = table.getColumnBindings();

			for(OdaResultSetColumn column : resultSetColumns) {
				columnBindings.addItem(createComputedColumn(column));
			}
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

		Column[] columns = getColumns();
		Field[] groups = getGroups();

		List<OdaResultSetColumn> result = new ArrayList<OdaResultSetColumn>();
		Set<String> names = new HashSet<String>();

		int position = 1;

		for(int index = 0; index < columns.length; index++) {
			Column column = columns[index];

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

				int indent = getIndentationColumns().length;

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

	private void createReportDetail(TableHandle table) throws RuntimeException {
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
				} else {
					cell = createIndentCell(1, 1, DetailCellStyle, -1);
				}

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
		boolean drop = !options.headers.containsKey(name);

		DesignElementHandle text = getElement(reportDesignHandle, name);

		if(text != null && drop && isAutoText(name)) {
			text.drop();
		}
	}

	private boolean isAutoText(String name) {
		return name.equals(Reports.EACHPAGE_PAGE_NUMBER) || name.equals(Reports.EACHPAGE_TIMESTAMP);
	}

	private void setReportCaption(ReportDesignHandle reportDesignHandle) throws SemanticException {
		setAutoText(reportDesignHandle, Reports.EACHPAGE_PAGE_NUMBER);
		setAutoText(reportDesignHandle, Reports.EACHPAGE_TIMESTAMP);

		for(Map.Entry<String, String> entry : options.headers.entrySet()) {
			String name = entry.getKey();

			DesignElementHandle element = getElement(reportDesignHandle, name);

			if(element != null && !isAutoText(name)) {
				try {
					element.setProperty("content", entry.getValue());
				} catch(SemanticException e) {
					try {
						element.setProperty("text", entry.getValue());
					} catch(SemanticException e1) {
					}
				}
			}
		}
	}

	private TableHandle createTable(ReportDesignHandle reportDesignHandle, String dataSetName) {
		try {
			Column[] columns = getColumns();

			TableHandle table = elementFactory.newTableItem(null, columns.length, 0, 0, 0);
			table.setProperty(TableHandle.DATA_SET_PROP, dataSetName);
			setStyle(table, TableStyle);

			int index = 0;

			for(Column column : columns) {
				float width = column.getWidth() * scaleFactor;
				DesignElementHandle tableColumn = table.getColumns().get(index++);
				tableColumn.getElement().setProperty(ITableColumnModel.WIDTH_PROP, new DimensionValue(width, DesignChoiceConstants.UNITS_PT));
			}

			setReportCaption(reportDesignHandle);

			DesignElementHandle reportBody = getElement(reportDesignHandle, Reports.REPORT_BODY);

			if(reportBody instanceof GridHandle) {
				GridHandle gridHandle = (GridHandle)reportBody;
				gridHandle.getCell(0, 0).getContent().add(table);
			} else {
				reportDesignHandle.getBody().add(table);
			}
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
		case Max:
			return DesignChoiceConstants.AGGREGATION_FUNCTION_MAX;
		case Min:
			return DesignChoiceConstants.AGGREGATION_FUNCTION_MIN;
		case Average:
			return DesignChoiceConstants.AGGREGATION_FUNCTION_AVERAGE;
		case Count:
			return DesignChoiceConstants.AGGREGATION_FUNCTION_COUNT;
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
				float fontSize = BirtUnitsConverter.convertToPoints(style.getFontSize());
				style.setProperty(IStyleModel.FONT_SIZE_PROP, "" + Math.max(fontSize * Math.min(scaleFactor, 1), Reports.MinimalFontSize) + "pt");
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

	private void modifyStyle(CellHandle cell) throws SemanticException {
		// cell.setProperty(StyleHandle.BORDER_LEFT_WIDTH_PROP, "0pt");
		// cell.setProperty(StyleHandle.BORDER_RIGHT_WIDTH_PROP, "0pt");
		cell.setProperty(StyleHandle.BORDER_TOP_WIDTH_PROP, "0pt");
		cell.setProperty(StyleHandle.BORDER_BOTTOM_WIDTH_PROP, "0pt");
		cell.setProperty(StyleHandle.BACKGROUND_COLOR_PROP, "white");
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
		modifyStyle(cell);
		return cell;
	}

	@SuppressWarnings("unchecked")
	private StyleHandle getStyle(String name) {
		List<StyleHandle> styles = reportDesignHandle.getAllStyles();

		for(StyleHandle style : styles) {
			if(style.getName().equalsIgnoreCase(name)) {
				return style;
			}
		}

		return null;
	}

	private CellHandle createIndentCell(int columnSpan, int rowSpan, String styleName, int styleIndex) throws SemanticException {
		return createIndentCell(columnSpan, rowSpan, styleName, styleIndex, "");
	}

	private String getColumnFormat(Column column) {
		string format = column.getField().format;

		if(format == null || format.isEmpty())
			return null;

		return format.get().replace("d", "dd").replace("m", "MM").replace("Y", "yyyy").replace("H", "hh").replace("i", "mm").replace("s", "ss");
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

			String format = getColumnFormat(column);

			if(styleHandle != null) {
				if(format != null)
					styleHandle.setDateTimeFormat(format);
				else if(dateFormatStyle != null)
					styleHandle.setDateTimeFormat(dateFormatStyle.getDateTimeFormat());
			}
		} else if(type.equals(DesignChoiceConstants.COLUMN_DATA_TYPE_DECIMAL) || type.equals(DesignChoiceConstants.COLUMN_DATA_TYPE_FLOAT)) {
			StyleHandle styleHandle = cell.getPrivateStyle();

			if(styleHandle != null) {
				styleHandle.setNumberFormatCategory("Fixed");
				styleHandle.setNumberFormat("#,##0.00");
			}
		}
	}

	private void createGroup(TableHandle table, Field groupField, int depth) {
		if(groupField == null && !hasAggregation())
			return;

		int indentationColumnsCount = getIndentationColumns().length;
		int currentIndent = Math.min(indentationColumnsCount, Math.max(depth, 0));
		int colCount_beforeAggregation = 0;
		Column[] columns = getColumns();

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

				CellHandle cell = createCell(columns.length - currentIndent, 1, GroupHeaderCellStyle, depth);
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

				if(dropGroupDetail() && depth == getGroups().length - 1) {
					group.setProperty(IGroupElementModel.HIDE_DETAIL_PROP, true);
				}
			}

			if(hasAggregation()) {
				String style = (depth == -1 ? GranTotalCellStyle : GroupFooterCellStyle);

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
						text = options.markTotals ? Resources.get(Reports.GroupTotalText) : "";
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
						text = options.markGrandTotals ? Resources.get(Reports.GroupGrandTotalText) : "";
						cell = createCell(colspan_text_groupTotal, 1, GranTotalCellStyle, depth, text);
					}

					cell.setProperty(DesignChoiceConstants.CHOICE_TEXT_ALIGN, DesignChoiceConstants.TEXT_ALIGN_LEFT);
					groupFooter.getCells().add(cell);
				} else {
					if(colCount_beforeAggregation > 0) {
						CellHandle cell = createIndentCell(colCount_beforeAggregation, 1, GroupDetailCellStyle, -1);
						groupFooter.getCells().add(cell);
					}
				}

				for(int index = colCount_beforeAggregation; index < columns.length; index++) {
					Column column = columns[index];

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
		if(hasAggregation()) {
			createGroup(table, null, -1);
		}

		Field[] groups = getGroups();

		for(int index = 0; index < groups.length; index++) {
			createGroup(table, groups[index], index);
		}
	}

	private Column getRootColumn() {
		return rootColumn;
	}

	private Column[] getColumns() {
		return rootColumn.getColumns();
	}

	private Column[] getIndentationColumns() {
		return indentationColumns;
	}

	private Field[] getGroups() {
		return groups;
	}

	private float getTotalWidth() {
		return getRootColumn().getTotalWidth();
	}

	private boolean hasAggregation() {
		for(Column column : getColumns()) {
			if(column.hasAggregation()) {
				return true;
			}
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

	private void initialize(Column rootColumn, Field[] groups) {
		if(m_initialized) {
			return;
		}

		m_initialized = true;

		if(rootColumn.getName() == null && rootColumn.getSubcolumns().length == 1) {
			rootColumn = rootColumn.getSubcolumns()[0];
			rootColumn.setParent(null);
		}

		for(Column column : rootColumn.getSubcolumns()) {
			if(column.isIndentation()) {
				rootColumn.removeColumn(column);
			}
		}

		indentationColumns = new Column[indentGroups() ? groups.length : 0];

		if(indentGroups()) {
			for(int i = indentationColumns.length - 1; i >= 0; i--) {
				Column column = Column.createIndentationColumn(options.indentGroupsBy);
				rootColumn.addColumn(column, 0);
				indentationColumns[i] = column;
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

			float pageWidth = 0;
			float tableWidth = getTotalWidth();

			if(options.pagesWide != 0 && format().equalsIgnoreCase(Reports.Pdf)) {
				float pageOverlapping = options.pagesWide > 1 ? options.pageOverlapping : 0;
				float paperWidth = options.pagesWide * (options.getPageWidth() - options.getHorizontalMargins() - options.pageOverlapping) + pageOverlapping;

				scaleFactor = paperWidth / tableWidth;
				pageWidth = options.pagesWide * options.getPageWidth();
			} else
				pageWidth = getTotalWidth() + options.getHorizontalMargins();

			masterPage.setProperty(IMasterPageModel.TYPE_PROP, DesignChoiceConstants.PAGE_SIZE_CUSTOM);

			masterPage.setProperty(IMasterPageModel.WIDTH_PROP, "" + pageWidth + "pt");
			masterPage.setProperty(IMasterPageModel.HEIGHT_PROP, "" + options.getPageHeight() + "pt");

			masterPage.setProperty(IMasterPageModel.LEFT_MARGIN_PROP, "" + options.getLeftMargin() + "pt");
			masterPage.setProperty(IMasterPageModel.RIGHT_MARGIN_PROP, "" + options.getRightMargin() + "pt");
			masterPage.setProperty(IMasterPageModel.TOP_MARGIN_PROP, "" + options.getTopMargin() + "pt");
			masterPage.setProperty(IMasterPageModel.BOTTOM_MARGIN_PROP, "" + options.getBottomMargin() + "pt");
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

			ReadAction action = options.actions.iterator().next();

			dataSetHandle.setProperty("queryText", action.getQuery().classId());
			return dataSetHandle;
		} catch(Exception e) {
			throw new RuntimeException(e);
		}
	}

	public int calculateMaxPagesWide(Column rootColumn, Field[] groups) {
		initialize(rootColumn, groups);

		float tableWidth = getTotalWidth();
		float paperWidth = options.getPageWidth() - options.getHorizontalMargins() - options.pageOverlapping;

		return (int)Math.ceil(tableWidth / paperWidth);
	}

	public String execute(Collection<Field> fields, Collection<Field> groupFields) {
		Column column = new Column();

		for(Field field : fields) {
			Column c = new Column(field.displayName());
			c.setField(field);
			c.setWidth(field.width());
			column.addColumn(c);
		}

		return executeDynamic(column, groupFields.toArray(new Field[0]));
	}

	public String executeDynamic(Column rootColumn, Field[] groups) {
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

		return generateAndSplit(reportDesignHandle);
	}

	private File getUniqueFileName(File folder, String name, String extension) {
		int index = 0;

		name = name.replace('/', '-').replace('\\', '-').replace(':', '-').replace('\n', ' ');

		if(name.endsWith(".")) {
			name = name.substring(0, name.length() - 1);
		}

		while(true) {
			String suffix = index != 0 ? (" (" + index + ")") : "";
			File file = new File(folder, name + suffix + "." + extension);

			if(!file.exists()) {
				return file;
			}

			index++;
		}
	}

	private String generateAndSplit(ReportDesignHandle reportDesignHandle) {
		File outputFile = null;
		File splittedFile = null;

		try {
			File folder = ReportOptions.getReportOutputFolder();

			outputFile = getUniqueFileName(folder, options.documentName(), format());
			outputFile.deleteOnExit();

			run(reportDesignHandle, outputFile);

			if(isSplitNeeded()) {
				splittedFile = getUniqueFileName(folder, options.documentName(), format());
				splittedFile.deleteOnExit();

				FileOutputStream output = null;
				FileInputStream input = null;

				try {
					output = new FileOutputStream(splittedFile);
					input = new FileInputStream(outputFile);
					PdfSplitter.split(options, input, output);
				} finally {
					IOUtils.closeQuietly(output);
					IOUtils.closeQuietly(input);
				}

				return new File(Folders.ReportsOutput, splittedFile.getName()).getPath();
			} else
				return new File(Folders.ReportsOutput, outputFile.getName()).getPath();
		} catch(IOException e) {
			throw new RuntimeException(e);
		}
	}

	public String execute() throws RuntimeException {
		return runAndRenderReport();
	}

	private String runAndRenderReport() throws RuntimeException {
		File temporaryFile = null;

		try {
			File folder = ReportOptions.getReportOutputFolder();
			temporaryFile = File.createTempFile("report", "." + format(), folder);

			run(temporaryFile);

			return new File(Folders.ReportsOutput, temporaryFile.getName()).getPath();
		} catch(IOException e) {
			throw new RuntimeException(e);
		} finally {
			if(temporaryFile != null) {
				try {
					temporaryFile.deleteOnExit();
				} catch(SecurityException e) {
					throw new RuntimeException(e);
				}
			}
		}
	}

	private void run(ReportDesignHandle reportDesignHandle, File outputFile) throws RuntimeException {
		try {
			IReportRunnable runnable = options.reportEngine().openReportDesign(reportDesignHandle);
			run(runnable, outputFile);
		} catch(EngineException e) {
			throw new RuntimeException(e);
		}
	}

	private void run(File outputFile) throws RuntimeException {
		try {
			IReportRunnable runnable = options.reportEngine().openReportDesign(reportDesignFile().getAbsolutePath());
			run(runnable, outputFile);
		} catch(EngineException e) {
			throw new RuntimeException(e);
		}
	}

	private void run(IReportRunnable runnable, File outputFile) throws RuntimeException {
		IRunAndRenderTask task = null;
		try {
			task = options.reportEngine().createRunAndRenderTask(runnable);

			HashMap<String, Object> contextMap = new HashMap<String, Object>();

			for(ReadAction action : options.actions)
				contextMap.put(action.getQuery().classId(), action);

			task.setAppContext(contextMap);

			IRenderOption options = new RenderOption();
			options.setOutputFormat(format()); // pdf, doc, ppt, html, xls
			options.setOutputFileName(outputFile.getAbsolutePath());

			if(format().equalsIgnoreCase(Reports.Html)) {
				HTMLRenderOption htmlOptions = new HTMLRenderOption(options);
				htmlOptions.setHtmlPagination(true);
				htmlOptions.setImageDirectory("image");
				options = htmlOptions;
			}

			task.setRenderOption(options);
			task.run();
		} catch(EngineException e) {
			throw new RuntimeException(e);
		} finally {
			if(task != null)
				task.close();
		}
	}

	static List<String> getReportTemplates(String className) {
		ReportBindingFileReader xmlReader = new ReportBindingFileReader();
		List<ReportInfo> reports = xmlReader.getReportTemplateFileNames(className);

		List<String> result = new ArrayList<String>();

		for(ReportInfo info : reports) {
			result.add(info.fileName());
		}

		return result;
	}
}
