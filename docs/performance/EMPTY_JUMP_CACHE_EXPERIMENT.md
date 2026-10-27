# Empty TB jump-cache clearing: held experiment

Source-only experiment over the RAM-identity correction. Not merged into the validated production branch; no bundled native binary, tests, fixtures or raw logs are added to Git. Overall 20x remains unachieved.

## Mechanism

A conservative per-CPU flag records that a TB jump-cache might contain a pointer. All four potentially non-null assignment sites set it before their store, including explicit TB requests. Whole-cache clear returns early only when the flag proves the table already empty, and resets the flag after an actual clear. Single-entry/page invalidation leaves it true. TLB dirty state is not used to infer jump-cache emptiness. No mapping, SMC, callback, timeout or invalidation semantics are intentionally relaxed. No clock optimization is included.

On the tested Linux x86_64 build CPUState remains 33,216 bytes: the flag at offset 33,212 fits the former tail padding. This was checked by a compile-time sizeof/alignment assertion. Other hosts were not rebuilt; this is not a new concurrent-engine guarantee.

Baseline native SHA256: a87c36222f1e44490217a0fcc0cd04f52fb2144255c66dee595b7a7805074c6b.
Candidate native SHA256: b2f08edc02580065dc9bf0166291bd381431ef5e8648275b7c5517f9e3f3d98c.

A separate instrumented build retained every clear. At power-of-two count prefixes, the cputlb translation unit saw all 1,024 small-map clears empty, 1,831/2,048 large-map clears empty, and all 4,096 clears empty for each Telegram cold case. Other samples also showed redundant clears. These are prefix counts, not complete totals or performance measurements; diagnostic timings are excluded.

## Correctness

ARM32/AArch64 native unit suites, callback/nested/timeout tests, store checks, RAM identity, randomized mapping lifecycle and subpage checks passed. A new private differential test passed 200 cycles per architecture of explicit TB request, repeated TLB flush, execution, TB removal/flush, CPU-context restore and mapping/protection reuse on both baseline and candidate.

The initial private test pretranslated straight-line code before configuring the execution end, then the baseline ran beyond that end and reported FETCH_UNMAPPED. The original source/failure was retained. The corrected test uses a return instruction and explicit link register to reach the end; both variants pass. This is a harness correction, not a claimed product fix. No new JUnit run is claimed.

## Fixed screening

Two ABBA/BAAB blocks, eight families, 64 cold and 64 long fresh JVMs. All 128 exits, expected order, affinity, native identities and unchanged runtime/fixture manifest checks passed. A/B share the same native resource slot/classpath; actual loader extraction paths remain random and witnessed. Telegram expected bytes are preverified outside timed JVMs. No concurrent build or profiling ran. No diagnostic data are pooled.

Ratios are descriptive geometric block estimates, not confidence intervals or universal speed guarantees. Speed ratios are baseline/candidate; RSS ratios are candidate/baseline. Two blocks are insufficient for equivalence or causal certainty, especially given prior identical-native noise.

| Family | Long work speed | Long process speed | Long RSS ratio | Cold process speed | Cold RSS ratio |
|---|---:|---:|---:|---:|---:|
| Map2 | 1.0707 | 1.0715 | 1.0090 | 1.0828 | 0.9921 |
| Map100 | 1.0925 | 1.0806 | 1.0036 | 0.9876 | 0.9864 |
| SG4K | 1.0189 | 1.0227 | 1.0024 | 1.0255 | 1.0816 |
| SG64K | 1.0174 | 1.0057 | 0.9925 | 0.9672 | 1.0166 |
| TT | 1.0081 | 1.0065 | 1.0070 | 1.0389 | 1.0141 |
| JNA64 | 1.0075 | 1.0068 | 0.9900 | 1.0569 | 1.0061 |
| Telegram32 | 1.0215 | 1.0247 | 0.9955 | 0.9731 | 1.0009 |
| Telegram64 | 1.0246 | 1.0236 | 0.9927 | 1.0234 | 0.9980 |

Equal-family long work/process estimates are 1.0323x/1.0299x; cold process is 1.0187x. Map work gains appear in both blocks (Map2 1.0747/1.0668; Map100 1.1361/1.0505), but cold SG4K RSS rises in both blocks, 1.1426/1.0239, aggregate +8.16%. Cold Telegram32 process is slower in both blocks, 0.9662/0.9800. SG64K cold process is also below one in both blocks. Some other estimates change direction between blocks.

## Decision

HOLD. Useful mapping-work signals do not override the observed cold-memory increase and uneven startup results. No extra repetitions solely to obtain acceptance, no production merge or binary publication, and no multiplication into previous cumulative speedups. The flag fitting existing padding does not prove process RSS neutrality; the cause of the observed RSS difference is unresolved.
