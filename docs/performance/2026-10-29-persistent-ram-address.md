# Persistent RAM-address ordering: experimental source candidate

Status: focused correctness checks passed; fixed performance screen not yet complete. This is source-only experimental work, not accepted for master or upstream PR #792. Overall 20x is not achieved.

## Different mechanism from prior held temporary indexes

Previous experiments rebuilt and sorted temporary RAMBlock-pointer arrays on each allocation, sometimes using a size threshold. This candidate retains a second address-sorted linked list across successful block creation and actual destruction. It does not replace the original size-sorted primary list, its traversal order or its tie-breaking policy.

A serial counting diagnostic on the published allocator observed 103,000 warm map100 allocations, 7,416,000 outer iterations and 625,004,000 inner comparisons, with all 103,000 backing requests served by the existing host cache. Map2 observed 250,000 allocations and 132,750,000 inner comparisons. These identify repeated scanning, not saved time; the shared safety baseline below changes some scans, so these counts must not be treated as its exact cost or as performance evidence.

## Matching safety baseline

Both arms contain published patches 0001–0010, the existing checked RAM-identity correction, and the existing snapshot-root/COW/restore-preflight repair. The latter changes uc.c and qemu/softmmu/memory.c; it does not eliminate the saved RAMList freed/last_block fields. Neither arm contains the held PUSH, no-op notdirty, inline-dispatch or temporary-index experiments.

Fresh baseline native SHA256: 0f77f41c82b2631a811557315665542dea6e78eb65963c8d42a9eb92c910cc48.
Candidate: 076f27a44745364026f2d8f61874fc35576e9a95f729272b83d125a4349299bc.
Java production checkout is the upstream-synchronized bf76a556 tree, with actual compiled artifacts separately pinned.

An earlier identity-only A/B encountered the same UC_ERR_MAP on the second valid same-context COW/map/unmap/restore/remap cycle. That failure is retained; adding the common snapshot-root repair made this reproducer pass on both arms. It was not fixed by the address index. The final performance comparison isolates the index only on this common repaired baseline. Even a favorable result would still require integrated comparison against current master before acceptance.

## Implementation

RAMBlock adds two address-list pointers and one per-query gap scratch field. RAMList adds address head/tail. Publication occurs only after host allocation succeeds and primary-list insertion; actual qemu_ram_free removes the address node before reclaim. Guest moveout does not remove a still-live backing block. Context restore does not roll the address list back.

The original non-freed path retains the checked baseline scan. On the post-free path, a complete geometric pass validates positive lengths, links, ordering/non-overlap and checked ends. Monotonic aligned ends permit a forward successor cursor to compute each block's gap without nested scans. Invalid alignment results clear scratch to zero. A final traversal in original primary-list order chooses the first smallest fitting gap, preserving ties and the original omission of space before the first block. Overlap, overflow or invalid geometry falls back to the safety baseline scan; fallback is not a repair for broken membership. Correctness relies on centralized add/free maintaining the exact same live members in both lists.

No separate index allocation is added, but memory is not free: on the measured 64-bit build RAMBlock grows 72→96 bytes, RAMList 32→48, and uc_struct 18,016→18,032. Host glibc usable allocation for a RAMBlock grows 72→104 bytes. Extra maintenance and memory are included in the cross-workload/RSS gates; no universal non-increasing-memory claim is made.

## Focused validation

- Actual extracted safety-baseline and candidate allocator models matched 50,000 cases per 32/64-bit width, including mixed order, ties, alignment crossing, overlap and overflow fallback. Each width also completed 100,000 add/free transitions with exact member-set/link checks.
- The 64-bit model passed ASan+UBSan with leak detection disabled; this is not a LeakSanitizer pass or a sanitizer run of the complete JNI engine.
- Native ARM32/ARM64 random mapping lifecycles (3,000 transitions each), host aliases and simple memory snapshots passed. Candidate checks explicitly verify primary/address member equality and links.
- Final common-safety A/B each passed ARM32/ARM64 50-cycle COW/map/unmap/same-context-restore/small-allocation tests, retaining a saved non-freed tail. Candidate checks members after each step. This does not certify every context history, especially restoring a newer context after discarding its versions.
- Static archive fault injection passed on both architectures/arms: save/restore calloc failure and retry, restore reserve realloc failure and conflict preflight, and host backing allocation failure leaving membership unchanged followed by successful retry. These cover specified failure points, not global OOM or leak freedom.
- Eight representative Java smokes per arm passed with actual loaded-native hash witnesses. These are not full Maven/CI passes.

Private test code, raw logs, counters and new binary artifacts remain outside Git as requested. This source commit includes the safety prerequisites and index patch, not a replacement production library.

## Fixed performance gate

Four ABBA/BAAB/ABBA/BAAB blocks, 128 cold plus 128 long independent JVMs across eight families. The geometric long-work ratio across map2/map100 must be at least 1.05, with favorable target-suite direction in every block. Every family's cold process time and long process/work time must stay within +3%, and peak RSS within +2%, using both arithmetic and geometric summaries. Actual native hash, classpath artifacts, input fixtures, JVM heap, order and CPU affinity are pinned. No extra data-dependent repeats, discarded adverse blocks or pooling with diagnostics.
