# Empty TLB flush clock read: held experiment

Independent source-only experiment over RAM identity baseline a87c36222f1e44490217a0fcc0cd04f52fb2144255c66dee595b7a7805074c6b. Candidate native SHA256 b236415f7e3218b8d711b6f1cdaadde76531794a1cd18788b09bf82a546df6d2. The empty jump-cache flag patch is not included.

Only defer the host-clock read until to_clean is nonzero in tlb_flush_by_mmuidx_async_work. Empty paths have no timestamp consumer; all dirty bookkeeping, cache clearing and flush counters remain. Nonempty paths still use one shared timestamp. Sampling it a few operations later may alter an adaptive resize decision near a timing boundary; no bit-identical capacity-trajectory claim is made. No field, cache or allocation is added.

Separate diagnostics retained original behavior and found many empty-to_clean calls (power-of-two prefixes, not final totals). Those measurements are not timing evidence. A parallel diagnostic of local-temp copy selection found a small number of opportunities but did not change optimization output; that diagnostic is absent from the candidate.

## Validation

ARM/AArch64 native units, callback/nested/timeout and store checks, 200 explicit TB-request/context/map cycles per architecture on both baseline and candidate, RAM identity/lifecycle and subpage checks pass. No new JUnit claim. Full native rebuild; no binary publication.

Fixed independent screening: 64 cold and 64 long fresh JVMs, eight families, two ABBA/BAAB blocks. All exits, order, affinity, actual native hash and unchanged runtime/fixture witness checks pass. Same classpath/native resource slot, externally preverified Telegram oracle, no concurrent builds/profilers. Actual extracted native paths remain randomized and witnessed. No pooling with another candidate or diagnostics, and no further repetitions to obtain acceptance.

Ratios are descriptive geometric estimates, not confidence intervals or causal guarantees. Speed is baseline/candidate; RSS is candidate/baseline.

| Family | Long work speed | Long wall speed | Long RSS | Cold wall speed | Cold RSS |
|---|---:|---:|---:|---:|---:|
| map2 | 0.9654 | 0.9677 | 0.9967 | 1.0550 | 0.9996 |
| map100 | 0.9896 | 0.9839 | 1.0033 | 0.9369 | 0.9892 |
| sg4k | 1.0125 | 1.0214 | 0.9969 | 0.9643 | 0.9102 |
| sg64k | 0.9708 | 0.9650 | 1.0391 | 0.9637 | 1.0083 |
| tt | 1.0156 | 1.0193 | 1.0183 | 1.0287 | 0.9927 |
| jna64 | 1.0159 | 1.0158 | 1.0045 | 0.9987 | 1.0047 |
| tg32 | 1.0208 | 1.0012 | 1.0083 | 1.0247 | 1.0030 |
| tg64 | 1.0353 | 1.0179 | 0.9928 | 1.0003 | 0.9958 |

Equal-family long work/process ratios are 1.0030x/0.9988x and cold-process 0.9959x: no useful broad speed signal. Cold map100 wall is slower in both blocks (0.9708/0.9043); cold SG4K and SG64K wall also decline in both. Long SG64K work is 0.9708x and RSS 1.0391x, with both blocks adverse. JNA64 long work is 1.0159x in both blocks, insufficient to offset the other observations. Prior identical-native variability cautions against causal claims from small changes or attributing RSS differences solely to this patch.

Decision: HOLD. Preserve the independent adverse results and leave validated production unchanged. Do not multiply stage ratios into cumulative performance or claim the requested overall20x. Tests, samples, binaries and raw logs remain outside Git.
