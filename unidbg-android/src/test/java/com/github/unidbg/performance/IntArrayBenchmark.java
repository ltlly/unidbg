package com.github.unidbg.performance;

import com.github.unidbg.AndroidEmulator;
import com.github.unidbg.arm.backend.Backend;
import com.github.unidbg.arm.backend.Unicorn2Factory;
import com.github.unidbg.linux.android.AndroidEmulatorBuilder;
import com.github.unidbg.pointer.UnidbgPointer;
import com.sun.management.ThreadMXBean;
import java.lang.management.ManagementFactory;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Locale;
import unicorn.UnicornConst;

/** Standalone native-backend microbenchmark; not an end-to-end application benchmark. */
public final class IntArrayBenchmark {
    private static volatile long sink;
    public static void main(String[] args) throws Exception {
        int count = Integer.parseInt(args[0]);
        int iterations = Integer.parseInt(args[1]);
        boolean arm64 = Boolean.parseBoolean(args[2]);
        boolean unicorn2 = Boolean.parseBoolean(args[3]);
        AndroidEmulatorBuilder builder = arm64 ? AndroidEmulatorBuilder.for64Bit() : AndroidEmulatorBuilder.for32Bit();
        if (unicorn2) builder.addBackendFactory(new Unicorn2Factory(false));
        try (AndroidEmulator emulator = builder.build()) {
            Backend backend = emulator.getBackend();
            long address = 0x40000000L;
            long bytes = (count * 4L + 4095) & ~4095L;
            backend.mem_map(address, Math.max(bytes, 4096), UnicornConst.UC_PROT_READ | UnicornConst.UC_PROT_WRITE);
            byte[] input = new byte[count * 4];
            ByteBuffer buffer = ByteBuffer.wrap(input).order(ByteOrder.LITTLE_ENDIAN);
            long expected = 0;
            for (int i = 0; i < count; i++) { int value = i * 0x9e3779b9; buffer.putInt(value); expected += value; }
            backend.mem_write(address, input);
            UnidbgPointer pointer = UnidbgPointer.pointer(emulator, address).setSize(input.length);
            for (int i = 0; i < Math.min(iterations, 2000); i++) consume(pointer.getIntArray(0, count), expected);
            ThreadMXBean bean = (ThreadMXBean) ManagementFactory.getThreadMXBean();
            if (bean.isThreadAllocatedMemorySupported()) bean.setThreadAllocatedMemoryEnabled(true);
            long thread = Thread.currentThread().getId();
            long allocated = bean.getThreadAllocatedBytes(thread);
            long start = System.nanoTime();
            for (int i = 0; i < iterations; i++) consume(pointer.getIntArray(0, count), expected);
            long elapsed = System.nanoTime() - start;
            allocated = bean.getThreadAllocatedBytes(thread) - allocated;
            System.out.printf(Locale.ROOT,
                "RESULT backend=%s arm64=%s count=%d iterations=%d ns_per_op=%.3f bytes_per_op=%.3f checksum=%d%n",
                backend.getClass().getSimpleName(), arm64, count, iterations,
                (double) elapsed / iterations, (double) allocated / iterations, sink);
        }
    }
    private static void consume(int[] values, long expected) {
        long sum = 0;
        for (int value : values) sum += value;
        if (sum != expected) throw new AssertionError("Incorrect memory read: " + sum + " != " + expected);
        sink = sum;
    }
}
