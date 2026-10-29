# Bounded TCG free-temp search: completed screen (HOLD)

The source-only experimental candidate bounds free-temp bitmap lookup by nb_temps. Source checkpoint: 1473e2400df81c456c07c95a28a0dd114780232e. This is an incremental experiment atop the experimental snapshot-root correctness repair, not an upstream or production comparison. The baseline itself remains held from performance acceptance.

Baseline native SHA256: 6e952301e5165a6e013fbdb1815020b5f9564877a72d30a9830de1cfeb16b03e.
Candidate native SHA256: 3bc6ff07c69f2a9d0193698409fb904aeb24b4e5c9590eb40a819ad6c40b09b7.

## Correctness and mechanism

tcg_func_start clears all free bitmaps when resetting nb_temps; allocation only increases it; freeing sets only a previously allocated temp index. Thus the first free index is unchanged. The no-match sentinel and success predicate both change to nb_temps, preserving the original allocation path. No fields, caches or retained allocations are added.

Eight serial dual-search diagnostic JVMs selected identical normalized indices on every observed allocation; eight separate candidate smoke JVMs passed; 1,004,666 comparisons against the actual 64-bit-host bitmap header passed over bounds 0..512, all valid single bits, random sets and successive clears. This is not 32-bit-host execution or exhaustive emulator validation. Diagnostic counters are not shipped.

Observed diagnostic allocation counts range from 26,965 to 261,047 per process, with 6,942 to 59,914 no-free-item cases. ARM bounds remain <=64; AArch64 sometimes uses a second word. Bounds are not actual word-read counts because successful searches stop early.

The existing x86-64 compiler shrinks each ARM/AArch64 allocator function from 0x15b to 0x11d bytes, but replaces eight unrolled checks with a dynamic loop, adds bound work and register save/restore on reuse paths. Less possible scanning does not imply faster execution.

## Frozen screen

64 cold and 64 long fresh JVMs, eight workload families, fixed ABBA then BAAB blocks. All 128 row keys, order, exits, affinity [0,1], loaded-native witnesses and matching classpath passed. A 1400-file runtime/fixture manifest was hashed before and after each phase; unchanged. The independent review recomputed all ratios and checked manifest-record consistency, but did not separately rehash all runtime files. Fixed native resource slot; actual extracted paths are random and witnessed. No concurrent build, profiler or heavy archive ran during timing. No extra data-dependent repeats.

Telegram expected bytes use the previously verified frozen oracle outside timed JVMs. Other sample checks retain their limitations: SG completion is not an independent business oracle, and large-map signing uses determinism.

Time ratios are baseline/candidate (>1 faster); RSS ratios are candidate/baseline (>1 more memory). Two-block descriptive estimates are not confidence intervals or proof of causality/equivalence.

| Family | Long work | Long wall | Long RSS | Cold wall | Cold RSS |
|---|---:|---:|---:|---:|---:|
| map2 | 1.0090 | 1.0058 | 1.0061 | 1.0476 | 0.9908 |
| map100 | 0.9473 | 0.9495 | 1.0027 | 1.0057 | 1.0020 |
| sg4k | 1.0013 | 1.0077 | 1.0040 | 0.9361 | 0.9441 |
| sg64k | 1.0046 | 1.0021 | 0.9925 | 0.9848 | 1.0158 |
| tt | 0.9951 | 0.9979 | 1.0102 | 1.0325 | 1.0020 |
| jna64 | 1.0315 | 1.0280 | 1.0011 | 1.0706 | 1.0076 |
| tg32 | 0.9864 | 1.0194 | 1.0003 | 1.0251 | 0.9959 |
| tg64 | 0.9807 | 0.9801 | 0.9936 | 1.0923 | 0.9945 |

Equal-family cold wall: 1.023271x; long wall: 0.998545x; long work: 0.994224x. Long results are effectively neutral at this screen resolution, not a reliable gain.

Adverse observations retained: Map100 long work is 0.947347x (candidate time about +5.56%), blocks 0.971108/0.924168; SG4K cold wall is 0.936123x (about +6.82%), blocks 0.953636/0.918932. SG64K cold RSS is 1.01583, both blocks above baseline. Other favorable and unfavorable cases often change direction across blocks. The cold average improvement does not cancel these adverse cases.

**Decision: HOLD.** No production merge, no bundled-native replacement, no reliable general speedup and no 20x claim. Do not multiply these ratios by previous experiments. Tests, samples, executable binaries and raw logs remain excluded from Git.

## Evidence hashes
- cold64.jsonl: 8e37300bc980c06e0a5fed60014ecda38dbb9370f3b4be846f3c59d8d147e68c
- long64.jsonl: fcde7c94a61e54da9ea5ad71365ed0c8d1963f916612422737e6c3012140c658
- runtime-manifest.json: ae6fb9dc469e36b6d5c0ff152accb9270bf56b9cbeea6e6ef157b7ec8ceba00a
- formal-plan.json: cae5cdbf6bafc3e9803f6e5a34765f0d3bc8d0aa7629cc011a3dec42356502c4
- diagnostic.jsonl: ab7fb1c3b9bf0901a5cc5a3e54f925ff188765a9751a6863802d156e5b4ecc5a
- smoke.jsonl: 2413a8b2d4cf8f9447653c80a85e41b6cacd56e44c27d512e5038f66ce6f500e
- bitmap-check.log: d7a316e76eef303feec0c0923e5231fac5f1233c067c228f199662ff8e1faca3
