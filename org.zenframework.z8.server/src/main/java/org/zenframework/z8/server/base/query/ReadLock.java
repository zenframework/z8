package org.zenframework.z8.server.base.query;

public enum ReadLock {
	None(Names.None),
	Update(Names.Update),
	Share(Names.Share),
	UpdateNoWait(Names.UpdateNoWait),
	ShareNoWait(Names.ShareNoWait);

	class Names {
		static protected final String None = "";
		static protected final String Share = "for share";
		static protected final String Update = "for update";
		static protected final String ShareNoWait = "for share nowait";
		static protected final String UpdateNoWait = "for update nowait";
	}

	private String fName = null;

	ReadLock(String name) {
		fName = name;
	}

	@Override
	public String toString() {
		return fName;
	}
}
