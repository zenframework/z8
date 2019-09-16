package org.zenframework.z8.server.ie;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;

import org.apache.commons.io.IOUtils;
import org.zenframework.z8.rmi.ObjectIO;
import org.zenframework.z8.server.base.Procedure;
import org.zenframework.z8.server.base.form.action.Parameter;
import org.zenframework.z8.server.base.table.system.Files;
import org.zenframework.z8.server.config.ServerConfig;
import org.zenframework.z8.server.engine.IApplicationServer;
import org.zenframework.z8.server.engine.Rmi;
import org.zenframework.z8.server.logs.Trace;
import org.zenframework.z8.server.runtime.IObject;
import org.zenframework.z8.server.runtime.RCollection;
import org.zenframework.z8.server.types.bool;
import org.zenframework.z8.server.types.file;

public class ExchangeJob extends Procedure {
	public static class CLASS<T extends ExchangeJob> extends Procedure.CLASS<T> {
		public CLASS(IObject container) {
			super(container);
			setJavaClass(ExchangeJob.class);
		}

		@Override
		public Object newObject(IObject container) {
			return new ExchangeJob(container);
		}
	}

	public ExchangeJob(IObject container) {
		super(container);
		useTransaction = bool.False;
	}

	@Override
	protected void z8_execute(RCollection<Parameter.CLASS<? extends Parameter>> parameters) {
		processFile(ServerConfig.exchangeFolderIn(), false);
	}
	
	private static void processFile(File file, boolean remove) {
		if (!file.exists())
			return;
		if (file.isFile()) {
			if (importMessage(file))
				file.delete();
			else
				moveToError(file);
		} else {
			File[] files = file.listFiles();
			Arrays.sort(files);
			for (File f : files)
				processFile(f, true);
			if (remove)
				file.delete();
		}
	}

	private static boolean importMessage(File f) {
		ObjectInputStream in = null;
		Object o = null;
		try {
			in = new ObjectInputStream(new FileInputStream(f));
			o = ObjectIO.read(in);
			if (o instanceof DataMessage) {
				Rmi.get(IApplicationServer.class).accept((DataMessage) o);
			} else if (o instanceof file) {
				file file = (file) o;
				Files files = Files.newInstance();
				if (!files.hasRecord(file.id))
					files.add(file);
			} else
				throw new IOException("Unsupported message type " + o.getClass().getName());
			return true;
		} catch (Exception e) {
			Trace.logError("Could not import file '" + f.getAbsolutePath() + "'", e);
			return false;
		} finally {
			IOUtils.closeQuietly(in);
		}
	}

	private static void moveToError(File file) {
		File errorFile = new File(ServerConfig.exchangeFolderErr(), file.getAbsolutePath().substring(ServerConfig.exchangeFolderIn().getAbsolutePath().length()));
		errorFile.getParentFile().mkdirs();
		try {
			java.nio.file.Files.move(file.toPath(), errorFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
		} catch (IOException e) {
			Trace.logError("Could not move file '" + file.getAbsolutePath() + "' to '" + errorFile.getAbsolutePath() + "'", e);
		}
	}
}
