# No-effect store-dispatch experiment: not accepted

## Decision and scope

This candidate is not merged into the production patch series or bundled native
library. It is independent of the held TLB-padding experiment: it starts from
production code `9b8775ebbff6d849ade96ecbd365d726f40fb62a`, without that pointer cache.
The complete formal cold phase failed a predeclared load-time guardrail. The
remaining long phase was intentionally stopped; its partial observations are not
used to claim a speedup. The overall 20x goal is not achieved.

The proposal changes only the three existing guarded store helpers (AArch64 byte,
ARM32 little-endian word and AArch64 little-endian doubleword). When the already
validated RAM region lacks EXEC and the raw code tag is not the -1 sentinel,
notdirty_write currently has no mapping query, invalidation, hook scan or dirty-tag
update. The candidate omits only that no-effect call. All original guards,
other calls, generic fallbacks and post-call addend reads remain intact. It adds
no cache, per-engine field or allocator state. This predicate depends on the
current helper's complete side effects and is not a general license to skip SMC.

- Baseline SHA-256: `27fcc8f9c7c6bf3708fd5fc4943941d008ffa493bde49283dff80e9689d32dfa` (4,307,560 bytes).
- Candidate SHA-256: `0b4c9c4a82d65585f791f9d658e7eb79ff7947fd90accc8dae842b719b57654a` (4,306,856 bytes).
- Runtime/build scope: Linux x86_64 Unicorn2, ARM32/AArch64 guests, not other bundled platforms or default Unicorn1.

## Correctness checks

The source matched published patches plus the private proposal. After diagnostic
builds, restoration/relinking reproduced the production candidate byte-for-byte,
with diagnostic exports absent. Broad native regressions, 16 Java tests and 32
smoke JVMs passed with native/class witnesses.

A four-combination matrix independently varies physical MR EXEC and custom TLB
EXEC permissions. A guest load first creates a writable NOTDIRTY entry; then two
aligned stores check bytes, surrounding canaries and raw tags. Each version passes
12 cases across the three helpers, both with the exact production library and in
separate instrumented builds. Counters show the call is omitted only for non-EXEC
MR plus non-sentinel code tag; sentinel and executable cases retain dirty updates
and invalidation attempts. Executable aliases verify a previously cached function
is invalidated. The test deliberately does not recreate an alias TB between two
stores through an already-clean data entry, and does not claim to fix that separate
existing alias protocol. Diagnostics are not timing evidence and were not shipped.

## Pilot, kept separate

Four balanced pairs across eight workloads give 64 cold and 64 long JVMs. All
captures and witnesses pass. Long equal-case baseline/candidate ratios were:

| Metric | Ratio | Exploratory 95% interval |
|---|---:|---:|
| Work | 1.10984 | 1.02483–1.20338 |
| Process wall | 1.08160 | 1.01700–1.15030 |
| Process CPU | 1.05836 | 1.01250–1.10631 |
| RSS | 1.00362 | 0.99813–1.01337 |

These were screening signals, not accepted results. Work ratios differed strongly
between baseline-first and candidate-first pairs (1.20189 versus 1.02483).
Security 4K had an adverse work pair of +7.029 seconds, and other adverse allocation,
RSS and timing observations were retained. No pilot observation was pooled into
formal results.

## Predeclared formal design

Eight suite rounds were planned for each phase. Each case uses a four-process
ABBA or BAAB block (A=baseline, B=candidate), balanced four of each per case.
A seeded initial case order is cyclically rotated so each case occupies every
position once. Workloads and binaries remain unchanged. Block effects are mean
log baseline minus mean log candidate; 20,000 bootstrap samples resample complete
suite rounds, not individual processes. There are eight bootstrap units per case,
not 32 independent observations. This reduces linear time drift but does not eliminate
nonlinear drift or carryover. Intervals are pointwise and unadjusted.

The same frozen 1,146-file runtime differs only in native library. Eleven class
identities, native SHA and explicit before/after INFO-logging witnesses are checked.
The formal runner pins 35 protocol inputs, verifies them before each JVM, takes
an exclusive lock, and creates output files exclusively. Process output has a
120-second/8-MiB failure bound. Shared dependency contents are checked separately;
JDK and dependency jars are not rehashed per JVM. No concurrent benchmark, build
or profiler ran during measurement.

Long work/wall were primary speed endpoints; cold and RSS were separate adverse
constraints, never pooled with long. Before formal execution, the load guardrail
was fixed as candidate/baseline point >1.05, lower 95% bound >1, and mean absolute
increase at least 5 ms. Process and memory constraints were also recorded before
launch. Unfavorable observations cannot be removed or followed by repeat-until-pass.

## Complete cold result and stop

All 256 cold observations pass the protocol audit. JNA load triggers all three
predeclared conditions:

- Candidate/baseline geometric ratio: **1.180802 [1.033154, 1.355162]**.
- Arithmetic mean load: baseline 89.857 ms, candidate 120.649 ms, difference +30.792 ms.
- Median block delta: +11.541 ms; both sequence strata are adverse.
- Five of eight block arithmetic deltas are positive. Round 0's +173.390 ms is influential and retained.

Geometric ratios and arithmetic means are different summaries; the mean delta is
not calculated from the geometric ratio. JNA wall and CPU means also increase,
but their ratio intervals cross no change and do not trigger those exact guards.
Security 4K app_init is another adverse observation (+22.323 ms, candidate/baseline ratio 1.133581
[1.004896,1.320498]); it is disclosed rather than silently treated as load.

Cold aggregate RSS passes the numeric screen: candidate/baseline 0.997575
[0.995509,0.999895], with no individual numeric RSS trigger. This does not cancel
the load-time failure or establish universal memory equivalence.

After that complete cold failure, the remaining long run was stopped (exit 130).
Its partial output is preserved, not interpreted as a completed formal dataset.
No additional trial is added to obtain a passing result. The measured adverse
result is not a claim that its exact causal mechanism has been established.
Production code is unchanged; tests, fixtures, diagnostic code and raw logs stay
outside Git.
