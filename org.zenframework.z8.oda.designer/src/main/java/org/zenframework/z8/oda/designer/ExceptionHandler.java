package org.zenframework.z8.oda.designer;

import java.io.PrintWriter;
import java.io.StringWriter;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IconAndMessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;

public class ExceptionHandler {

	public static void showException(Shell parentShell, String title, String msg, Throwable ex) {
		new ExceptionDialog(parentShell, title, msg, ex).open();
	}

	static class ExceptionDialog extends IconAndMessageDialog {

		private Button detailsButton;
		private Text textArea;
		private String title;
		private boolean textCreated = false;
		private Throwable exception;
		private Display display;

		protected ExceptionDialog(Shell parentShell, String title, String msg, Throwable ex) {
			super(parentShell);
			this.title = title;
			this.message = msg;
			this.exception = ex;
			if(parentShell != null)
				this.display = parentShell.getDisplay();
			else
				this.display = PlatformUI.getWorkbench().getDisplay().getActiveShell().getDisplay();

			setShellStyle(SWT.DIALOG_TRIM | SWT.RESIZE | SWT.APPLICATION_MODAL);
		}

		@Override
		protected void configureShell(Shell shell) {
			super.configureShell(shell);
			shell.setText(title);
		}

		@Override
		protected Image getImage() {
			return display.getSystemImage(SWT.ICON_ERROR);
		}

		@Override
		protected void createButtonsForButtonBar(Composite parent) {
			createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
			detailsButton = createButton(parent, IDialogConstants.DETAILS_ID, IDialogConstants.SHOW_DETAILS_LABEL, false);
		}

		@Override
		protected void buttonPressed(int id) {
			if(id == IDialogConstants.DETAILS_ID) {
				toggleDetailsArea();
			} else {
				super.buttonPressed(id);
			}
		}

		private void toggleDetailsArea() {
			Point windowSize = getShell().getSize();
			Point oldSize = getShell().computeSize(SWT.DEFAULT, SWT.DEFAULT);

			if(textCreated) {
				textArea.dispose();
				textCreated = false;
				detailsButton.setText(IDialogConstants.SHOW_DETAILS_LABEL);
			} else {
				textArea = createTextArea((Composite)getContents());
				detailsButton.setText(IDialogConstants.HIDE_DETAILS_LABEL);
			}

			Point newSize = getShell().computeSize(SWT.DEFAULT, SWT.DEFAULT);

			getShell().setSize(new Point(windowSize.x, windowSize.y + (newSize.y - oldSize.y)));

		}

		@Override
		protected Control createDialogArea(Composite parent) {

			createMessageArea(parent);

			Composite composite = new Composite(parent, SWT.NONE);
			GridLayout layout = new GridLayout();
			layout.marginHeight = convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_MARGIN);
			layout.marginWidth = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_MARGIN);
			layout.verticalSpacing = convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_SPACING);
			layout.horizontalSpacing = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_SPACING);
			layout.numColumns = 2;
			composite.setLayout(layout);
			GridData childData = new GridData(GridData.FILL_BOTH);
			childData.horizontalSpan = 2;
			composite.setLayoutData(childData);
			composite.setFont(parent.getFont());
			return composite;

		}

		protected Text createTextArea(Composite parent) {
			textArea = new Text(parent, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL | SWT.MULTI);

			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			exception.printStackTrace(pw);

			textArea.setText(sw.toString());

			GridData data = new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.GRAB_HORIZONTAL | GridData.VERTICAL_ALIGN_FILL | GridData.GRAB_VERTICAL);
			data.heightHint = 200;
			data.horizontalSpan = 2;
			textArea.setLayoutData(data);
			textArea.setFont(parent.getFont());
			textArea.setEditable(false);
			textCreated = true;
			return textArea;
		}
	}

}
