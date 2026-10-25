# Exit-request guard: corrected-harness validation remains HOLD

This is a separately preregistered measurement protocol, not another repetition of the original formal test. The native candidate is unchanged (8077e2201544faa74139e8185ecaa967e55b5e44a268436950b6ffe9011f5eb6), compared with identity-corrected a87c36222f1e44490217a0fcc0cd04f52fb2144255c66dee595b7a7805074c6b. The old formal and memory diagnostics remain in EXIT_GUARD_CURRENT_VALIDATION.md and are not pooled or invalidated.

## Why the protocol changed
The Telegram harness computed an independent JCE PBKDF2 reference result before constructing the emulator on every JVM launch. Direct oracle computation was outside emulator phase timers but inside whole-process CPU/wall/peak RSS. Native allocation samples were dominated by JVM compiler stacks; neither that observation nor NMT proved the specific cause of the prior RSS difference.

Expected vectors were therefore generated outside timed JVMs and independently verified byte-for-byte with JCE and Python hashlib: PBKDF2-HMAC-SHA512, ASCII/UTF-8 password "123456", eight zero salt bytes, 64-byte output, iteration counts 100/1000/100000. Frozen decoding was separately checked against JCE. Unsupported counts fail. Every guest invocation, warmup, iteration count, JNI buffer initialization and output comparison remains unchanged. This changes benchmark setup, not production unidbg or the guest algorithm.

Both variants use the same absolute native-resource slot and classpath order. Its native file is replaced only after the preceding JVM has exited; resource and actually loaded native hashes are checked each run. Main-class and oracle-class sources are recorded and verified. NativeLibraryUtil still extracts libraries to randomized temporary paths; these were recorded, not falsely claimed constant.

## Fixed independent matrix
128 cold plus 128 long fresh JVMs, four ABBA/BAAB blocks across eight families. All 256 outputs, native witnesses, classpaths, executed class sources, affinity and 1400 runtime pins passed audit. Old guard/lazy pilot, first formal and A/A control are not mixed into these estimates. Four-cluster bootstrap intervals are descriptive, not broad equivalence proof.

Long-work baseline/candidate ratios:
- Map2 1.0522 [1.0011,1.0889]; Map100 1.0083 [0.9992,1.0168].
- SG4K 1.1310 [1.1104,1.1520]; SG64K 1.2070 [1.1763,1.2277].
- TT 1.0980 [1.0611,1.1551]; JNA64 0.9833 [0.8866,1.0627].
- Telegram32 1.1436 [1.0935,1.1800]; Telegram64 1.0737 [1.0436,1.1237].
Equal-family long-work geometric mean 1.08496x; long process 1.07752x; cold process 1.01945x. This does not establish an overall 20x improvement or allow multiplying earlier increments.

Telegram32 long peak RSS was 102.20→102.41 MiB under this protocol, versus the prior protocol's 108.73→116.39 MiB. This is not a causal decomposition: setup/path conditions changed together and instrumentation results varied. All family mean peak-RSS increases were below the preregistered 2% limit. SG64K long RSS fell from 151.40 to 139.11 MiB, but that isolated reduction is not a universal guarantee.

## Decision against the predeclared gates
The protocol required each family's mean RSS growth ≤2% AND descriptive upper growth bound ≤5%. Cold SG4K mean RSS 115.03→117.14 MiB was approximately +1.83%, but its descriptive upper growth bound was approximately +6.47%. That uncertainty gate failed. It is not proof of a 6.47% actual regression, but it prevents declaring the required near-unchanged memory established under this gate.

Status remains HOLD / NOT ACCEPTED. Promising throughput evidence is preserved, including JNA64's adverse and uncertain point estimate. No further repetitions solely to obtain acceptance; do not relax the gate after seeing results. The validated branch and bundled native stay unchanged. New harnesses, reference-vector generators, samples, raw logs and binaries remain outside Git.
