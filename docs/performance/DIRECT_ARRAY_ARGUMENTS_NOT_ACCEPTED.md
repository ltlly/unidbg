# Direct-array argument parsing: allocation result, not accepted

## Decision

The private Java candidate is **not accepted into production**. Production code remains at 9b8775ebbff6d849ade96ecbd365d726f40fb62a; subsequent commits through c361d5018672b7088ce79d9aeee05959d8678933 are documentation only. The 20× goal has not been achieved.

The candidate replaces the two temporary lists in ProxyUtils.parseMethodArgs with fresh, exactly sized arrays. It preserves conversion expressions, order, null handling, unpacking, lookup and visitor behavior. It introduces no sample-specific conditions, cache or new runtime API. No candidate source, test code, samples or raw logs are included here.

## Verification

- Recompiling the baseline with matching Java 8 debug/compiler options reproduced its class files byte-for-byte. The candidate changed only ProxyUtils.class and ProxyUtils$MethodArgs.class. Native SHA-256 remained 27fcc8f9c7c6bf3708fd5fc4943941d008ffa493bde49283dff80e9689d32dfa.
- 10,035 strengthened differential checks passed, including explicit expected success/failure, reference identity, custom class loading, boxing and floating-point bits.
- 72 integration assertions passed for constructors, static/instance calls, VarArg/VaList, cached members, visitor mutation and class-name patching. An initial fixture incorrectly expected constructor visitors to run before construction; that baseline fixture failure was preserved and the expectation corrected, not production behavior.
- 32 cold/long fixture smoke processes passed.
- Interpreter-only mechanism checks saved 48, 112, 160 and 224 allocated bytes per parse at arities 0, 1, 8 and 16. These are not JIT speed or resident-memory measurements.

## Fixed allocation pilot

64 processes covered eight cases, with two four-process ABBA/BAAB blocks per case. All observations were retained. Both 1,146-file manifests, 23 protocol pins, native identity, eleven class witnesses per process, argv, affinity, order and captures passed audit.

Candidate minus baseline mean current-thread allocated bytes:

| Case | Byte difference | Relative difference |
|---|---:|---:|
| Map2 | -20,838,910 | -0.823% |
| Map100 | -12,132,436 | -1.265% |
| JNA64 | -214,231,890 | -2.568% |
| SG4k | +343,530 | +0.233% |
| SG64k | -29,354 | -0.020% |
| TT | +190,858 | +0.0037% |
| Telegram32 | 0 | 0% |
| Telegram64 | -84 | -0.352% |

Map2, Map100 and JNA improved in both blocks; each candidate observation was below every baseline observation for that case. The tiny changes in the other cases do not establish improvement. Allocated bytes are neither allocation-event counts nor retained heap or RSS.

## Why held

Timing neutrality remains unproven. Candidate JNA work reached 23.559 s versus baseline maximum 11.816 s; Map100 reached 13.320 s versus 9.519 s. SG64k candidate work was higher in both blocks (+19.72%, +10.96%). Baseline runs also had substantial tails. Telegram32 candidate mean RSS was +4.92 MiB (+4.08%), although maxima were nearly equal and a low baseline observation influenced the mean.

Independent identical-artifact controls had already demonstrated large timing variation, including a native-only control. This does not prove that adverse candidate observations are harmless noise. Two blocks support neither a speed claim nor a convincing no-regression claim. Allocation improvements do not override those unresolved safety observations.

Rows do not embed protocol/manifest hashes; the pinned runner verified them before launch. The nested MethodArgs class had manifest verification but no independent runtime class witness. Raw diagnostics and fixtures remain outside Git.
