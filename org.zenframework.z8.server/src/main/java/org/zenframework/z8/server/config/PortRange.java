package org.zenframework.z8.server.config;

import java.util.Random;

public class PortRange {

	private final Random random = new Random();
	private final int range[];

	public PortRange(int... range) {
		this.range = range;
	}

	public boolean isInRange(int value) {
		for (int i = 0; i < range.length; i += 2) {
			if (range[i] <= value && value <= range[i + 1])
				return true;
		}
		return false;
	}

	public int getRandomPort() {
		int n = random.nextInt(range.length / 2);
		return random.nextInt(range[n * 2 + 1] - range[n * 2] + 1) + range[n * 2];
	}

	public static PortRange parsePortRange(String spec) {
		String parts[] = spec.split("\\,");
		int range[] = new int[parts.length * 2];
		for (int i = 0; i < parts.length; i++) {
			String minMax[] = parts[i].split("\\-");
			range[i * 2] = Integer.parseInt(minMax[0].trim());
			range[i * 2 + 1] = minMax.length == 1 ? range[i * 2] : Integer.parseInt(minMax[1].trim());
		}
		return new PortRange(range);
	}

}
