# Advisory near-code mmap hint: held experiment

Status: NOT ACCEPTED. Source-only experiment; no native library update and no general 20x claim. Compared against the identity-corrected private baseline, not original upstream and not the current published bundled native.

## Change and scope
Linux LP64 x86-64 ordinary TCG arenas up to 1 GiB receive a page-aligned advisory mmap hint 64 MiB above the engine's native initialization function. mmap is never fixed. Overflow/page checks precede hint construction. A failed hinted allocation retries the original NULL-hint allocation once; successful distant mappings are retained. Larger arenas and other platforms retain original behavior. Existing individual branch displacement checks, cache capacities, guard pages, protections and ASLR are unchanged. This is a heuristic, not a placement guarantee. Host-page rather than huge-page alignment may affect huge-page behavior and is not assumed harmless from source alone.

Baseline SHA256: a87c36222f1e44490217a0fcc0cd04f52fb2144255c66dee595b7a7805074c6b.
Candidate SHA256: 23d3009d166d8c8ccd25286e0961c7c04a6c35ab98ce0c16e7ff1e9d0ae30442.

## Correctness and mechanism evidence
24 fresh native layout processes covered ARM32/AArch64, 1 MiB/default 1 GiB/2 GiB arenas, guards and guest execution. Eligible hints were honored in the simple single-engine cases, but the baseline was already near. Twelve fresh single-JVM diagnostics likewise showed no missing baseline whole-arena reach in their observed workloads.

Eight fresh native processes with three live engines changed observed call placement from near/far/far to near/near/far. Emitted translation-block bytes fell for that second engine (ARM32 264 to 234, AArch64 272 to 239). This is a mechanism result, not a workload speedup.

Twelve forced mmap64 tests covered hinted failure with one NULL fallback, accepted far success without retry, double failure, configured sizes, guest output and unmapping on close. ARM/ARM64 native suites, callback/global-state tests, RAM identity/lifecycle checks, eight sample smoke processes and fourteen JUnit cases passed. An initial checker incorrectly expected an unpublished inline guard and failed on the baseline first; that invalid expectation was corrected, and the original failed artifacts retained privately.

## Instrumented dual-engine screening
48 fresh JVMs: four ABBA/BAAB blocks, three workloads, two live engines per JVM. Map2 used 10,000 calls per engine; TT and JNA64 used 100,000. Probes ran before timed work. Native hash witnesses, row order and golden outputs passed in all 48 processes. This was screening with diagnostics, not a formal uninstrumented acceptance run.

Mean work speedups (baseline/candidate), engine 0 then engine 1:
- Map2: 0.9745x, 1.0082x. Mean process speedup 0.9928x; RSS 141.20 to 140.78 MiB.
- TT: 0.9783x, 0.9918x. Mean process speedup 1.0066x; RSS 126.88 to 124.93 MiB.
- JNA64: 1.0355x, 1.0208x. Mean process speedup 1.0191x; RSS 121.63 to 120.79 MiB.

Crucially, engine 1 lacked whole-arena helper reach in every observed JVM for both variants. Engine 0 baseline reach was true in 6/8 Map2 and 8/8 TT/JNA runs; candidate was 7/8 Map2, 8/8 TT and 7/8 JNA. The synthetic three-engine placement improvement therefore did not reliably transfer to these Java processes. Block-level speedups were mixed, and no causal workload benefit is established.

## Decision
Hold the experiment and do not change the validated branch or its bundled native library. No repeated screening until favorable, no sample-specific placement adjustments, no multiplication with earlier speedups. New harnesses, sample code, raw logs and private diagnostic binaries remain outside Git; only this report and the source patch are published on an experimental branch.
