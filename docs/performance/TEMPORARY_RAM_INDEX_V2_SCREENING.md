# Size-gated temporary RAM index V2: HELD

This experiment retains the original corrected scan below 64 live RAMBlocks;
larger sets use the temporary sorted index in a separate non-inlined function.
The 64-block crossover was informed by an independent generic map-count sweep,
not sample names. No persistent metadata was added. The scratch stack frame is
kept outside the small-set path. Current source remains experimental.

Baseline native: a87c36222f1e44490217a0fcc0cd04f52fb2144255c66dee595b7a7805074c6b.
V2 native: 2bdfe43eacb7ac082b065c3b8816a3967abee41df4b1bce8d87acae0214d43f9.
No precompiled native is published by this commit.

Correctness: 50,000 actual-source model cases per 32/64-bit width, including
threshold edges, overlap, overflow, allocation failure and selected-offset ties;
ASan/UBSan with leak detection disabled (the previously disclosed LSan environment
limit remains); ARM32/64 units, callback/store checks, 3,000 lifecycle operations
per architecture, aliases/snapshots, exact 192-live-map traces, eight sample smoke
JVMs and fourteen Java tests with native witnesses passed. Review found no
correctness blocker. Duplicated candidate arithmetic must stay synchronized.

Fixed screening used two ABBA/BAAB blocks, 64 cold and 64 long JVMs, frozen runtime
hashes and affinity, with complete observations retained. Ratios below are
baseline/candidate geometric means of per-block ratios. This is not a cumulative
comparison against original upstream and is not an overall 20x result.

| Case | Cold process | Long work | Long process | Long RSS |
|---|---:|---:|---:|---:|
| map2 | 0.9222 | 1.0321 | 1.0310 | 1.0047 |
| map100 | 1.0115 | 1.1410 | 1.1258 | 1.0060 |
| sg4k | 0.9347 | 0.9780 | 0.9761 | 1.0021 |
| sg64k | 1.0276 | 1.0454 | 1.0424 | 1.0098 |
| tt | 1.0228 | 0.9776 | 0.9873 | 0.9998 |
| jna64 | 0.9702 | 1.0114 | 1.0170 | 0.9978 |
| tg32 | 1.0366 | 0.9834 | 0.9853 | 1.0343 |
| tg64 | 1.0350 | 0.9751 | 0.9859 | 1.0137 |

Map100 work improved about 14%, but equal-case process improvement was only
about 1.8%. Map2 cold process was about 8% slower and SG4K cold about 7% slower;
both blocks were adverse in both cases. Other long-work results include adverse
SG4K, TT and Telegram observations. These are screening results, not broad
statistical equivalence. V2 remains HELD; no acceptance or validated-branch change.
No further crossover-threshold tuning is planned from these samples. Investigation
returns to mapping/rebuild call-chain costs. New tests/samples/raw logs remain
outside Git. The previous V1 report and all adverse evidence are preserved.
