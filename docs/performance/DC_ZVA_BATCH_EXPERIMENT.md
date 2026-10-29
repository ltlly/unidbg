# DC ZVA non-executable RAM batching: experimental candidate

Status: implementation and bounded correctness checks completed; fixed performance screen not yet completed. Not accepted for production and not an overall 20x claim.

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
