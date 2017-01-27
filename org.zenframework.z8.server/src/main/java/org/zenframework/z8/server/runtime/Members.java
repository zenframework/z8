package org.zenframework.z8.server.runtime;

import java.util.Collection;

public class Members extends RCollection<IClass<? extends IObject>> {
	private static final long serialVersionUID = -434029387095730997L;

	IObject owner = null;

	public Members(IObject owner) {
		super(/* true */);
		this.owner = owner;
	}

	@Override
	public boolean add(IClass<? extends IObject> member) {
		member.setOwner(owner);
		member.setOrdinal(size());
		return super.add(member);
	}

	@Override
	public void add(int index, IClass<? extends IObject> member) {
		member.setOwner(owner);
		super.add(index, member);
	}

	@Override
	public boolean addAll(int index, Collection<? extends IClass<? extends IObject>> members) {
		for(IClass<? extends IObject> member : members)
			member.setOwner(this.owner);

		return super.addAll(index, members);
	}
}
