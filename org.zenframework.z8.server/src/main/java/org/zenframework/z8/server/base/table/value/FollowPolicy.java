package org.zenframework.z8.server.base.table.value;

public enum FollowPolicy {
	Default(Names.Default), Custom(Names.Custom);

	class Names {
		static protected final String Default = "default";
		static protected final String Custom = "custom";
	}

	private String fName = null;

	FollowPolicy(String name) {
		fName = name;
	}

	@Override
	public String toString() {
		return fName;
	}

	static public FollowPolicy fromString(String string) {
		if(Names.Default.equals(string))
			return FollowPolicy.Default;
		else if(Names.Custom.equals(string))
			return FollowPolicy.Custom;
		else
			throw new RuntimeException("Unknown anchor follow policy: '" + string + "'");
	}
}
