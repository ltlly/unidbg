# TLB padding experiment: not accepted

## Decision

The tested RAM-region pointer cache is **not included** in the production patch
series or bundled library. Production code remains that of
`9b8775ebbff6d849ade96ecbd365d726f40fb62a`. This report does not claim a new speedup,
a memory leak, a semantic defect, or achievement of the overall 20x goal.

The candidate reused existing 64-bit-host TLB-entry padding for a direct
MemoryRegion pointer under the existing full-page RAM/global-dispatch proof.
It retained raw-tag, permission, hook and snapshot guards, the old index fallback,
and complete dirty/SMC handling. Linux x86_64 ARM32/AArch64 entry sizes remained
64 bytes (TLB) and 16 bytes (IOTLB). Unchanged structure and library sizes do not
establish unchanged process RSS.

- Baseline native SHA-256: `27fcc8f9c7c6bf3708fd5fc4943941d008ffa493bde49283dff80e9689d32dfa`.
- Candidate native SHA-256: `8bc3cd39c3742d39cadec397f34a58953328393fdb469552d93a50325b7629e8`.
- Both libraries: 4,307,560 bytes.

## Recovery and validation boundary

A workspace recovery lost the earlier private candidate artifacts and raw timing
records before publication. The source was reconstructed, and rebuilding produced
the identical candidate SHA above. Earlier timing summaries were **not reused or
pooled**. All measurements below are new, post-recovery observations.

Fresh six-module Maven compilation, 16 Java tests, ARM32/ARM64 native units,
callbacks, randomized stores, protection/recovery, SMC, MMIO, snapshots and exit
checks passed. New pointer-identity and forced-NULL fallback tests covered high
physical addresses, aliases, collisions/victim reuse, topology replacement,
permissions/hooks, flush, split unmap and memory snapshots. The AArch64 wide
variant performed 8-byte stores; its section test uses zero high words, while
broader randomized store checks provide separate value coverage. Layout checks
are limited to the tested Linux x86_64 builds.

The earlier additional held-out, page-size and CPU-context test artifacts were
not recovered and are not claimed rerun here. Two incomplete cold smoke runs
failed harness phase assertions, not JVM execution: map fixtures have no close
phase, and the security fixture has app_init instead of close. Assertions were
corrected against source before the final 32 successful smoke runs and all formal
timing. Smoke and incomplete runs are excluded from the estimates.

## Measurement protocol

Eight unchanged workloads, full frozen production runtimes, identical JDK8,
128 MiB heap, explicit official INFO logging, shared paths and harness. Each JVM
is preceded by a 1,146-file runtime-manifest check; only the native library differs.
Eleven class identity/hash witnesses, mapped native SHA and before/after logging
hash/level checks are recorded. Native hashing streams through an 8 KiB buffer.
The 18 shared dependency jars and protocol/harness hashes were rechecked after
measurements; the JDK is shared, not rehashed per invocation.

Every capture succeeded, with a 120-second and 8-MiB failure bound. No concurrent
build, benchmark or profiler ran during timing. Small read-only analysis and
checkpoint work was allowed; filesystem caches were not flushed.

- Formal: seven alternating pairs per eight cases, 112 cold and 112 long JVMs.
- Independent cold confirmation: fourteen balanced pairs each for security 4K,
  Telegram32 and JNA, 84 JVMs, selected for the formal adverse signals.
- Independent long confirmation: fourteen balanced pairs for **all eight cases**,
  224 JVMs, fixed in advance after the formal audit. No additional repeat-until-pass.

These three stages keep cold and long datasets separate. Ratios below are baseline/candidate;
values above 1 favor the candidate. Intervals are exploratory, pointwise 95%
percentile bootstrap with 20,000 paired repeat-block samples, seed 20261024.
Suite summaries weight cases equally. Fixed order and temporal dependence limit
inference; these are not randomized production trials or formal equivalence tests.
Long work uses the anchored RESULT field; cold work is the measured warm phase,
not generic startup ns. Whole-process metrics include JVM setup and witnesses.

## Formal long result

| Metric | Equal-case B/C | 95% interval |
|---|---:|---:|
| Work time | 1.05953 | 0.97447–1.16242 |
| Process wall | 1.05291 | 0.97563–1.14346 |
| Process CPU | 1.03022 | 0.96267–1.10389 |
| Peak RSS | 1.01188 | 1.00628–1.01683 |

Work AB/BA ratios were 1.01754/1.11822. The positive average is not evidence of a
stable aggregate gain when the interval includes no improvement. Map2 and
Telegram32 leaned slower, with wide intervals. Apparent RSS improvement depended
strongly on Telegram64 and did not establish a general reduction.

## Independent full long confirmation

| Metric | Equal-case B/C | 95% interval |
|---|---:|---:|
| Work time | 1.04178 | 0.99321–1.09429 |
| Process wall | 1.02791 | 0.98629–1.07290 |
| Process CPU | 1.02467 | 0.98664–1.06382 |
| Peak RSS | 0.98727 | 0.97536–0.99850 |

Balanced 7AB/7BA bootstrap leaves the conclusion unchanged. Every individual
case's work-time interval includes no change. Peak RSS is approximately 1.29%
higher geometrically in this confirmation, reversing the formal mean direction.
RSS is adverse in both order splits (B/C 0.99548 and 0.97912).

Telegram32 paired RSS increases average 5.315 MiB [0.098, 10.705], with 10/14
increases. Telegram64 increases average 6.515 MiB [0.468, 13.044], while the paired
median is -1.309 MiB: tails materially affect its mean. TT and JNA mean changes
are +1.258 and +1.811 MiB but their intervals cross zero. Formal security-fixture
allocation increases were not independently confirmed. No allocation result is
used as a substitute for process RSS.

## Independent cold confirmation

Telegram32 process CPU rises by mean 0.247 seconds [0.031, 0.498], and its load
phase by 40.6 ms [4.3, 85.4]. JNA process CPU rises by mean 0.212 seconds
[0.063, 0.360]. Balanced-order sensitivity retains these adverse intervals.
Whole-process wall changes remain uncertain. The formal JNA RSS increase is not
independently confirmed; security 4K speed, allocation and RSS remain uncertain.
These are adverse observations, not proof of a specific causal mechanism.

## Outliers and interpretation

All observations are retained. Examples include formal Map100 cold warm time
180.828 to 18.293 ms, formal SG64 long work 13.918 to 5.756 seconds, and independent
long Map2 work 26.036 to 49.503 seconds and SG64 5.808 to 14.217 seconds. Large
favorable and unfavorable RSS pairs also occur. No scheduling, GC or leakage
explanation is asserted without evidence, and no pair is removed for looking
unusual. Historical summaries are not used to cancel fresh adverse results.

The evidence is insufficient for a stable overall speed benefit and shows
adverse cold-start CPU and RSS observations. Accordingly, the candidate is held rather than
merged, and this fixed confirmation is not extended until it happens to pass.
Tests, samples, benchmark code and raw logs remain outside Git.
