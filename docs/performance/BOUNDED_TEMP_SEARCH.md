# Bounded TCG free-temp search: experimental candidate

Status: correctness screening passed within the scope below; performance measurement pending. Not accepted for production, not a 20x claim.

The candidate searches the existing free-temp bitmap only below nb_temps and changes the success predicate to the same bound. tcg_func_start clears all free bitmaps when resetting nb_temps; allocation only increases it; freeing sets only an allocated temp's bit. Thus the smallest free index is unchanged. The no-match sentinel changes, but still selects the original allocation path. No new fields, caches or retained allocations are added.

Baseline is the experimental snapshot-root repair checkpoint 9adeb707458f23d73112e75eeee183dd33c04914, whose independent performance status remains HOLD. This is an isolated incremental experiment, not an upstream comparison or production integration.

Baseline native SHA256: 6e952301e5165a6e013fbdb1815020b5f9564877a72d30a9830de1cfeb16b03e.
Candidate native SHA256: 3bc6ff07c69f2a9d0193698409fb904aeb24b4e5c9590eb40a819ad6c40b09b7.

Validation completed: eight representative serial dual-search diagnostic JVMs selected identical normalized indices on every observed allocation; eight candidate smoke JVMs passed; 1,004,666 actual 64-bit-header bitmap comparisons passed, covering bounds 0 through 512, every valid single bit, randomized sets and clearing. This does not claim 32-bit-host execution or a complete application oracle for every sample. Diagnostic counters are local-only and excluded from this patch.

Observed allocation counts ranged from 26,965 to 261,047 per diagnostic process; no-free-item counts ranged from 6,942 to 59,914. ARM bounds stayed within the first 64 bits; AArch64 occasionally used a second word. Bounds are not actual word-read counts: searches stop early on a hit.

Existing x86-64 host code changes from unrolled eight-word checks to a dynamic loop. Each ARM/AArch64 function shrinks from 0x15b to 0x11d bytes, but gains dynamic-bound checks and register save/restore on reuse paths. Therefore fewer possible reads do not establish a speedup.

A fixed two-block ABBA/BAAB screen of eight workload families, 64 cold and 64 long JVMs, is in progress. Identical classpath, fixed native resource slot, loaded-library hash witnesses, affinity [0,1], immutable runtime manifest, no concurrent build/profiler, and no extra data-dependent repeats. Results will be retained including regressions. No binary, test/sample code or raw logs are included in Git.
