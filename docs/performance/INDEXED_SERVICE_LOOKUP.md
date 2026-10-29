# Indexed service lookup: experimental candidate

Status: correctness checks passed within the scope below; frozen performance screen pending. No production acceptance or overall speedup claim.

Common service IDs use lazily grown Svc arrays indexed by numeric value. Values below256 and256..65535 occupy separate compact arrays, but these are numeric partitions, not Thumb/ARM namespaces: collisions retain the same identity. Negative and larger IDs retain a lazily allocated HashMap fallback. Empty arrays are shared without mutation; growth occurs before assignment.

Preserved ordinary behavior: numeric counter progression and skipping128, replace-then-throw on duplicate IDs, registration visibility before onRegister, callback failure retaining the service, reentrant registration, misses returning null, and overflow IDs supported through fallback. The table remains non-thread-safe as before. Resource-exhaustion behavior is not claimed identical: failed array expansion retains the previous array after the counter was advanced; HashMap may have inserted a node before a failed resize.

Correctness: baseline and candidate each passed169,050 lookup/registration checks over real32/64-bit emulators, including4096 regular registrations, numeric boundaries, forced integer overflow, mixed-type collisions, reentry and callback failures. An additional21 checks per variant cover default stub bytes and returned pointers, default Thumb/A64/ARM encoding-failure retention, collision callback exclusion and unchanged counters on invalid type/null/architecture. Eight representative candidate JVM smokes passed. These are bounded tests, not complete emulator equivalence or OOM guarantees.

Fresh baseline profile on the current experimental6e9523 native observed getSvc CPU samples22/929 for JNA64 and6/494 for TT. Allocation sampling attributed18,924,496 and12,782,352 bytes respectively to getSvc stacks over200k loops. These are sampling observations, not predicted speedups; whole-process allocation and memory must be assessed separately.

A/B use exactly the same native SHA2566e952301e5165a6e013fbdb1815020b5f9564877a72d30a9830de1cfeb16b03e, whose independent performance acceptance is still HOLD. This Java candidate is not combined with the rejected bounded-temp patch. Source is based on ab9400b124b699f56518f87b347777866f25ce4b. Baseline/candidate classes are compiled with identical JDK8 source/target flags and witnessed by SHA256 at runtime.

Prespecified screen: eight families, fixed ABBA/BAAB,64cold+64long fresh JVMs, identical classpath/fixed class/native slot, native and class witnesses, immutable runtime manifest, affinity[0,1], no concurrent profiling/build, no data-dependent extra repeats. Tests, samples, binaries and raw logs are not committed. Results and adverse cases will be retained.
