# Emitter-only exit-request guard: promising, not accepted

This closes the independent-validation gap in an older two-repeat guard/lazy pilot. It is not the accepted zero-exit optimization and does not change helper call flags. No overall 20x claim, bundled native update or validated-branch promotion.

## Mechanism and correctness
Linux x86-64 ARM/AArch64 TCG emitter recognizes only check_exit_request calls. It emits a DWORD comparison of the current CPU negative icount field with zero and JNS over the original near/far call on the returning path. A negative request invokes the unchanged helper, including delay-slot, nested-level, PC-override and longjmp behavior. Original TCG call flags, argument preparation, clobbers, global synchronization and callback reload barriers remain intact. NO_WRITE_GLOBALS remains rejected.

Current identity baseline SHA256 a87c36222f1e44490217a0fcc0cd04f52fb2144255c66dee595b7a7805074c6b; candidate 8077e2201544faa74139e8185ecaa967e55b5e44a268436950b6ffe9011f5eb6. Fresh source review, four ARM/AArch64 near/far emitted-code proofs, native ARM/ARM64 suites, callback/nesting/timeout checks, store/identity/lifecycle/subpage checks and eight sample smokes passed. This experiment does not claim a fresh JUnit run.

## Independent fixed validation
Exactly 128 cold plus 128 long fresh JVMs, four ABBA/BAAB blocks, eight families. All 256 records and 1395 runtime pins audited. Old pilot and identical-native A/A control are not pooled. Four-cluster bootstrap intervals are descriptive, not broad equivalence proof. Small differences cannot confidently be assigned to this patch given the separate A/A noise finding.

Long-work ratios baseline/candidate:
- Map2 1.0330 [1.0056,1.0538]; Map100 1.0128 [1.0028,1.0261].
- SG4K 1.1385 [1.0989,1.1824]; SG64K 1.1835 [1.1783,1.1888].
- TT 1.0473 [1.0300,1.0605]; JNA64 1.0102 [0.9911,1.0297].
- Telegram32 1.1628 [1.1500,1.1738]; Telegram64 1.0932 [1.0652,1.1235].
Equal-family long-work geometric mean 1.08325x, process 1.07437x. This is incremental versus a private identity-corrected baseline, not original upstream.

Adverse observations retained: Telegram32 peak RSS 108.73→116.39 MiB, ratio 0.9351 [0.8814,0.9860]. SG4K cold process 0.9465 [0.9019,0.9933]; Map2 initialization 342.12→370.39 ms. Cold equal-family process 0.98882x. Long suite mean RSS near equal does not erase individual increases.

## Bounded memory attribution, not acceptance reruns
Four instrumented Telegram32 JVMs in fixed ABBA order paused their main thread after warmup/work/close for process-map snapshots. Both arms had 2 MiB resident TCG arenas and 15,265,792 guest-resident bytes. Generated code grew from 1,405,552 to 1,428,416 bytes, about 23 KiB. Whole-process RSS differed more substantially: A ~127.3–127.6 MiB and B ~141.6–144.3 MiB after work. Excess remained after closing the emulator, when its TCG arena was gone. Java heap usage and heap-map residency were similar; a large anonymous allocation region differed. Anonymous region ownership/cause is NOT established by its address or shape.

A fixed two-process NMT attempt could not attach with jcmd; it supplied no NMT attribution. A separate fixed A/B diagnostic recovered read-only NMT through the in-process DiagnosticCommand MBean. It reported broadly similar JVM tracked memory (committed 183654/183811 KiB), while process RSS differed by only ~1.4 MiB. NMT does not account for every third-party allocation or allocator-retained page. Instrumentation changed behavior and did not reproduce the earlier RSS gap reliably; it neither proves the patch leak-free nor attributes the observed increase to Java or Unicorn.

Decision: retain as a promising source-only experiment, NOT ACCEPTED. Do not dismiss the memory/startup concern, subtract control ratios, repeat performance runs until favorable, or promote solely from the mean speedup. Further work needs a new causal memory hypothesis or a materially changed implementation. New tests, probes, raw outputs and binaries remain outside Git.
