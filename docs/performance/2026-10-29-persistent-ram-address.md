# Persistent RAM-address ordering: experimental source candidate

Status: HOLD. The fixed screen terminated after a candidate-labelled long-work JVM exceeded the original 120-second timeout. The cause is unresolved; do not merge into master or upstream PR #792. Focused checks passed, but the complete performance/correctness acceptance is not established. Overall 20x is not achieved.

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

## Stopped screen and unresolved timeout

The 128 cold JVMs completed. In long block0/map2, the first A and B JVMs completed; the second B (position2) exceeded the original 120-second subprocess timeout. The wrapper killed that test child, returned an error and stopped the remaining plan. The failure record and stderr are retained. The failed process did not reach its final native witness or complete resource record; its fixed-prefix/argument selection identified the intended candidate. Do not call 128+3 records 131 passed runs or calculate a long-suite speedup from the two completed observations. No failed row was replaced and the screen was not resumed.

Source review did not find a definite cause. Valid address geometry makes the validated forward scan bounded, but a previously corrupted list or an unrelated runtime stall cannot be excluded without the failing stack. This is not grounds to dismiss the timeout as noise.

A separate fixed two-process CPU-observer diagnostic completed on both arms with matching native witnesses and did not reproduce the timeout. Profiled timings are not acceptance evidence and do not explain or erase the failure. The bounded eight-process unmodified-workload batch also completed without crossing 120 seconds, with matching observed mappings and final native witnesses. It therefore did not capture a failing stack. Its /proc PID/task/PID observations describe the process leader, not necessarily HotSpot's workload thread; the recorded zero-CPU/futex samples must not be used to claim workload blocking. Neither non-reproduction nor those diagnostic timings restore acceptance.

A separate full C-engine/JNI ASan+UBSan diagnostic build first stopped on a negative left shift in unchanged Thumb translation code. An explicitly documented UBSan-recovery run retained all UB reports, kept ASan memory-error termination enabled, and completed the original 50,000-iteration map2 workload with its expected result and actual native witness. It emitted no ASan memory-error report in that execution, but UBSan still reported shifts at translate.c:7820 and8221, so it is **not a clean UBSan pass**. JVM internals and generated TCG instructions were not compiler-instrumented; leak detection was disabled. This additional evidence does not explain the original uninstrumented timeout, prove universal safety, or change HOLD status.
