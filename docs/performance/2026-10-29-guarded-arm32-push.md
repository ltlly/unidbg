# Guarded ARM32 PUSH batching: experimental candidate

Status: correctness screens described below passed; fixed performance screen is running. This is not accepted for master or the upstream PR. No speedup is claimed.

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
