package com.github.unidbg.pointer;

import com.github.unidbg.InvalidMemoryAccessException;
import junit.framework.TestCase;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Random;

public class IntArrayTest extends TestCase {
    public void testLittleEndianUnalignedAndChunkBoundaries() {
        Random random = new Random(78123);
        for (int count : new int[]{0, 1, 2, 15, 16, 1023, 1024, 1025, 4096, 4097, 16384}) {
            byte[] data = new byte[count * 4 + 13];
            random.nextBytes(data);
            UnidbgPointer pointer = new UnidbgPointer(null, data);
            for (int offset : new int[]{0, 1, 7, 13}) {
                int[] actual = pointer.getIntArray(offset, count);
                assertEquals(count, actual.length);
                ByteBuffer expected = ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN);
                for (int i = 0; i < count; i++) {
                    assertEquals("count=" + count + ", offset=" + offset + ", i=" + i,
                            expected.getInt(offset + i * 4), actual[i]);
                }
            }
        }
    }

    public void testSizeAndInvalidLength() {
        UnidbgPointer pointer = new UnidbgPointer(null, new byte[32]).setSize(16);
        assertEquals(4, pointer.getIntArray(0, 4).length);
        assertEquals(0, pointer.getIntArray(999, 0).length);
        for (int count : new int[]{-1, 0x7ffffff}) {
            try { pointer.getIntArray(0, count); fail("invalid count accepted"); }
            catch (InvalidMemoryAccessException expected) { }
        }
        try { pointer.getIntArray(1, 4); fail("size overrun accepted"); }
        catch (InvalidMemoryAccessException expected) { }
    }
}
