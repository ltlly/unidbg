# Indexed service lookup: completed screen (HOLD)

Source checkpoint: fce0e44b67f5a0e1e03ebb1665eaf980ea777468, based on ab9400b124b699f56518f87b347777866f25ce4b. This independent Java experiment is not combined with the held bounded-temp candidate. It does not establish an upstream or cumulative speedup.

## Change and semantic limits

Common service IDs use lazily grown Svc arrays indexed by numeric value. IDs below 256 and IDs 256 through 65535 occupy separate compact arrays. These are numeric partitions, not Thumb/ARM namespaces: collisions preserve identity. Negative and larger values retain a lazily allocated HashMap fallback. Shared empty arrays are never mutated.

Normal behavior retained: counter progression and skipping 128; replace-then-throw on collisions; registration visible before onRegister; callback failure retaining the service; reentrant registration; null for misses; and overflow IDs supported by fallback. No new static service references or caches. Neither version promises thread-safe concurrent registration.

Resource-exhaustion states are not guaranteed identical: array growth failure retains the old array after advancing the counter, whereas HashMap may have inserted a node before a failed resize. A sparse, artificially high first ID can also allocate a larger array. No OOM recovery or universal memory reduction is claimed.

## Correctness and fresh profiling

Baseline and candidate each passed 169,050 lookup/registration checks using real 32/64-bit guest emulators, including 4096 regular registrations, numeric boundaries, forced integer overflow, mixed-type collisions, callback failure and reentry. Another 21 checks per variant cover default stub bytes and non-null returned pointers, Thumb/A64/ARM encoding-failure retention, collision callback exclusion and unchanged counters on invalid registration. Eight candidate workload smokes passed. These bounded checks are not exhaustive emulator equivalence or all-platform certification.

Fresh baseline CPU samples included getSvc in 22/929 JNA64 samples and 6/494 TT samples. Over 200,000 loops, allocation sampling attributed 18,924,496 and 12,782,352 weighted bytes to getSvc stacks; candidate allocation profiles had no getSvc samples. Main-thread allocated-byte measurements in those separate profiled runs fell by about 1.1% for each workload. These are mechanism observations from single profiled runs, not controlled timing estimates, global peak-RSS reductions or a predicted speedup.

## Frozen unprofiled screen

64 cold and 64 long fresh JVMs across eight families, fixed ABBA then BAAB blocks. All exits, unique row keys, order, affinity [0,1], native/class witnesses and classpath checks passed. A 1413-file runtime/fixture manifest was unchanged before/after each phase. Both variants were compiled with identical JDK 8 source/target flags and swapped into the same classpath slot. Native resource slot is also fixed; extraction paths remain random and witnessed. No concurrent build, profiling or heavy archive ran during timing; light read-only monitoring occurred near the end. No extra data-dependent repeats.

Both variants use native SHA256 6e952301e5165a6e013fbdb1815020b5f9564877a72d30a9830de1cfeb16b03e. That snapshot-root baseline itself remains held from performance acceptance; this experiment does not implicitly accept it.

Baseline ARMSvcMemory class SHA256: 47add87351ec907eb74d9226bbbb1479c3a9e32eb121ea9d2e545f962a712713.
Candidate class SHA256: 1b88d77b145493230011a22b797b93a1e4351e10a052e2840dd5d061f6434058.

Telegram uses the frozen, previously verified expected bytes outside timed JVMs. SG completion and large-map determinism do not constitute independent business oracles.

Time ratios are baseline/candidate (>1 faster); RSS ratios are candidate/baseline (>1 more memory). Two-block descriptive estimates are not confidence intervals or proof of causality/equivalence.

| Family | Long work | Long wall | Long RSS | Cold wall | Cold RSS |
|---|---:|---:|---:|---:|---:|
| map2 | 1.0524 | 1.0410 | 0.9817 | 1.0021 | 0.9978 |
| map100 | 0.9973 | 0.9928 | 0.9743 | 0.9169 | 1.0211 |
| sg4k | 0.9975 | 0.9979 | 0.9926 | 0.9538 | 1.0098 |
| sg64k | 1.0362 | 1.0306 | 0.9897 | 1.1039 | 1.0200 |
| tt | 0.9895 | 0.9869 | 1.0053 | 0.9790 | 1.0032 |
| jna64 | 1.0093 | 1.0042 | 1.0073 | 1.0550 | 0.9980 |
| tg32 | 0.9496 | 0.9678 | 1.0027 | 0.9530 | 1.0038 |
| tg64 | 0.9910 | 0.9887 | 1.0126 | 1.0593 | 0.9927 |

Equal-family cold wall: 1.001094x; long wall: 1.000985x; long work: 1.002434x. These are effectively neutral at this screen resolution.

Adverse evidence retained: Map100 cold wall is 0.91689x (candidate time about +9.06%), both blocks adverse; Telegram32 cold wall is 0.95302x (+4.93%), both blocks adverse; Telegram32 long work is 0.94955x (+5.31%), both blocks adverse. SG64K cold RSS is about 1.01998, above baseline in both blocks. Some other workloads show favorable results, but these do not establish consistent general benefit.

**Decision: HOLD.** Reduced common-ID boxing is useful mechanism evidence, but this screen does not show reliable overall speed or peak-memory improvement. No production merge, no 20x claim, no multiplying these ratios by previous results, and no additional repeats solely to obtain acceptance. Source and this report are published; test/sample code, binaries and raw logs remain outside Git.

## Evidence hashes
- cold64.jsonl: 8ab1d05c0bc5dde7815a2510a771c66808684a397af369fdff8377e09d60cc17
- long64.jsonl: cb5f687398793e0a56ee50280820e0ac5c0087105d58995689a51b9b2faa72b9
- formal-plan.json: e001e77068b9f6d2178dcaf4fc6f265bef656dfe04ed40d6487cd45f26192669
- runtime-manifest.json: 73cb801be8b07a630dac2a9803825480d7b0c2ccaf1786d495850188819d87f1
- profile.jsonl: caf09ceb4c0d83fb50a7da349cdedb673275cbb1a3748b8562f8e21eb30c66d4
- profile-candidate.jsonl: 85fec51573409401e5a165e900d17fd7f1e743be58b457beaacd4d7abc403106
- smoke.jsonl: b495be160d4f2af0f6055e4ef088d6b6b5de815d2b08c0266a9f2242187d0a56
- check-baseline.log: 4349f141996efb8939caa048c10c09ccbf3ac671aea2cefe0c727708f0a40d5f
- check-candidate.log: 4349f141996efb8939caa048c10c09ccbf3ac671aea2cefe0c727708f0a40d5f
- default-baseline.log: a1f2c3a8fa7ca8a5025ae6ac85e5d67e64f6081921fcddc3cb6270f543d37b6a
- default-candidate.log: a1f2c3a8fa7ca8a5025ae6ac85e5d67e64f6081921fcddc3cb6270f543d37b6a
