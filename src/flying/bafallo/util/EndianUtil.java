package flying.bafallo.util;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class EndianUtil {
	
	public static int byteArrayToInt(byte[] b) {
	    final ByteBuffer bb = ByteBuffer.wrap(b);
	    bb.order(ByteOrder.LITTLE_ENDIAN);
	    return bb.getInt();
	}

	public static byte[] intToByteArray(int i) {
	    final ByteBuffer bb = ByteBuffer.allocate(Integer.SIZE / Byte.SIZE);
	    bb.order(ByteOrder.LITTLE_ENDIAN);
	    bb.putInt(i);
	    return bb.array();
	}
	
	public static long byteArrayToLong(byte[] b) {
	    final ByteBuffer bb = ByteBuffer.wrap(b);
	    bb.order(ByteOrder.LITTLE_ENDIAN);
	    return bb.getLong();
	}

	public static byte[] longToByteArray(long l) {
	    final ByteBuffer bb = ByteBuffer.allocate(Long.SIZE / Byte.SIZE);
	    bb.order(ByteOrder.LITTLE_ENDIAN);
	    bb.putLong(l);
	    return bb.array();
	}
	
	public static long unsigned32(int n) {
	    return n & 0xFFFFFFFFL;
	}
}
