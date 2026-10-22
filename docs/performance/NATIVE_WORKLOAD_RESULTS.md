# Guest-entry JNI validation and limits

## Equal-work comparison

Same native Unicorn2 backend, same deterministic input/output, five independent JVM runs per variant, alternating order, fixed 128 MiB heap, 1,000 warmup operations per process. Measured iterations: 1,000 for 1,024-element JNI; 500 for 16,384-element JNI and CPU control; 200 for 65,536-element JNI.

| Workload | Architecture | Speedup | Baseline RSS KiB | Candidate RSS KiB | RSS change |
| --- | --- | ---: | ---: | ---: | ---: |
| compute 10000 | ARM32 | 0.94x | 61568 | 61320 | -0.40% |
| jni 1024 | ARM32 | 5.73x | 100500 | 96564 | -3.92% |
| jni 16384 | ARM32 | 23.82x | 107312 | 101780 | -5.16% |
| jni 65536 | ARM32 | 30.03x | 107956 | 106836 | -1.04% |
| compute 10000 | ARM64 | 0.95x | 60384 | 60276 | -0.18% |
| jni 1024 | ARM64 | 6.05x | 92576 | 86552 | -6.51% |
| jni 16384 | ARM64 | 26.73x | 99188 | 94072 | -5.16% |
| jni 65536 | ARM64 | 32.18x | 100980 | 99308 | -1.66% |

The two large JNI transaction sizes exceed 20x on both architectures in this run, with median peak RSS reduced (~1–5%). The 1,024-element case does not reach 20x with this fixed operation warmup. Java allocation in large JNI transactions drops by roughly 71%. This supports the target for these explicit large-array JNI workloads only.

## Adverse results retained

The separate one-second warmup series produced more completed warmup operations in the faster implementation. Large-array JNI latency improved ~28–32x, but peak RSS rose in some cases (up to ~35% for ARM32 at 1,024 integers). This unequal-work series does not satisfy the memory gate. It remains committed in native-workloads-paired.txt/native-workloads-summary.json rather than being discarded. Allocation per operation fell, but total allocation rate and JVM/JIT/GC behavior can still raise process RSS. Equal-work results do not guarantee equal memory in every continuously saturated deployment.

The CPU-only decrement-loop control does not call the changed method. It measures 0.94–0.95x in the fixed-work series and 1.03–1.10x with time-based warmup. These short-process measurements show startup/scheduling/JIT noise; no CPU-loop acceleration or universal no-regression guarantee is claimed.

## Acceptance status

- The implemented change and native correctness tests are complete and published.
- Target established on large-array read and guest-entry SetIntArrayRegion transaction fixtures with equal-work memory comparison.
- Overall unidbg / arbitrary application 20x is NOT established. Pure native compute does not benefit from this patch.
- A representative application-level acceptance requires a concrete native library, emulator/backend configuration, JNI call sequence, inputs and expected outputs. No such user workload has been supplied.
- Existing Android64 demonstration guest stack-corruption failure remains unresolved in both original and changed code. Legacy Unicorn library loading remains unavailable here.
- No external coding agent, remote Codex job, GitHub Actions benchmark or token configuration was used. Commits were published through the existing GitHub connector.

## Reproduce

See BENCHMARK.md, NativeWorkloadBenchmark.java and scripts/performance/run_int_array.py. Use scripts/performance/summarize_native_workloads.py on either complete raw series. Do not mix phases or omit failing samples. The original warmup series defaults to time-based warmup; supply final argument 1000 for fixed work.
