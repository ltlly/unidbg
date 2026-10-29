# DC ZVA non-executable RAM batching: experimental candidate

Status: fixed 256-process screen completed and independently audited. Performance gates failed: HOLD, not accepted for production.

## Structural evidence and scope

A fresh profile on native 6e952301e5165a6e013fbdb1815020b5f9564877a72d30a9830de1cfeb16b03e found DC ZVA in 360 of 2287 Telegram64 main-thread CPU samples (15.74%). This overlaps store-path samples and must not be added to them. Other seven families had no ZVA samples; the instruction implementation is generic, but the observed warm bottleneck is not shared across all families.

A separate serial diagnostic retained original behavior and classified the final byte loop. The long Telegram64 run executed 1,600,225 ZVAs, all meeting the narrow proposed predicate; JNA64 executed 189, all eligible; other six had none. Diagnostic times are not performance measurements.

## Candidate and safety argument

Keep both original TLB probe rounds, actual input-address byte writes, and the final fallback loop. Only coalesce the final loop when the whole block lies within one target page, no snapshot or WRITE hook exists, no stop/error is pending, the current write tag is exactly page|NOTDIRTY, raw code tag is non-sentinel, and a current cached full-page RAM section identifies writable, non-readonly, non-executable RAM.

For that combination the old per-byte notdirty_write performs neither SMC invalidation nor dirty-tag clearing. A single memset replaces repeated lookup/helper/byte stores, leaving NOTDIRTY intact. EXEC, MMIO, watchpoint, invalid/subpage tag, COW, cross-page and callback paths remain unchanged. The code is compiled only for AArch64 guests on 64-bit hosts and adds one explicitly suffixed C export; exports are not claimed identical.

Do not generalize this into persistent NOTDIRTY clearing. Hook addition does not re-arm all cached dirty tags, and tlb_set_dirty affects other MMU/victim entries. The candidate avoids both state changes. It does not add atomic-zeroing, concurrent-host-access or alias-SMC guarantees.

## Bounded correctness evidence

Baseline and candidate matched 128 aligned-block boundary cases, RW/RWX permissions, dynamically added write hooks (66 callbacks including original probes), protection faults and recovery, RW/RWX toggles and memory-snapshot restoration. Additional matching checks cover MMIO address/size/value sequence hashes, unmapped/protected-write recovery, write-hook stop and PC changes, an actually translated executable target invalidated by zeroing, shared host backing, nonidentity physical translation, repeated colliding TLB addresses and unmap/remap. Eight representative Java smoke JVMs passed with loaded-library hashes.

A test-only symbol interposer confirmed the optimized branch was exercised: basic fixture 68 bulk hits/74 fallbacks; edge fixture 131 hits/4 fallbacks. Interposition is excluded from timing and from this patch.

Important limitation: an initial nested-emulation-from-memory-write-hook fixture segfaulted in both baseline and candidate. Its original source and failures were retained, and the remaining edge cases were then run separately without that case. This is not a passing nested-emulation test, nor proof of the crash cause or a candidate-specific regression. No 32-bit host execution or all-platform certification is claimed.

## Prespecified performance screen

Baseline native: 6e952301e5165a6e013fbdb1815020b5f9564877a72d30a9830de1cfeb16b03e.
Candidate native: 2ea8cdabb2b757998162d451683ed4b333fc98b12cfbf82d1f72e5324f86dd26.

This is incremental to the experimental snapshot-root repair, whose own performance status remains HOLD. It includes neither bounded-temp nor indexed-service candidates.

Four fixed blocks ABBA/BAAB/ABBA/BAAB, eight families, 128 cold plus 128 long fresh JVMs. Target Telegram64 long-work ratio >=1.05 with favorable direction in all four blocks; other-family long/cold mean time growth <=3%; each family's mean peak-RSS growth <=2%. Violations or unresolved correctness risks hold acceptance. These descriptive gates are not broad equivalence proof. No extra data-dependent repeats, concurrent builds/profiling/heavy archives, or multiplication with previous increments. Runtime/fixture manifests and actual native hashes are checked; fixed classpath/native slot, randomized actual extraction paths, affinity [0,1].

Tests, samples, binaries and raw logs are excluded from Git. The main validated branch and bundled native are unchanged. Results, including adverse outcomes, will be reported separately.

## Completed fixed screen: HOLD

All 256 rows completed: exact four-block orders, unique row keys, exit codes, affinity [0,1], loaded-native witnesses and unchanged 1400-file runtime/fixture manifest checks passed. Independent review recomputed all ratios; it verified manifest-record consistency rather than rehashing every runtime file. No build/profiler/heavy archive ran during timing; light read-only monitoring and Git metadata checks occurred.

Time ratios are baseline/candidate (>1 faster); RSS ratios are candidate/baseline (>1 more memory).

| Family | Long work | Long wall | Long RSS | Cold wall | Cold RSS |
|---|---:|---:|---:|---:|---:|
| map2 | 0.7411 | 0.7496 | 1.0008 | 0.9867 | 0.9907 |
| map100 | 0.9560 | 0.9687 | 0.9988 | 1.0934 | 1.0144 |
| sg4k | 1.0563 | 1.0520 | 0.9957 | 1.0585 | 0.9777 |
| sg64k | 0.8517 | 0.7588 | 0.9871 | 1.1305 | 0.9758 |
| tt | 0.9899 | 0.9831 | 0.9825 | 0.9853 | 0.9961 |
| jna64 | 1.3445 | 1.3696 | 1.0041 | 0.9296 | 1.0033 |
| tg32 | 1.0930 | 1.0901 | 1.0069 | 1.0029 | 1.0196 |
| tg64 | 0.9120 | 0.8865 | 0.9918 | 0.9834 | 0.9947 |

Equal-family cold wall 1.019405x; long wall 0.965362x; long work 0.979245x.

Predeclared target fails: Telegram64 long work is 0.911984x, below 1.05x, with block ratios 0.553541 / 1.121310 / 0.856192 / 1.301673, not consistently favorable. Non-target time guardrails also fail: Map2 long work 0.741137x (observed candidate time +34.93%), SG64K 0.851660x (+17.42%), Map100 0.955955x (+4.61%), and JNA64 cold wall 0.929617x (+7.57%). Mean RSS growth remains <=2% in every family, but that does not offset the timing failures. Both arithmetic and geometric mean checks reject acceptance, so the wording of mean does not affect the decision.

Block variability is very large, including cases outside the changed AArch64 instruction path. These observations cannot be attributed automatically to the patch. Absolute times also differ materially from earlier independent experiments; those runs are not pooled or used as controls. Mechanism evidence and bounded correctness do not establish an application speedup.

**HOLD: do not merge this candidate into master or replace the bundled native.** Keep the adverse results, do not extend the run to seek a favorable outcome, and do not claim overall 20x. Further research must distinguish measurement variability from actual path costs rather than accepting on operation counts alone.

## Screen evidence hashes
- cold128.jsonl: 1132a366c3bdb3f4964b55d9dadfbb0c7175bb44c3924674230922c10950f21f
- long128.jsonl: d186142f44ba419dc5b178bd982dcf207f1ee8cf8b0852e491cdc6adbd0b8b69
- formal-plan.json: c2aabc4225361c0a1b4278f5a5c23c518a25306141a1b1740fac69959dc12890
- runtime-manifest.json: ebd04b82eceea95f4864a9a6bcb5e300536626439ced6fb5c82d71f274f61b34
- analysis.json: d2bd32c2bb55e7008c7f83c3380e97d37ec41002a0bc62053fe83538787dd5ce
- diagnostic-long.jsonl: 3cc37cf63bd354849050eec3ab35c629a9b04e3e31a9d8c95d720e647ee41464
- branch-witness.log: 965adf21a0c4263e88b268a05595b224b7412e0673766fdc4908ede37fba0a75
- edges-branch-witness.log: bbf0b12ab5126226757ad2ca356aedb61dddb13880d1c1385de9b0d14add14a3

