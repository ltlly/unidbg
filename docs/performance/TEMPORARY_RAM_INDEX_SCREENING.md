# Temporary RAM-offset index screening: NOT ACCEPTED

This experimental source branch is based on the identity-corrected source at
f77f9a2e233ec1ab5f78942eaee66f7a8e8cc753. It is not the validated release and
contains no replacement precompiled native library. Overall 20x is unmet.

The temporary index sorts RAMBlock pointers by offset and binary-searches the
nearest following block, retaining original outer list order, gap selection,
alignment and overflow checks. It uses 128 stack pointers and a checked temporary
heap allocation for larger sets. Invalid/overlapping intervals or allocation
failure use the original scan. There is no persistent cache or snapshot state.

## Correctness checks

Actual extracted source models matched 50,000 cases for each 32/64-bit width,
including forced allocation failure, overlap, wrap, ties and index-allocation
accounting. ASan/UBSan passed with leak detection disabled; LeakSanitizer itself
could not run under the environment's ptrace restriction, so no LSan pass is
claimed. ARM32/ARM64 unit suites, exit/store cases, RAM identity checks, 3,000
lifecycle operations per architecture, shared host aliases and memory snapshots
passed. A 192-live-map trace with 3,000 replacements per architecture selected
identical offsets. Eight real sample smoke processes and 14 Java regressions
passed with actual native hash witnesses.

## Fixed screening, October 25, 2026

Baseline: identity-corrected native a87c36222f1e44490217a0fcc0cd04f52fb2144255c66dee595b7a7805074c6b.
Candidate: 6fc41543d1981ea5170bb1ba94914ff949cb4abd28f3d911112e2c8af0f68e32.
Two ABBA/BAAB suite blocks, eight families: 64 cold + 64 long JVMs. Fixed CPU
affinity, native witnesses and runtime file manifests audited. All observations
retained; the pilot does not establish broad equivalence or acceptance.

Ratios below are baseline/candidate; above one is faster for time and lower RSS
for memory. They are geometric means of block-level ratios, not cumulative gains.

| Case | Cold process | Long work | Long process | Long RSS |
|---|---:|---:|---:|---:|
| map2 | 1.0584 | 1.0081 | 1.0107 | 1.0030 |
| map100 | 0.9754 | 1.1176 | 1.1034 | 0.9936 |
| sg4k | 1.1426 | 0.9880 | 0.9907 | 0.9944 |
| sg64k | 0.9519 | 0.9965 | 0.9986 | 0.9881 |
| tt | 0.9362 | 0.9895 | 0.9884 | 0.9993 |
| jna64 | 0.9169 | 0.9936 | 1.0011 | 1.0093 |
| tg32 | 1.0037 | 1.0114 | 1.0112 | 0.9939 |
| tg64 | 1.0072 | 0.9882 | 0.9896 | 0.9930 |

Map100 improved about 12% in work time, but equal-case whole-process improvement
was only about 1%. JNA cold process was about 9% slower, with both blocks adverse;
SG4K long work was about 1% slower in both blocks. The candidate is HELD, not
accepted or applied to perf/validated-optimization. Source-only publication
preserves the experiment, not endorsement. Further investigation will measure
generic small-set index overhead and stack/code-layout costs, without workload
names or output-specific dispatch.

New tests, samples, raw logs and candidate binaries remain outside Git.
