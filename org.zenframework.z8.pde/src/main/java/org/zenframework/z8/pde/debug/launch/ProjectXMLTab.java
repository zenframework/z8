package org.zenframework.z8.pde.debug.launch;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTab;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import org.zenframework.z8.pde.Plugin;
import org.zenframework.z8.pde.preferences.PreferencePage;
import org.zenframework.z8.pde.preferences.PreferencePageConsts;

public class ProjectXMLTab extends AbstractLaunchConfigurationTab {
	final static String PROJECT_XML_KEYS = "project.xml.keys";
	final static String PROJECT_XML_VALUES = "project.xml.values";
	final static String PROJECT_XML_COMMENTS = "project.xml.comments";
	final static String PROJECT_XML_ENABLED = "project.xml.enabled";

	List<String> keys = null;
	List<String> values = null;
	List<String> comments = null;
	List<String> enabled = null;

	private List<Integer> valueId = null;
	private Integer currValueId = 0;

	private final static List<String> EMPTY = new ArrayList<String>(0);

	private Button m_importButton;
	private Button m_exportButton;
	private Button m_addButton;

	private PreferencePage preferences = new PreferencePage();

	private Composite topLevel;

	private ScrolledComposite toppestLevel;

	private Composite fields = null;

	@Override
	public void createControl(final Composite parent) {
		toppestLevel = new ScrolledComposite(parent, SWT.H_SCROLL | SWT.V_SCROLL);
		toppestLevel.setExpandHorizontal(true);
		toppestLevel.setExpandVertical(true);
		setControl(toppestLevel);
		topLevel = new Composite(toppestLevel, SWT.NONE);
		GridLayout topLayout = new GridLayout(1, false);
		topLevel.setLayout(topLayout);
		Composite buttonLevel = new Composite(topLevel, SWT.NONE);
		GridLayout buttonLayout = new GridLayout(1, false);
		buttonLevel.setLayout(buttonLayout);
		m_importButton = createPushButton(buttonLevel, "Импорт параметров", null);
		// m_importButton.setData(gd);
		m_exportButton = createPushButton(buttonLevel, "Экспорт параметров", null);
		// m_exportButton.setData(gd);
		m_addButton = createPushButton(buttonLevel, "Добавить параметр", null);
		// m_addButton.setData(gd);
		toppestLevel.setContent(topLevel);
		m_importButton.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}

			@Override
			public void widgetSelected(SelectionEvent e) {
				FileDialog fd = new FileDialog(parent.getShell());
				fd.setFilterExtensions(new String[] { "*.xml" });
				fd.setText("Выберите файл для импортирования");
				String path = preferences.getAttribute(PreferencePageConsts.ATTR_WEB_INF_PATH, ResourcesPlugin.getWorkspace().getRoot().getFullPath().toOSString());
				fd.setFilterPath(path);
				String xmlPath = fd.open();
				if(xmlPath == null)
					return;
				try {
					import_config(xmlPath);
					createWidgets();
					setDirty(true);
					updateLaunchConfigurationDialog();

				} catch(Exception ev) {
					Plugin.log(ev);
				}
			}

		});

		m_exportButton.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}

			@Override
			public void widgetSelected(SelectionEvent e) {
				FileDialog fd = new FileDialog(parent.getShell());
				fd.setFilterExtensions(new String[] { "*.xml" });
				fd.setText("Выберите файл для экспортирования");
				String path = preferences.getAttribute(PreferencePageConsts.ATTR_WEB_INF_PATH, ResourcesPlugin.getWorkspace().getRoot().getFullPath().toOSString());
				fd.setFilterPath(path);
				String xmlPath = fd.open();
				if(xmlPath == null)
					return;
				try {
					File f = new File(xmlPath);
					FileWriter fb = new FileWriter(f);
					fb.append(export());
					fb.close();

				} catch(Exception ev) {
					Plugin.log(ev);
				}
			}

		});

		m_addButton.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}

			@Override
			public void widgetSelected(SelectionEvent e) {
				String[] diaValues = getKeyFromDialogs();

				if(diaValues == null)
					return;

				keys.add(diaValues[0]);
				comments.add(diaValues[1]);
				values.add("");
				enabled.add("true");

				valueId.add(currValueId);
				currValueId++;

				addValueWidgets(keys.size() - 1);
				updateLayout();
				setDirty(true);
				updateLaunchConfigurationDialog();
			}

		});

	}

	private String[] getKeyFromDialogs() {
		return getKeyFromDialogs("", "");
	}

	private String[] getKeyFromDialogs(final String defKey, String defComment) {
		String[] res = new String[2];
		InputDialog dia = new InputDialog(getShell(), "Название параметра", "Введите название параметра:", defKey, new IInputValidator() {

			@Override
			public String isValid(String newText) {
				if(newText.isEmpty())
					return "Название параметра не может быть пустым";
				if(!newText.equals(defKey))
					if(keys.contains(newText))
						return "Такой параметр уже есть";
				return null;
			}

		});

		int result = dia.open();
		if(result != InputDialog.OK)
			return null;
		res[0] = dia.getValue();

		dia = new InputDialog(getShell(), "Описание параметра", "Введите описание параметра:", defComment, new IInputValidator() {

			@Override
			public String isValid(String newText) {
				if(newText.isEmpty())
					return "Описание параметра не может быть пустым";
				return null;
			}

		});

		result = dia.open();
		if(result != InputDialog.OK)
			return null;
		res[1] = dia.getValue();

		return res;
	}

	@Override
	public String getName() {
		return "Project.xml";
	}

	@Override
	public void initializeFrom(ILaunchConfiguration configuration) {
		if(fields != null) {
			fields.dispose();
			fields = null;
		}
		try {
			keys = new ArrayList<String>();
			values = new ArrayList<String>();
			comments = new ArrayList<String>();
			enabled = new ArrayList<String>();
			valueId = new ArrayList<Integer>();
			keys.addAll(configuration.getAttribute(PROJECT_XML_KEYS, EMPTY));
			values.addAll(configuration.getAttribute(PROJECT_XML_VALUES, EMPTY));
			comments.addAll(configuration.getAttribute(PROJECT_XML_COMMENTS, EMPTY));
			enabled.addAll(configuration.getAttribute(PROJECT_XML_ENABLED, EMPTY));
			if(values.size() != keys.size() || comments.size() != keys.size() || enabled.size() != keys.size()) {
				keys.clear();
				values.clear();
				comments.clear();
				enabled.clear();
			} else {
				for(int i = 0; i < keys.size(); i++)
					valueId.add(i);
				currValueId = keys.size();
			}
		} catch(Exception e) {
			Plugin.log(e);
		}
		if(values.isEmpty() || comments.isEmpty()) {
			// GridData gd = new GridData(SWT.FILL);
			fields = new Composite(topLevel, SWT.NONE);
			// fields.setData(gd);
			GridLayout topLayout = new GridLayout(1, false);
			fields.setLayout(topLayout);
		} else {
			createWidgets();
		}
	}

	public void createWidgets() {
		if(fields != null) {
			fields.dispose();
			fields = null;
		}
		fields = new Composite(topLevel, SWT.NONE);
		// fields = new ScrolledComposite(topLevel, SWT.BORDER | SWT.H_SCROLL |
		// SWT.V_SCROLL);
		GridLayout topLayout = new GridLayout(1, false);
		fields.setLayout(topLayout);
		for(int i = 0; i < keys.size(); i++) {
			addValueWidgets(i);
		}
		updateLayout();
	}

	public void import_config(String xmlPath) throws IOException, ParserConfigurationException, SAXException {
		keys = new ArrayList<String>();
		values = new ArrayList<String>();
		comments = new ArrayList<String>();
		enabled = new ArrayList<String>();
		valueId = new ArrayList<Integer>();
		DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
		Document doc;
		try {
			doc = docBuilder.parse(new File(xmlPath));
		} catch(FileNotFoundException f) {
			return;
		}
		doc.getDocumentElement().normalize();
		NodeList nl = doc.getChildNodes();
		Node props = null;
		for(int i = 1; i < nl.getLength(); i++) {
			if(nl.item(i).getNodeName().equals("properties")) {
				props = nl.item(i);
				break;
			}
		}
		if(props == null)
			return;

		String searchStr = "<entry key=\"";
		for(int i = 0; i < props.getChildNodes().getLength(); i++) {
			Node n = props.getChildNodes().item(i);
			String key = null;
			String value = null;
			String en = null;
			// если ключ не закомментирован
			if(n.getNodeName().equals("entry")) {
				key = n.getAttributes().getNamedItem("key").getNodeValue();
				value = n.getTextContent();
				en = "true";
			}
			// если закомментирован
			else if(n.getNodeType() == Node.COMMENT_NODE && n.getNodeValue().contains(searchStr)) {
				String s = n.getNodeValue();
				int begKey = s.indexOf(searchStr) + searchStr.length();
				int endKey = s.indexOf('"', begKey);
				int begValue = endKey + 2;
				int endValue = s.indexOf('<', begValue);
				key = s.substring(begKey, endKey);
				value = s.substring(begValue, endValue);
				en = "false";
			}
			if(key != null) {
				// Получаем значение комментария
				Node comm = null;
				if(i >= 2)
					comm = props.getChildNodes().item(i - 2);
				String comment = null;
				if(comm != null)
					if(comm.getNodeType() == Node.COMMENT_NODE) {
						comment = comm.getNodeValue();
						// возможно, это закомментированный entry
						// тогда комментарий считаем отсутствующим
						if(comment.contains(searchStr))
							comment = null;
					}
				if(comment != null)
					comment = comment.trim();
				else
					comment = "";
				keys.add(key);
				values.add(value);
				comments.add(comment);
				enabled.add(en);
				valueId.add(currValueId);
				currValueId++;
			}
		}
	}

	private String getValueLabel(int index) {
		String key = keys.get(index);
		String comment = comments.get(index);
		if(comment.length() > 0) {
			if(comment.contains("\n"))
				comment = "\n\t" + comment;
			else
				comment = " (" + comment + ")";

		}
		return key + comment;
	}

	private void addValueWidgets(int index) {
		final Integer id = valueId.get(index);
		final Label l = new Label(fields, SWT.NONE);
		final Composite composite = new Composite(fields, SWT.NONE);
		GridLayout topLayout = new GridLayout(3, false);
		composite.setLayout(topLayout);
		l.setText(getValueLabel(index));
		l.setToolTipText("Двойной клик мышью для редактирования параметра");
		l.addMouseListener(new MouseListener() {

			@Override
			public void mouseDoubleClick(MouseEvent e) {
				int index = valueId.indexOf(id);

				String[] diaValues = getKeyFromDialogs(keys.get(index), comments.get(index));

				if(diaValues == null)
					return;

				keys.set(index, diaValues[0]);
				comments.set(index, diaValues[1]);

				l.setText(getValueLabel(index));
				updateLayout();
			}

			@Override
			public void mouseDown(MouseEvent e) {
			}

			@Override
			public void mouseUp(MouseEvent e) {
			}

		});
		GridData gd = new GridData(200, SWT.DEFAULT);
		final Text t = new Text(composite, SWT.BORDER | SWT.FILL);
		t.setLayoutData(gd);
		t.setText(values.get(index));
		t.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				int index = valueId.indexOf(id);
				values.set(index, t.getText());
				setDirty(true);
				updateLaunchConfigurationDialog();
			}

		});
		t.setEnabled(enabled.get(index).equals("true"));

		final Button c = createCheckButton(composite, "");
		gd = new GridData(15, SWT.DEFAULT);
		c.setLayoutData(gd);
		c.setToolTipText("Использовать параметр при запуске");

		c.setSelection(enabled.get(index).equals("true"));
		c.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}

			@Override
			public void widgetSelected(SelectionEvent e) {
				int index = valueId.indexOf(id);
				enabled.set(index, c.getSelection() ? "true" : "false");
				setDirty(true);
				updateLaunchConfigurationDialog();
				t.setEnabled(c.getSelection());

			}
		});
		final Button b = createPushButton(composite, "X", null);
		gd = new GridData(15, SWT.DEFAULT);
		b.setLayoutData(gd);
		b.setToolTipText("Удалить параметр");
		b.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}

			@Override
			public void widgetSelected(SelectionEvent e) {
				int ind = valueId.indexOf(id);
				values.remove(ind);
				comments.remove(ind);
				keys.remove(ind);
				enabled.remove(ind);
				valueId.remove(ind);
				l.dispose();
				t.dispose();
				b.dispose();
				c.dispose();
				composite.dispose();
				updateLayout();
				setDirty(true);
				updateLaunchConfigurationDialog();
			}
		});
	}

	@Override
	public void performApply(ILaunchConfigurationWorkingCopy configuration) {
		configuration.setAttribute(PROJECT_XML_KEYS, keys);
		configuration.setAttribute(PROJECT_XML_COMMENTS, comments);
		configuration.setAttribute(PROJECT_XML_VALUES, values);
		configuration.setAttribute(PROJECT_XML_ENABLED, enabled);
	}

	@Override
	public void setDefaults(ILaunchConfigurationWorkingCopy configuration) {
		try {
			String webInfPath = new PreferencePage().getAttribute(PreferencePageConsts.ATTR_WEB_INF_PATH, "");
			IPath p = new Path(webInfPath);
			p = p.append("project.xml");
			String xmlPath = p.toOSString();
			if(!new File(xmlPath).exists())
				return;
			import_config(xmlPath);
			configuration.setAttribute(PROJECT_XML_KEYS, keys);
			configuration.setAttribute(PROJECT_XML_COMMENTS, comments);
			configuration.setAttribute(PROJECT_XML_VALUES, values);
			configuration.setAttribute(PROJECT_XML_ENABLED, enabled);
			keys = null;
			values = null;
			comments = null;
			enabled = null;
			valueId = null;
		} catch(Exception e) {
			Plugin.log(e);
		}
	}

	private void updateLayout() {
		toppestLevel.setMinSize(topLevel.computeSize(SWT.DEFAULT, SWT.DEFAULT, true));
		toppestLevel.layout(true);
		topLevel.layout(true);
		fields.layout(true);
		/*
		 * fields.update(); fields.redraw(); toppestLevel.setContent(topLevel);
		 * toppestLevel.layout(true); toppestLevel.update();
		 */
	}

	@Override
	public boolean isValid(ILaunchConfiguration config) {
		super.isValid(config);
		setErrorMessage(null);
		if(values.isEmpty() || comments.isEmpty()) {
			setMessage(null);
			setErrorMessage("Не установлены настройки project.xml");
			return false;
		}
		setMessage("Настройки project.xml");
		return true;
	}

	String export() {
		String result = "<?xml version=\"1.0\" encoding=\"WINDOWS-1251\"?>\r\n";
		result += "<!DOCTYPE properties SYSTEM \"http://java.sun.com/dtd/properties.dtd\">\r\n";
		result += "<properties>\r\n";
		result += "<comment>Тестовая схема аналитиков</comment>\r\n";
		for(int i = 0; i < keys.size(); i++) {
			String comment = comments.get(i);
			if(comment.length() > 0) {
				if(comment.contains("\n"))
					result += ("<!--\n\t" + comment + "\n-->\n").replace("\n", "\r\n");
				else
					result += "<!-- " + comment + " -->\r\n";
			}
			boolean e = enabled.get(i).equals("true");
			if(!e)
				result += "<!-- ";
			result += "<entry key=\"" + keys.get(i) + "\">" + values.get(i) + "</entry>";
			if(!e)
				result += " -->";
			result += "\r\n";
		}
		result += "</properties>\r\n";
		return result;
	}

}
