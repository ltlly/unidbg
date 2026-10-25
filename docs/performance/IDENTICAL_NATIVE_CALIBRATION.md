# Identical-native A/A measurement calibration

This is measurement calibration, not an optimization. Exactly one fixed 64-cold plus 64-long JVM run across eight families, two ABBA/BAAB blocks, retained all observations. Both labels load byte-identical a87c36222f1e44490217a0fcc0cd04f52fb2144255c66dee595b7a7805074c6b native libraries, identical classes and fixtures. All 128 records, outputs, order, native witnesses, CPU affinity and runtime hashes audited.

The labels have no intended code difference, yet their block-geometric mean A/B ratios varied:
- Cold process: Map2 1.0550, Map100 1.0229, SG4K 0.9362, SG64K 0.9101, TT 0.9705, JNA64 1.0493, TG32 1.0012, TG64 0.9965.
- Long work: Map2 1.0185, Map100 1.0428, SG4K 0.9892, SG64K 0.9881, TT 1.0210, JNA64 1.0154, TG32 1.0296, TG64 0.9985.
- Long peak-RSS ratios included TG32 1.0377 and TG64 0.9680.
- Equal-family process ratios were 0.9915 cold and 1.0079 long.

Even the long Map100 difference was positive in both blocks (1.0558 and 1.0300), despite identical code. Two positive blocks are not sufficient causal evidence. These measurements do not prove all earlier regressions were noise, establish a universal noise bound, or justify subtracting A/A ratios from separate candidate runs. The control is not pooled with pilot/formal experiments and does not retroactively accept any held patch.

Interpretation: small code-performance differences observed in the preceding experiments should be described as adverse or favorable measurements, not confidently attributed to the patch. The bounded-compaction Map2 gain reproduced near 16% in pilot and independent formal runs, which remains promising evidence, but broader acceptable performance/memory has not been established. Existing source-only experiment status remains HELD; the validated branch and bundled native are unchanged.

Next engineering decisions must distinguish structural mechanism evidence from noisy end-to-end process measurements. More observations alone, without a fixed question and appropriate controls, are not an acceptance strategy. No overall 20x claim is supported. New calibration harnesses, raw logs, fixtures and binaries stay outside Git.
