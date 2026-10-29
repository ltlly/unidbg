# Snapshot root repair: recovered ordinary-workload screen (HOLD)

This is a fresh complete October 29 screen of source checkpoint 46b58802e9fd0779d5bd8adcc22d62c03cf24ca4, not a new optimization or a cumulative upstream comparison. The interrupted October 27 raw screen was lost and is not pooled, imputed, or treated as passing. The recovered source rebuilt the byte-identical candidate library.

Baseline native SHA256: a87c36222f1e44490217a0fcc0cd04f52fb2144255c66dee595b7a7805074c6b.
Candidate native SHA256: 6e952301e5165a6e013fbdb1815020b5f9564877a72d30a9830de1cfeb16b03e.

## Protocol and checks

Eight ordinary Java workload families; 64 cold and 64 long fresh JVMs; two fixed ABBA/BAAB blocks. The long phase was run in two fixed 32-process invocations, with a pause only between completed blocks for durable preservation. No data-dependent ordering or extra acceptance repeats. Cold, long block0 and long block1 were each archived and read back. No concurrent build/profiler/heavy archive ran during timing.

All 128 exits, unique row keys, exact order, affinity [0,1], loaded-native hash witnesses, identical classpath and unchanged 1400-file runtime/fixture manifest checks pass. Telegram oracle bytes were preverified outside timed JVMs. A/B used the same native resource slot; actual native extraction paths remain random and witnessed. Eight fresh smoke processes also passed. These checks do not establish an independent business oracle for every sample: SG has completion checks, and large-map signing uses determinism rather than an independent business-output oracle.

Ratios below are geometric block estimates, not confidence intervals. Time ratios are baseline/candidate (>1 faster); RSS ratios are candidate/baseline (>1 more memory). Two blocks cannot establish broad equivalence or causally attribute small differences to the patch.

| Family | Long work | Long wall | Long RSS | Cold wall | Cold RSS |
|---|---:|---:|---:|---:|---:|
| map2 | 1.0256 | 1.0220 | 0.9957 | 0.9338 | 1.0019 |
| map100 | 0.9998 | 1.0097 | 0.9935 | 1.0484 | 0.9470 |
| sg4k | 1.0000 | 1.0006 | 1.0080 | 0.9565 | 0.9960 |
| sg64k | 1.0305 | 1.0258 | 0.9941 | 1.0148 | 1.0010 |
| tt | 0.9740 | 0.9697 | 1.0215 | 0.9440 | 1.0103 |
| jna64 | 1.0109 | 1.0046 | 0.9949 | 0.9812 | 1.0133 |
| tg32 | 1.0010 | 1.0022 | 0.9997 | 0.9412 | 0.9997 |
| tg64 | 1.0057 | 1.0120 | 1.0006 | 1.0021 | 0.9949 |

Equal-family long work is 1.0058x and long process wall 1.0057x, essentially neutral at this screen's resolution. Cold wall is 0.9770x (about 2.4% longer candidate time). Do not call the approximately 0.6% long estimate a reliable optimization gain.

Adverse evidence is retained:
- TT long wall: blocks 0.9521/0.9877; work 0.9508/0.9977; peak RSS ratio 1.0423/1.0011, aggregate 1.0215.
- Telegram32 cold wall: 0.9198/0.9632, aggregate 0.9412 (about 6.2% longer).
- SG4K and JNA64 cold wall are also slower in both blocks.
- Small-map and TT cold estimates are adverse overall but change direction between blocks; some other effects are likewise inconsistent.

Decision: HOLD from performance acceptance and production merge. The bounded snapshot correctness checks remain separate evidence; they do not prove ordinary-workload non-regression. No extra repeats solely to obtain a favorable result, no multiplication with earlier ratios, and no overall20x claim. The main branch and bundled native remain unchanged. Tests, samples, binaries and raw logs are excluded from Git as requested.

## Raw evidence hashes
- cold64.jsonl: 1db73d548e701b3e6a582c3659baa5cb53a3c8a368465975377f61b710698df9
- long-block0.jsonl: f86fbb340722a6dfb8eb6efff39df8e18b78c40ee1aa7344706799a03c2cf483
- long-block1.jsonl: b19740444adcfa54223142b84f0287e2896d6eb69cf9261c15469884d04338d7
- runtime-manifest.json: 74cdb0dbbe066b8147d6db736a703a198614c64c7bc00f5faeddd654870617b6
- formal-plan.json: 1bedc52201b5c5eff980f53bd0ec6ce4a2a7b1f9cba57199838cdabffb1b2061

## Cold-phase detail

These descriptive phase ratios are also baseline/candidate, without confidence bounds. Short phases are particularly noisy; they are retained rather than inferred from process totals.

- map2: load 0.8845x, onload 1.0199x, startup 0.9150x, warm 0.8738x.
- map100: load 1.0688x, onload 1.2078x, startup 0.9859x, warm 1.1409x.
- sg4k: app_init 0.8941x, load 0.8690x, onload 0.8553x, startup 1.0003x, warm 0.9817x.
- sg64k: app_init 1.0171x, load 1.0004x, onload 1.0388x, startup 0.9918x, warm 0.9987x.
- tt: close 0.8880x, load 0.9323x, onload 0.8640x, startup 0.9465x, warm 1.0350x.
- jna64: close 0.7018x, load 0.9709x, onload 0.9030x, startup 0.9944x, warm 0.8267x.
- tg32: close 0.9575x, load 0.9811x, onload 0.9585x, startup 0.9228x, warm 0.9683x.
- tg64: close 0.8237x, load 1.0154x, onload 0.9559x, startup 1.0276x, warm 0.9781x.
