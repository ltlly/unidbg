# Backend eligibility audit: no default switch

The performance objective requires preserved behavior. Switching to an existing backend is not newly implemented optimization, and passing sample outputs does not prove equivalent semantics.

Environment: Linux x86-64, ARM/AArch64 guests. Existing Dynarmic native SHA256 39eaa95e2701821a161105b9ce51f8082a4be87c970af77ac62a73ecd66dcdb6; explicit DynarmicFactory(false), no silent fallback. Eight small Map2/Map100/SG4K/SG64K/TT/JNA64/Telegram32/Telegram64 sample runs passed their output/round-trip checks and reported the actual Dynarmic backend plus loaded native hash. No throughput ratio is inferred from these smoke timings.

## Shipped-binary contract probes
Six separate ARM64 JVM probes compared the existing Dynarmic binary with the identity-corrected Unicorn2 native a87c36222f1e44490217a0fcc0cd04f52fb2144255c66dee595b7a7805074c6b. Each final probe recorded the loaded native hash and actual backend class. External 30-second process timeouts were used; none fired.

1. Host-code rewrite: execute MOV X0,1; replace it with MOV X0,2; execute again. Unicorn2 returned 1 then 2. Dynarmic returned 1 then 1, reusing stale translated code.
2. Instruction count: execute two MOV instructions with count=1. Unicorn2 left X0=1; Dynarmic left X0=2, ignoring the limit.
3. Guest store into a read-only data page. Unicorn2 raised UC_ERR_WRITE_PROT and retained zero. Dynarmic completed and wrote 42.

These are observed capability differences, not performance regressions introduced by this work. The probes do not establish every unsupported behavior on every architecture.

## Source audit
DynarmicBackend.emu_start ignores count/timeout and handles until through its SVC convention, not a general PC boundary. Code/read/write/block hooks throw UnsupportedOperationException. Debug hook, single-step and fast-debug paths are no-ops in the adapter/inherited FastBackend. Native page permissions are stored but not consulted by the inspected read/write/fetch paths. Native unmapped read/write callbacks call Java and then abort rather than implementing Unicorn-style recovery. Explicit removeJitCodeCache inherits a no-op. CPU context operations copy CPU state, not memory snapshots; nested execution safety is not established. Source analysis alone is distinct from shipped-binary evidence above.

The default Unicorn1 adapter also lacks some Unicorn2-specific features; forwarded calls are not proof of native parity. No blanket backend equivalence is claimed.

## Decision
Do not change the default backend or count an existing faster restricted backend as general 20x optimization. Any future backend-selection proposal must explicitly preserve the caller's required capabilities, including hooks, limits, memory protection and code invalidation. Samples passing alone are insufficient. Production code and native resources remain unchanged; new probes, sample wrappers and raw results remain outside Git.
