# Performance optimization plan

## Baseline

Upstream: zhkl0228/unidbg, commit `2ba8156eef31501ebd35194adb5795ef3e01a285` (0.9.10-SNAPSHOT).
Changes are isolated on `perf/validated-optimization`. Preserve upstream license and attribution.

## Goal, not an achieved result

Target: 20x performance improvement with memory reduced or approximately unchanged. No speedup has been measured yet. A local hot-path speedup does not establish a 20x end-to-end emulator speedup.

In the absence of a supplied workload, establish reproducible API/JNI/memory and representative native-emulation workloads first. Record the workload, input sizes, backend, architecture, JVM, CPU, warmup, repetitions, wall time, CPU time, Java allocation and peak process RSS. Compare identical inputs and outputs on the same machine; keep backend changes separate from implementation improvements. Do not disable correctness, hooks or required behavior to improve scores.

Provisional memory gate: no sustained allocation increase and median peak RSS within 5% of baseline across independent JVM runs; report raw samples and noise rather than hiding regressions. This is an engineering tolerance, not a user-approved relaxation of the goal.

## Incremental workflow

1. Preserve baseline and build/test evidence.
2. Add deterministic correctness tests and a benchmark that reproduces measured overhead.
3. Profile and optimize one bounded hot path at a time.
4. Run correctness, differential baseline comparisons and memory measurements.
5. Commit each completed step and publish it to this branch; verify the remote commit before the next step.
6. Publish successes, regressions and remaining gaps. Never call the target achieved on cherry-picked microbenchmarks.

Remote commits are created through the authenticated GitHub connector and branch refs are updated there (the equivalent of publishing local commits); no personal access token is requested. No external coding-agent or Codex job is launched.

## Initial findings

The source contains small per-element backend memory reads and Java buffer allocations in UnidbgPointer. This is only a candidate for measurement, not proof of an application bottleneck. The repository defaults to skipping tests, so validation must explicitly enable them.
