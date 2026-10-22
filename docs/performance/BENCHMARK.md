# Integer-array memory benchmark

`IntArrayBenchmark` measures the existing `UnidbgPointer.getIntArray` API on real Unicorn and Unicorn2 backends, with ARM32/ARM64 emulator setup. It reads deterministic little-endian data and validates every returned integer through a checksum on every operation. It is a native-memory transfer microbenchmark, **not** an emulator instruction-throughput or whole-application speed claim.

Build with tests enabled (`-Dmaven.test.skip=false`); tests are disabled by upstream's default POM. Use Java 8 for the unmodified baseline: its wildcard `Module` import conflicts with Java 9+ `java.lang.Module`.

Run the standalone main with arguments `count iterations arm64 unicorn2`. Use fixed `-Xms128m -Xmx128m`, independent JVM processes, at least five paired baseline/candidate runs and alternate order. Warmup is min(iterations, 2000) operations. Report every raw RESULT line and `/usr/bin/time -v` peak RSS; do not conflate thread-allocated bytes with retained heap or native RSS. Record exact classpath/build commit and host configuration. Test counts 1, 16, 1024 and 16384 to expose small-input regressions as well as large transfers.

Correctness unit tests separately cover unaligned offsets, random values, zero/invalid lengths, bounded pointers and chunk-boundary sizes. Native page-boundary/error tests must pass before accepting an optimized implementation.

## Guest-entry JNI and CPU-control workload

`NativeWorkloadBenchmark` executes the real emulated JNI `SetIntArrayRegion` entry (JNI table slot 211), including guest entry, SVC dispatch, backend memory reads, Java array update and checksum validation. It uses the existing JNI implementation and the same Unicorn2 backend for both variants. This is a synthetic complete JNI transaction, not an APK or representative user `.so` application. Its `compute` mode executes an ARM32/ARM64 decrement loop without integer-array transfer as a negative control.

Arguments: `jni|compute count iterations arm64 [warmupIterations]`. With no final argument, warm up for one second; with an explicit nonnegative final argument, perform exactly that many warmup operations. The fixed-time series is for warmed latency and does unequal total work; its peak RSS is NOT an equal-work comparison. The fixed-work series uses 1,000 warmup operations per process. Keep both series, including adverse memory results.

The runner supports `UNIDBG_BENCHMARK_MAIN=com.github.unidbg.performance.NativeWorkloadBenchmark`. Run each sample as a separate Python invocation so child RSS accounting contains only that JVM.

For a fresh checkout, build/install the reactor with Java 8 and tests compiled (`mvn -Dmaven.test.skip=false -DskipTests -Dmaven.javadoc.skip=true -Dgpg.skip=true install`), then use Maven `dependency:build-classpath` for `unidbg-android` to obtain the test dependency classpath. Prepend the reactor's API, Android and backend `target/classes` and `target/test-classes`. The recorded experiment used the same built reactor/dependencies for both variants, prepending only the baseline UnidbgPointer class; hashes are in results/class-hashes.json. For independent reproduction, build baseline commit 5a0029aa and candidate commit 855bed7e in separate worktrees with the same harness and dependencies.
