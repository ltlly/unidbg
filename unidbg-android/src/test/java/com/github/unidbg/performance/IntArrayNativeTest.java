package com.github.unidbg.performance;

import com.github.unidbg.AndroidEmulator;
import com.github.unidbg.arm.backend.Backend;
import com.github.unidbg.arm.backend.BackendException;
import com.github.unidbg.arm.backend.Unicorn2Factory;
import com.github.unidbg.linux.android.AndroidEmulatorBuilder;
import com.github.unidbg.linux.android.dvm.VM;
import com.github.unidbg.linux.android.dvm.array.IntArray;
import com.github.unidbg.pointer.UnidbgPointer;
import junit.framework.TestCase;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Random;
import unicorn.UnicornConst;

public class IntArrayNativeTest extends TestCase {
    public void testArm32() throws Exception { verify(false); }
    public void testArm64() throws Exception { verify(true); }
    private void verify(boolean arm64) throws Exception {
        AndroidEmulatorBuilder builder = arm64 ? AndroidEmulatorBuilder.for64Bit() : AndroidEmulatorBuilder.for32Bit();
        builder.addBackendFactory(new Unicorn2Factory(false));
        try (AndroidEmulator emulator = builder.build()) {
            Backend backend = emulator.getBackend();
            long address = 0x40000000L;
            // Adjacent mappings and the end of the final mapped page are intentional.
            backend.mem_map(address, 4096, UnicornConst.UC_PROT_ALL);
            backend.mem_map(address + 4096, 4096, UnicornConst.UC_PROT_ALL);
            byte[] bytes = new byte[8192];
            new Random(154123).nextBytes(bytes);
            backend.mem_write(address, bytes);
            UnidbgPointer pointer = UnidbgPointer.pointer(emulator, address);
            ByteBuffer expected = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN);
            for (int offset : new int[]{0, 1, 4093, 4096}) {
                int count = (8192 - offset) / 4;
                int[] actual = pointer.getIntArray(offset, count);
                for (int i = 0; i < count; i++) assertEquals(expected.getInt(offset + i * 4), actual[i]);
            }
            assertEquals(expected.getInt(8188), pointer.getIntArray(8188, 1)[0]);
            boolean failed = false;
            try { pointer.getIntArray(8190, 1); }
            catch (BackendException expectedError) { failed = true; }
            assertTrue("unmapped reads must still fail", failed);
            pointer.setInt(0, 0x87654321);
            assertEquals(0x87654321, pointer.getIntArray(0, 1)[0]);
            pointer.setInt(0, 0x12345678);
            assertEquals(0x12345678, pointer.getIntArray(0, 1)[0]);
            VM vm = emulator.createDalvikVM();
            IntArray array = new IntArray(vm, new int[2048]);
            array._ReleaseArrayCritical(pointer, VM.JNI_COMMIT);
            assertEquals(0x12345678, array.getValue()[0]);
            for (int i = 1; i < 2048; i++) assertEquals(expected.getInt(i * 4), array.getValue()[i]);
        }
    }
}
