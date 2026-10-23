# Fixed ARM64 dl_iterate_phdr trampoline

Relative to `386918a78d6bbbd3d55665e3a408ca02c410500f`, normal service
registration emits the same 29 little-endian ARM64 instructions directly instead
of invoking Keystone. Reserved or unusual service numbers retain the original
assembler path. This is a generic linker optimization, without application names,
result caching, or changes to callback behavior. It does not remove Keystone from
other paths or accelerate every application.

## Validation

Private differential tests compared every normal service number (65,534 cases)
with the original assembler output. All bytes matched; allocation bounds and
labels were checked. Six abnormal/reserved-number outcomes matched the baseline.
Both variants passed 24 real-module execution rounds covering empty lists,
callback order/data, early returns, low-32-bit callback comparisons, saved
registers, post callbacks, reentry rejection and allocation cleanup. Existing
callback stack alignment was preserved, not changed. Sixteen Java regressions
passed. Fresh-process checks confirmed that this registration path no longer
loads Keystone's native binding in the candidate. Tests and samples are not
included in Git.

## Measurement

Linux x86_64, Java 8, fixed 128 MiB heap; identical native library in both arms
(SHA-256 `5d6898c8ad234d08251718412fa1168074c352ca6d8bf402bfb5b767b289cb6b`).
Class origins and mapped native hashes were checked. OS caches were not cleared.
Seven alternating fresh-JVM pairs for each of eight workloads produced 112 cold
runs. Telegram ARM64 exercised this path and showed:

- Load: 159.79 to 104.53 ms mean; paired geometric speedup 1.5285x,
  exploratory 95% interval 1.4292–1.6018.
- Whole-process wall: 608.59 to 537.55 ms mean; paired speedup 1.1312x,
  interval 1.1053–1.1615 (about 11.6% lower duration).
- Peak RSS candidate/baseline: 0.9641, interval 0.9566–0.9715.

All seven pairs improved these three measurements. Other seven workloads did not
show comparable benefits: aggregate wall speedup 1.0072x [0.9930, 1.0218]. Small
Telegram ARM64 onload/warm phases had slower point estimates (approximately
1.5/2.4 ms additional mean duration), despite the lower total wall time.

Five longer pairs for each workload produced 80 runs. An environment restart
split these into 21 complete pairs before and 19 complete pairs afterward; no
pair spans the restart. Every workload's operation-time and wall-time interval
included neutrality. Equal-workload aggregate operation speedup was 0.9949x
[0.9740, 1.0163], wall 0.9972x [0.9799, 1.0137]. Small/large-map workloads had
negative operation point estimates of 3.3%/2.5%, with order/epoch sensitivity;
these data do not establish strict nonregression bounds. Telegram ARM64 long-run
RSS ratio was 0.8900 [0.8812, 0.8992]. No steady-state allocation-reduction claim
is made from noisy small allocation counts.

Intervals are exploratory paired bootstrap intervals, not adjusted for multiple
comparisons. The demonstrated benefit is cold loading on this generic ARM64
linker path, not universal startup acceleration, sustained throughput improvement,
or the requested overall 20x speedup. Only this host configuration was measured.
