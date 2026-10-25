# Bounded interior dispatcher scans: held experiment

Status: NOT ACCEPTED after one fixed pilot and exactly one independent formal validation. Source only; bundled native and validated branch unchanged. No general 20x claim.

## Implementation
Retain the original compression algorithm and lookup layout. Add a separate per-node pair of uint16_t bounds containing every initialized non-NIL child. Interior nodes begin [512,0), terminal nodes [0,512). Each nonterminal setter invocation widens its node's span once after visiting entries. Compaction reads that node's bounds before rewriting incoming edges, skips only guaranteed-NIL prefixes/suffixes, and retains all internal NIL checks, recursion, singleton handling and skip limits. Both dispatcher destruction paths release the side array. This costs four bytes per allocated 2048-byte node, an additional pointer/allocation, and update work; it is not a free optimization.

Baseline SHA256 a87c36222f1e44490217a0fcc0cd04f52fb2144255c66dee595b7a7805074c6b.
Candidate SHA256 15f0a4f2a58d8f5e691253475b691c0609a9894076cdcce539817ccdf8b68528.

## Correctness evidence and limitations
Read-only source review found no blocker. Actual-source baseline and candidate models each passed 3.2 million lookup comparisons over page bits 10/12/14/16, holes, boundaries and full address-space coverage. Candidate ASan/UBSan also passed. Repeated compaction preserved lookups. The accumulated FNV tree digest matched (52a5532fade49307); this is digest evidence, not a byte-for-byte tree comparison.

ARM/ARM64 native suites, memory hook/register callback checks, RAM identity/lifecycle checks, 500 subpage overlay/removal cycles per architecture and eight golden-output JVM smokes passed. A free-wrapper test checked 100 map-rebuild, snapshot-restore and close cycles per architecture. Initial raw address counting observed two frees during restore because an address could be reused; the corrected test quarantines the watched allocation until stage end and verifies exactly one release without address reuse. Initial harness retained privately. No fresh JUnit run is claimed.

## Fixed screening and independent validation
Pilot: 64 cold plus 64 long fresh JVMs, two ABBA/BAAB blocks, all eight families. Map2/Map100 long work 1.1625x/1.0499x, but cold SG4K/TG64 and long TT adverse, TG64 RSS higher. These observations remain separate; not pooled with formal results.

Formal: 128 cold plus 128 long fresh JVMs, four ABBA/BAAB blocks, unchanged native/classes/fixtures/workloads. All 256 records, order, outputs, native witnesses, affinity and 1395 runtime pins audited. Ratios below are baseline/candidate block geometric means. Four-cluster bootstrap intervals are descriptive, not proof of broad equivalence.

Long work:
- Map2 1.1624x, interval [1.1217,1.2046].
- Map100 1.0346x, [1.0182,1.0628].
- SG4K 0.9782x, [0.9679,0.9901].
- SG64K 0.9838x, [0.9703,0.9960].
- TT 1.0159x, [0.9966,1.0289].
- JNA64 1.0199x, [0.9977,1.0425].
- Telegram32 1.0085x, [0.9722,1.0398].
- Telegram64 1.0491x, [1.0275,1.0647].

Equal-family long process ratio 1.0306x and cold process 1.0022x do not override individual regressions. SG4K initialization mean 355.17 to 377.76 ms, ratio 0.9421x [0.8843,0.9987]. Telegram32 peak RSS rose from 108.84 to 118.52 MiB, ratio 0.9185 [0.8720,0.9600]. Map100 long RSS 123.69 to 124.23 MiB; SG64K 150.71 to 152.49 MiB. Tiny metadata size does not justify assuming negligible measured process memory impact.

Decision: HOLD, no additional repeats solely for acceptance and no per-workload tuning. The mapping improvement reproduced, but SG regressions and Telegram32 memory growth fail the requested general performance/memory objective. Keep tests, new sample code, raw logs and binaries out of Git. Do not multiply incremental ratios or present these as original-upstream 20x progress.
