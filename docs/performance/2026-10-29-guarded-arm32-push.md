# Guarded ARM32 PUSH batching: experimental candidate

Status: the fixed 256-process screen completed and failed acceptance. HOLD: do not merge into master or the upstream PR. Focused correctness checks below passed, but reliable performance benefit was not established.

## Matched source and scope

The engine baseline is official Unicorn 2.1.4 with published patches 0001–0010. Neither variant contains the held snapshot identity, DC ZVA, bounded temporary bitmap, or indexed service candidate. Java is the upstream-synchronized production tree bf76a5569f4e223944e5a08ff62a4a6a5f2c3c4c.

Fresh matched baseline native SHA256: 25df5bacf110c5f379dd97188ced4d32de5a767fce3513e50dbb549cb14af3df. Candidate: 87adf6d40664938893f9dc222f853f62883c071ad95e0eea909fd852195bdf89. The fresh baseline is not byte-identical to the previously published native artifact. No new binary or private test harness is included in this commit.

Only ordinary little-endian ARM32 PUSH/STMDB SP writeback on 64-bit hosts is eligible. User-bank, R13/R15-list, M-profile, IT, exit-suppressed, and BE/BE8 cases retain the original translation. At runtime the helper requires a full same-page aligned writable RAM range, exact NOTDIRTY tag, non-sentinel raw code tag, no EXEC permission, snapshot, write callback, pending stop/error/exit. Raw code non-sentinel is not proof of an executable mapping; it matches the original notdirty_write side-effect condition. The helper does not clear NOTDIRTY.

All fallback decisions occur before any store. Success reloads each source register immediately before its write and checks exit immediately afterward, using the real JIT return address. The original post-writeback runs only after normal completion. Default helper flags and a local TCG address temporary preserve synchronization and branch liveness. Final machine code was checked for per-element register load/store/PLT call and a fresh signed 32-bit exit-counter read.

## Diagnostic motivation, not performance

A separate instrumented eight-family run classified only statically eligible PUSH instructions in its warm window. Full runtime eligibility was map2 1,801,895/1,851,895; map100 1,510,529/1,611,529; SG4K 4,887,647/4,895,695; SG64K 2,516,850/2,520,731; TT 4,000,000/4,000,000; Telegram32 2,500,052/2,500,055. The two AArch64 workloads had zero such ARM32 calls. These are not all-store coverage, retired-store counts, or saved-time measurements.

The first diagnostic incorrectly used raw code == sentinel in the full intersection. It was corrected to != sentinel after inspecting notdirty_write, and all eight samples were rerun. The invalid first-run zero-intersection result was not used to reject or support the optimization. Original diagnostic evidence is retained privately, not committed.

## Correctness evidence and boundaries

- Eight representative Java smokes passed on the fresh baseline and candidate with actual loaded-native SHA witnesses.
- Eight direct ARM/Thumb-2 sparse/maximal-list complete-write cases passed on baseline. Candidate explicit-return-address injection passed 92 complete/partial/PC-override cases, including stop after the final element, unchanged SP on interrupted completion, exact memory prefixes, and untouched surrounding bytes. This injection is candidate-side contract testing, not an unmodified-baseline asynchronous timing comparison.
- Same-TB consecutive pushes, subsequent SP/register use, ARM condition-false, and Thumb 16-bit PUSH plus executed/skipped IT cases passed. A separate interposer confirmed fast-path entry where expected and zero entries for excluded conditional paths.
- Baseline and candidate produced identical state/memory summaries for late write-hook source-register mutation, per-element hook stop, cross-page unmapped fault, misalignment, RWX memory, read-only fault, and memory-snapshot restoration. Actual already-translated self-modifying code was invalidated correctly in both.
- Three nested emulation stop positions passed candidate-side partial-write/PC/SP checks and outer execution continued afterward.

An initial Thumb test warmed the adjacent 1 KiB page rather than the PUSH page, causing expected fallback and a failed fast-entry assertion; correcting the fixture stack address enabled the intended test. It was not a candidate memory-correctness failure.

These focused checks are not exhaustive proof, cross-platform validation, a full Maven test run, or a performance result. Private fixtures, test source, raw traces, and instrumentation remain outside Git.

## Fixed performance gate

Four blocks ABBA/BAAB/ABBA/BAAB, 128 cold and 128 long independent JVMs across eight families. Target six-ARM32-family geometric long-work ratio must be at least 1.05, with favorable target-suite direction in each block. Every family long/cold mean time growth must be at most 3%; mean peak RSS growth at most 2%. Runtime content, actual native hash, JVM heap, sample order and CPU affinity are pinned. No data-dependent extra repeats. Any gate failure or unresolved correctness risk holds acceptance. Overall 20x is not achieved or claimed.

## Completed screen (2026-10-29)

All 128 cold and 128 long rows completed with exit 0, planned order, correct actual-native witnesses and unchanged runtime manifest. Independent recalculation matched the analysis. The target six-family geometric long-work speed ratio was **1.018117**, below 1.05, with blocks **0.947284 / 0.848467 / 1.169124 / 1.143445**. The first two blocks were unfavorable. Eight-family cold-wall ratio was 0.944597; long-wall 1.005928; long-work 1.017469.

Ratios below are baseline time / candidate time (higher is faster); RSS is candidate / baseline (lower is smaller). These descriptive observations do not establish causation or equivalence.

| Family | Long work ratio | Cold wall ratio | Long RSS ratio |
|---|---:|---:|---:|
| map2 | 1.034333 | 0.879456 | 1.004832 |
| map100 | 0.951685 | 1.108790 | 1.008652 |
| sg4k | 1.039042 | 1.036540 | 0.997737 |
| sg64k | 1.117070 | 0.974904 | 1.003374 |
| tt | 1.046877 | 0.953043 | 1.001423 |
| jna64 | 1.008580 | 0.929612 | 1.005377 |
| tg32 | 0.931158 | 0.813211 | 0.998718 |
| tg64 | 1.022520 | 0.892777 | 1.002018 |

All per-family RSS guards passed. Cold-wall guards failed for map2, SG64K, TT, JNA64, TG32 and TG64; long-wall/work guards failed for map100 and TG32. The SG64K cold arithmetic-mean growth of 3.0071% alone was marginal, but multiple other failures are not dependent on that borderline result.

An independent method audit noticed that the plan's per-family “mean” did not name arithmetic versus geometric. A note recorded at 16:47 UTC, before final analysis, conservatively required both to pass the original limits; no runs or thresholds were relaxed. The existing target-suite definition remained geometric. Both summaries and all unfavorable blocks were retained.

Block heterogeneity was substantial, including the unchanged AArch64 control paths. For example TG32 work ratios ranged from 0.524614 to 1.652682. Do not attribute all observed regressions to this patch or select only favorable blocks. No extra data-dependent performance repeats were added. A separate subsequent CPU-stack diagnostic is for explaining costs only and cannot repair this failed acceptance screen.

Method limits: the parent process records affinity and Java inherits it normally; Java does not self-report affinity. The 2278-file runtime manifest pins actual artifacts. Production Java/pom files matched the merged source tree, and a preceding Maven compile log supports provenance, but this is not an independent reproducible-build proof or a claim that full Maven tests passed. Method scripts were retained and hash-checked separately.
