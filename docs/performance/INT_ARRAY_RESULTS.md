# Bounded integer-array read optimization

## Change

`UnidbgPointer.getIntArray` reads up to 1,024 integers per backend call and decodes little-endian values directly. Scratch arrays are bounded to 4 KiB. No guest-memory cache or backend selection change is introduced. The existing JNI `SetIntArrayRegion` / `ReleaseIntArrayElements` pathways use this API.

## Measurements (not whole-emulator results)

Unicorn2, Linux x86_64, Temurin Java 8, fixed 128 MiB heap. Five independent JVM processes per variant per case, alternating baseline/candidate order (80 processes total), 2,000 warmup and 2,000 measured operations. Every operation checks its checksum. Baseline uses the exact original UnidbgPointer class prepended to the same classpath; candidate uses the changed class. Raw records and summarizer are committed.

| Integers | ARM32 speedup | ARM64 speedup |
| --- | ---: | ---: |
| 1 | 2.02x | 1.97x |
| 16 | 5.18x | 5.99x |
| 1,024 | 17.78x | 19.31x |
| 16,384 | 34.27x | 36.91x |

At 16,384 integers, measured Java allocation falls from 458,768 to 131,344 bytes/operation (~71.4% reduction). Median peak RSS falls from 86,544 to 85,784 KiB (ARM32) and 85,720 to 84,740 KiB (ARM64). Across these cases median RSS ratios are 0.948–0.996; this is not a universal memory guarantee. RSS includes JVM startup and is noisy. Warmup is limited and small-input numbers are more sensitive to JIT startup; they require longer steady-state confirmation.

## Correctness and limits

- 14 API/JNI/structure/protocol unit checks pass on both baseline and candidate.
- Two additional native tests (ARM32 and ARM64) pass on both: adjacent mappings, unaligned data, final mapped bytes, unmapped-read failure, writes observed between calls, and actual JNI array release/commit.
- Android32 upstream native demonstration exits guest code 0 in both variants.
- Android64 upstream demonstration reports guest stack corruption/guest exit 1 in BOTH baseline and candidate, despite host Java exit 0. This is an existing unresolved limitation, NOT a passing test or a demonstrated regression fix.
- Legacy Unicorn's native library cannot load in this environment; recorded, not silently replaced. Timing evidence uses Unicorn2 only. Demonstrations use their upstream Dynarmic selection.
- This establishes >20x on the large-array transfer workload only. It does NOT establish 20x across arbitrary native code, instruction loops, libraries, JNI workloads or complete applications. The broader target remains open.
