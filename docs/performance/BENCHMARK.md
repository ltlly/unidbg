# Integer-array memory benchmark

`IntArrayBenchmark` measures the existing `UnidbgPointer.getIntArray` API on real Unicorn and Unicorn2 backends, with ARM32/ARM64 emulator setup. It reads deterministic little-endian data and validates every returned integer through a checksum on every operation. It is a native-memory transfer microbenchmark, **not** an emulator instruction-throughput or whole-application speed claim.

Build with tests enabled (`-Dmaven.test.skip=false`); tests are disabled by upstream's default POM. Use Java 8 for the unmodified baseline: its wildcard `Module` import conflicts with Java 9+ `java.lang.Module`.

Run the standalone main with arguments `count iterations arm64 unicorn2`. Use fixed `-Xms128m -Xmx128m`, independent JVM processes, at least five paired baseline/candidate runs and alternate order. Warmup is min(iterations, 2000) operations. Report every raw RESULT line and `/usr/bin/time -v` peak RSS; do not conflate thread-allocated bytes with retained heap or native RSS. Record exact classpath/build commit and host configuration. Test counts 1, 16, 1024 and 16384 to expose small-input regressions as well as large transfers.

Correctness unit tests separately cover unaligned offsets, random values, zero/invalid lengths, bounded pointers and chunk-boundary sizes. Native page-boundary/error tests must pass before accepting an optimized implementation.
