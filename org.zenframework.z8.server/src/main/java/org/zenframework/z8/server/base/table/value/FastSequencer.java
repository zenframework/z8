package org.zenframework.z8.server.base.table.value;

public class FastSequencer {

	// 2026-10-01 - the day when queues switched to sequencer based on time
	public static final long DefaultSeed = 1790802000000l; // 2026-10-01 00:00:00:000

	public static long next(String key) {
		return next(key, DefaultSeed);
	}

	public static long next(String key, long seed) {
		return System.currentTimeMillis() - seed;
	}

}
