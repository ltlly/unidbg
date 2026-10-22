package com.github.unidbg.performance;

import com.github.unidbg.AndroidEmulator;
import com.github.unidbg.arm.backend.Backend;
import com.github.unidbg.arm.backend.Unicorn2Factory;
import com.github.unidbg.linux.android.AndroidEmulatorBuilder;
import com.github.unidbg.linux.android.dvm.VM;
import com.github.unidbg.linux.android.dvm.array.IntArray;
import com.github.unidbg.pointer.UnidbgPointer;
import com.sun.management.ThreadMXBean;
import java.lang.management.ManagementFactory;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Locale;
import unicorn.UnicornConst;

/** Complete guest-entry/JNI-dispatch transactions plus an unaffected CPU control. */
public final class NativeWorkloadBenchmark {
    private static volatile long sink;
    public static void main(String[] args) throws Exception {
        String mode = args[0];
        int count = Integer.parseInt(args[1]);
        int iterations = Integer.parseInt(args[2]);
        boolean arm64 = Boolean.parseBoolean(args[3]);
        AndroidEmulatorBuilder builder = arm64 ? AndroidEmulatorBuilder.for64Bit() : AndroidEmulatorBuilder.for32Bit();
        builder.addBackendFactory(new Unicorn2Factory(false));
        try (AndroidEmulator emulator = builder.build()) {
            Backend backend = emulator.getBackend();
            final Runnable operation;
            if ("jni".equals(mode)) {
                VM vm = emulator.createDalvikVM();
                IntArray array = new IntArray(vm, new int[count]);
                int handle = vm.addLocalObject(array);
                long address = 0x40000000L;
                backend.mem_map(address, Math.max(4096, (count * 4L + 4095) & ~4095L), UnicornConst.UC_PROT_ALL);
                byte[] bytes = new byte[count * 4];
                ByteBuffer data = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN);
                long checksum = 0;
                for (int i = 0; i < count; i++) { int value = i * 0x9e3779b9; data.putInt(value); checksum += value; }
                backend.mem_write(address, bytes);
                long env = UnidbgPointer.nativeValue(vm.getJNIEnv());
                // JNI function table index 211: SetIntArrayRegion, existing unidbg implementation.
                long function = UnidbgPointer.nativeValue(vm.getJNIEnv().getPointer(0).getPointer(211L * (arm64 ? 8 : 4)));
                final long expected = checksum;
                final Number envArgument;
                final Number addressArgument;
                if (arm64) { envArgument = env; addressArgument = address; }
                else { envArgument = (int) env; addressArgument = (int) address; }
                operation = () -> {
                    emulator.eFunc(function, envArgument, handle, 0, count, addressArgument);
                    long sum = 0;
                    for (int value : array.getValue()) sum += value;
                    if (sum != expected) throw new AssertionError("JNI output mismatch");
                    sink = sum;
                };
            } else if ("compute".equals(mode)) {
                long address = 0x50000000L;
                backend.mem_map(address, 4096, UnicornConst.UC_PROT_ALL);
                // subs register0, register0, #1; b.ne loop; ret/bx lr
                int[] instructions = arm64 ? new int[]{0xf1000400, 0x54ffffe1, 0xd65f03c0}
                        : new int[]{0xe2500001, 0x1afffffd, 0xe12fff1e};
                ByteBuffer code = ByteBuffer.allocate(12).order(ByteOrder.LITTLE_ENDIAN);
                for (int instruction : instructions) code.putInt(instruction);
                backend.mem_write(address, code.array());
                operation = () -> {
                    long result = emulator.eFunc(address, count).longValue();
                    if (result != 0) throw new AssertionError("Guest loop returned " + result);
                    sink = result;
                };
            } else throw new IllegalArgumentException("Unknown mode " + mode);
            int warmupIterations = args.length > 4 ? Integer.parseInt(args[4]) : -1;
            int warmups = 0;
            if (warmupIterations < 0) {
                long warmupEnd = System.nanoTime() + 1000000000L;
                do { operation.run(); warmups++; } while (System.nanoTime() < warmupEnd);
            } else {
                for (; warmups < warmupIterations; warmups++) operation.run();
            }
            ThreadMXBean bean = (ThreadMXBean) ManagementFactory.getThreadMXBean();
            bean.setThreadAllocatedMemoryEnabled(true);
            long thread = Thread.currentThread().getId();
            long allocated = bean.getThreadAllocatedBytes(thread);
            long start = System.nanoTime();
            for (int i = 0; i < iterations; i++) operation.run();
            long elapsed = System.nanoTime() - start;
            allocated = bean.getThreadAllocatedBytes(thread) - allocated;
            System.out.printf(Locale.ROOT,
                    "RESULT mode=%s arm64=%s count=%d iterations=%d warmups=%d ns_per_op=%.3f bytes_per_op=%.3f checksum=%d%n",
                    mode, arm64, count, iterations, warmups, (double) elapsed / iterations, (double) allocated / iterations, sink);
        }
    }
}
