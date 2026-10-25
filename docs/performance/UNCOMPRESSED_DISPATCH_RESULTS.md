# Uncompressed dispatcher representation: HELD

Independent source experiment based on identity-corrected f77f9a2e. The comparison
baseline is the private identity-corrected native, not the currently bundled
validated-branch binary. No sorted-index or dispatcher-pool experiment is included.
No precompiled native is shipped here. Overall 20x remains unmet.

The dispatcher construction already yields a complete queryable radix tree.
This experiment omits optional post-build edge compaction, retaining the original
section IDs, holes, bounds checks, subpage handling, permissions, listeners, TLB
flushing and SMC invalidation. It introduces no pool/state or lazy mutation during
reads. The tradeoff is fewer whole-tree scans versus more lookup pointer loads.
Compaction never freed node/section capacities, so this does not reduce their
requested allocation; neither source fact predicts measured RSS.

Baseline: a87c36222f1e44490217a0fcc0cd04f52fb2144255c66dee595b7a7805074c6b.
Candidate: 3f0ebf1de40ef114afcb7046b0912aa9dda849545f542cb426aae7d052f8e6b9.

Correctness checks: 3.2 million extracted-source lookups before/after compaction,
page bits 10/12/14/16, holes, high/end addresses and full 64-bit coverage; ASan/
UBSan with leak detection disabled; ARM32/64 units, callback/store checks,
identity and 3,000-operation lifecycle tests, high physical addresses and virtual
aliases, section reuse, split unmap, snapshots, 500 partial-page MMIO cycles per
architecture, eight real sample smokes and fourteen Java tests with native hash
witnesses. These passed. The previously disclosed LSan environment limit remains.

## Independent full validation

A prior 64-cold/64-long pilot was promising in Map workloads but mixed elsewhere.
Exactly one independent four-block ABBA/BAAB validation followed: 128 cold plus
128 long fresh JVMs. All 256 records, order, affinity, native witnesses, expected
outputs and 1,395 runtime file hashes were audited. Pilot and formal data were
not pooled; no further repeat-until-favorable confirmation is planned.

Ratios are baseline/candidate, using geometric means of block-mean ratios.
Above one is faster for time or lower candidate RSS for memory. Four clusters
provide descriptive evidence, not general equivalence or cross-platform proof.

| Case | Cold process | Long work | Long process | Long RSS |
|---|---:|---:|---:|---:|
| map2 | 1.0177 | 1.2219 | 1.2080 | 0.9934 |
| map100 | 0.9846 | 1.1454 | 1.1258 | 0.9992 |
| sg4k | 1.0034 | 0.9995 | 0.9925 | 0.9964 |
| sg64k | 0.9870 | 0.9966 | 0.9978 | 0.9986 |
| tt | 0.9211 | 0.9802 | 0.9840 | 1.0042 |
| jna64 | 0.9924 | 0.9878 | 0.9896 | 0.9973 |
| tg32 | 1.0250 | 0.9894 | 0.9895 | 1.0056 |
| tg64 | 0.9995 | 0.9838 | 0.9789 | 0.9580 |

Map2 work improved 1.2219x (descriptive block-bootstrap 95% interval 1.2016–1.2565)
and Map100 1.1454x (1.1231–1.1671). Equal-case process gain was only 1.0304x.
Adverse evidence: TT cold initialization 309.6→340.1ms and process ratio 0.9211
(interval 0.8665–0.9791); Telegram32 long process 0.9895 (0.9762–0.9993),
Telegram64 0.9789 (0.9653–0.9937), with Telegram64 mean peak RSS 115.14→119.94MiB.
Other unfavorable values remain in the table. Improvements do not override these
costs. Candidate HELD; no change to perf/validated-optimization and no overall
20x claim. Source-only publication preserves the experiment, not endorsement.

New test code, samples and raw logs remain outside Git. Only Linux x86_64 with
Unicorn2 was runtime-tested; no successful CI run is claimed here.
