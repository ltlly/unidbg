# Terminal dispatcher leaf batching: not accepted

Source-only held experiment atop the RAM identity correction. No bundled native change, no formal acceptance, no global 20x claim.

At radix level zero, step is one and every remaining covered entry is a leaf. This patch fills min(remaining pages, entries to node end) identical PhysPageEntry structures, then advances index and remaining count once. Upper recursion, section ownership, topology, compression and node allocation remain unchanged. Struct assignment avoids hard-coded bitfield layout. Static review verified bounds, zero count, unsigned aggregate counter equivalence and nonaliasing of the internal index/count locals.

Baseline native SHA256 a87c36222f1e44490217a0fcc0cd04f52fb2144255c66dee595b7a7805074c6b.
Candidate native SHA256 4646581eb10cb9009eee331630a716f4f37290026be4dffa55264711f365f443.

Correctness: extracted actual-source radix model passed 3.2 million lookups both normally and under ASan/UBSan, covering page bits 10/12/14/16, holes, boundaries, full 64-bit coverage and pre/post compression. ARM/ARM64 native unit suites, memory-hook/register callback checks, RAM identity and lifecycle tests, and eight golden-output JVM smokes passed. Initial private harness compilation omitted internal include paths; corrected using the engine's generated compiler flags before executing those checks. These were harness build errors, not passing product tests. No new JUnit run is claimed for this experiment.

Fixed screening: 64 cold and 64 long fresh JVMs, eight workload families, two ABBA/BAAB blocks. All 128 rows, order, native hashes, golden exits, CPU affinity and unchanged runtime/fixture manifest audited. Ratios below are baseline/candidate geometric means of the two block ratios, not confidence bounds. Cold-process equal-family geometric mean 1.0000x; long-process 1.0043x.

Long work: Map2 1.0298x (both blocks positive); Map100 0.9907x; SG4K 0.9907x; SG64K 1.0048x; TT 1.0055x; JNA64 1.0587x (blocks 0.9824/1.1408, inconsistent); Telegram32 0.9908x; Telegram64 0.9832x (both blocks adverse). Telegram64 process 0.9450x. RSS ratios mostly near one, with Telegram64 about 4.1% lower candidate RSS; lower memory does not excuse the slowdown.

Cold-process Map100 0.9527x, SG64K 0.9798x, TT 0.9164x, each adverse in both blocks. TT initialization ratio 0.8839x. These regressions are retained rather than averaged away or rerun until favorable.

Decision: HOLD. The small Map2 gain does not establish a useful general improvement with acceptable startup and long-work behavior. Keep validated branch and native library unchanged. This branch contains only source/report, not new test/sample code, raw logs or binaries. Do not multiply these ratios by prior experiment ratios.
