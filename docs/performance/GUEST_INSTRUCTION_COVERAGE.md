# Guest instruction coverage diagnostic

No performance change, timing claim or general20x claim. This closes a previously unmeasured instruction-attribution question, not a benchmark acceptance gate.

Unicorn2 identity native a87c36222f1e44490217a0fcc0cd04f52fb2144255c66dee595b7a7805074c6b; all final eight JVMs reported the loaded hash and passed existing output/round-trip checks. One predetermined warm call per family; Telegram used 100 PBKDF iterations. These small diagnostic windows are not the longer acceptance workloads.

A block hook was installed before loading/warmup and counting activated only for the selected warm call. It captured current bytes and explicit ARM/Thumb mode, caching by address/mode/content so remaps and modified code could not reuse stale decodes. Positive bounded block sizes and contiguous decoded sizes were checked. Caps: 500,000 captured entries, 10,000 unique versions, roughly4 MiB captured code. Reaching a cap disables counting until cleanup. Hooking changes generated code and overhead; recorded workload times are unusable as performance evidence.

Counts describe block-entry-weighted decoded static instruction exposure, NOT retired instructions. Predication, faults, stops inside blocks and Thumb IT may prevent later instructions in an entered block from executing. Generic SIMD/FP was not classified from mnemonic prefixes.

Final observations (entries / decoded weighted exposure):
- Map2:535 /3160.
- Map100:29851 /240231.
- SG4K:92091 /1259755.
- SG64K:500000 captured plus one cap-triggering callback /8746248; TRUNCATED.
- TT:106 /588.
- JNA64:103 /393.
- Telegram32:42182 /2191082.
- Telegram64:22966 /598192.

All captured unique blocks decoded completely. No captured mnemonic began with AES, SHA, SM3 or SM4. This does not mean the programs perform no cryptography, does not cover unobserved paths, and cannot establish absence in the unsampled SG64K tail. Telegram64 exposure emphasized add/eor/ror; Telegram32 loads/bit operations and carry arithmetic; SG emphasized loads/bit operations/stack traffic. No specialized crypto-instruction hotspot is established by this trace.

Initial diagnostic failure: the first SG64K run crashed in JVM JNI invocation while the tracer used immediate in-callback detachment upon saturation. Original output, crash report and tracer source were retained privately. Changing saturation to stop counting without removing the live hook, then detaching after the workload, allowed the fixed diagnostic suite to finish. This correlation does not establish the full crash root cause. A separate small ARM64 self-removal loop succeeded with exact native witness; it does not resolve the larger ARM32 crash. No production hook implementation was changed and no unsupported claim of a generic hook-removal bug is made.

Decision: do not pursue an AES/SHA instruction-specific optimization on the assumption these measured paths use those instructions. Do not substitute guest algorithms, alter advertised CPU features, or introduce sample-specific dispatch. New trace wrappers, raw outputs, bytecode probes and crash logs stay outside Git.
