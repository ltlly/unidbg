# Released dispatcher buffer pool: HELD

Independent experiment based on identity-corrected source f77f9a2e. This does
not include either temporary sorted-index candidate. No precompiled native is
shipped here, and perf/validated-optimization is unchanged. Overall 20x unmet.

One per-engine slot retains only a fully destroyed dispatcher's empty object and
section/node buffer capacities, with a 64 KiB requested-storage cap. Actual
subpage destruction, guest unmapping, topology rendering/rebuild, TLB flushing,
callbacks and SMC invalidation remain unchanged. Admission requires initialized
engine, no snapshot and no close; taking is also disabled during snapshot/close.
The pool pointer is appended to private engine state; release_common explicitly
drains it after address-space destruction. Allocator overhead is not included
in the 64 KiB requested-byte bound.

Native baseline: a87c36222f1e44490217a0fcc0cd04f52fb2144255c66dee595b7a7805074c6b.
Candidate: 145be686f616e42591d43b6a6a44e063fb864e9514fe1a6ed296cf347da13e1b.

Correctness: native ARM32/64 units, exit/store checks, identity/lifecycle/alias/
snapshot tests, 8 sample smoke processes and 14 Java tests with native witnesses
passed. Static-link free interception checked 100 close cycles per architecture:
the cached object and both non-null backing arrays were freed exactly once.
It also confirmed 2,000 buffer reuses per architecture, snapshot gates, zeroing
and genuine unmapped faults. A further 500 partial-page MMIO overlay/removal
cycles per architecture passed. Source review found no additional blocker.

Fixed two-block ABBA/BAAB screening ran 64 cold plus 64 long JVMs. All 128 records,
runtime hashes, native witnesses, outputs, order and affinity audited. Ratios
below are baseline/candidate, using geometric means of block-level ratios.

| Case | Cold process | Long work | Long process | Long RSS |
|---|---:|---:|---:|---:|
| map2 | 1.0002 | 1.0224 | 1.0213 | 0.9973 |
| map100 | 1.0307 | 0.9742 | 0.9721 | 1.0018 |
| sg4k | 0.9671 | 1.0569 | 1.0534 | 0.9989 |
| sg64k | 0.9577 | 1.0033 | 0.9996 | 1.0331 |
| tt | 0.9551 | 0.9900 | 0.9883 | 0.9992 |
| jna64 | 1.0294 | 0.9911 | 0.9925 | 1.0084 |
| tg32 | 0.9947 | 1.0206 | 1.0334 | 0.9598 |
| tg64 | 1.0495 | 1.0411 | 1.0323 | 1.0471 |

Equal-case process gain was only about 1%. Map100 long work regressed about 3%
in both blocks; Telegram32 peak RSS increased about 4%. Cold TT was adverse in
both blocks, and other adverse results remain in the table. Correctness and
bounded requested pool bytes are not proof of speed or peak-RSS acceptance.
Candidate HELD; source publication preserves investigation, not endorsement.

Follow-up symbol-resolved CPU diagnosis points to dispatch tree insertion and
compaction, not merely allocation churn. New tests, samples and raw logs remain
outside Git. No successful CI run is claimed by this report.
