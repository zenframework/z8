package java.rmi.dgc;

import java.rmi.server.UID;
import java.security.SecureRandom;

public final class VMID implements java.io.Serializable {

	private static final byte[] randomBytes;

	public byte[] addr;
	public UID uid;

	private static final long serialVersionUID = -538642295484486218L;

	static {
		SecureRandom secureRandom = new SecureRandom();
		byte bytes[] = new byte[8];
		secureRandom.nextBytes(bytes);
		randomBytes = bytes;
	}

	public VMID() {
		addr = randomBytes;
		uid = new UID();
	}

	@Deprecated
	public static boolean isUnique() {
		return true;
	}

	@Override
	public int hashCode() {
		return uid.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof VMID) {
			VMID vmid = (VMID) obj;
			if (!uid.equals(vmid.uid))
				return false;
			if ((addr == null) ^ (vmid.addr == null))
				return false;
			if (addr != null) {
				if (addr.length != vmid.addr.length)
					return false;
				for (int i = 0; i < addr.length; ++i)
					if (addr[i] != vmid.addr[i])
						return false;
			}
			return true;
		} else {
			return false;
		}
	}

	@Override
	public String toString() {
		StringBuffer result = new StringBuffer();
		if (addr != null)
			for (int i = 0; i < addr.length; ++i) {
				int x = addr[i] & 0xFF;
				result.append((x < 0x10 ? "0" : "") + Integer.toString(x, 16));
			}
		result.append(':');
		result.append(uid.toString());
		return result.toString();
	}
}
