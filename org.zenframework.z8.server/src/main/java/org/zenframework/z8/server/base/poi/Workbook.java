package org.zenframework.z8.server.base.poi;

import java.io.File;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.zenframework.z8.server.base.poi.Sheet;
import org.zenframework.z8.server.runtime.IObject;
import org.zenframework.z8.server.runtime.OBJECT;
import org.zenframework.z8.server.types.file;
import org.zenframework.z8.server.types.integer;

public class Workbook extends OBJECT {
	public static class CLASS<T extends Workbook> extends OBJECT.CLASS<T> {
		public CLASS(IObject container) {
			super(container);
			this.setJavaClass(Workbook.class);
		}

		public Object newObject(IObject container) {
			return new Workbook(container);
		}
	}

	public org.apache.poi.ss.usermodel.Workbook workbook;
	private OPCPackage opcPackage;

	public Workbook(IObject container) {
		super(container);
	}

	public void operatorAssign(file file) {
		try {
			String e = file.getAbsolutePath();
			this.opcPackage = OPCPackage.open(new File(e));
			this.workbook = new XSSFWorkbook(this.opcPackage);
		} catch(Throwable var3) {
			this.safeClose();
			throw new RuntimeException(var3);
		}
	}

	private void safeClose() {
		try {
			if(this.opcPackage != null) {
				this.opcPackage.close();
			}
		} catch(Throwable e) {
		}

		this.workbook = null;
		this.opcPackage = null;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Sheet.CLASS<? extends Sheet> z8_getSheet(integer index) {
		Sheet.CLASS<Sheet> cls = new Sheet.CLASS(this);
		cls.get().sheet = this.workbook.getSheetAt(index.getInt());
		return cls;
	}

	public void z8_close() {
		this.safeClose();
	}
}
