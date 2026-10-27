# Snapshot root identity: bounded correctness validation

Source-only experiment over RAM identity baseline f77f9a2. None of the held jump-cache, clock or duplicate-moveout performance patches is included. Production remains unchanged. Overall 20x performance is not achieved and this report makes no speed claim.

## Changes

The public mapping index tracks complete absolute root mappings, rather than relative-address COW children. Container conversion preserves the original root's priority and permissions. Moveout removes the indexed root. Restore filters newer layers before publishing the surviving root, discards mappings born after the target snapshot, and keeps earlier unmap-level sentinels intact.

FlatView growth allocates a temporary buffer before replacing the old one. A recoverable allocation failure preserves its previous ranges/count/dispatcher. Context restore restores its previous snapshot level and JIT state on that error.

Before changing restore state, preflight identifies precisely the roots surviving the existing shallow filter, checks active/queued and queued/queued overlap, and reserves enough mapping-index capacity. Registration then uses a no-allocation insertion helper. Standard realloc preserves the old index on failure; the active compatibility allocator's free-on-failure behavior is not used at this call site. No global allocator semantic change is made. Ordinary map insertion rejects counts beyond the signed binary-search bound. Overlap comparisons handle a legal mapping ending exactly at 2^64. Capacity rounding uses the source MEM_BLOCK_INCR (32), with overflow guards.

## Fresh final checks, October 27

Final Linux x86_64 native SHA256: 6e952301e5165a6e013fbdb1815020b5f9564877a72d30a9830de1cfeb16b03e.

- ARM32/ARM64 native unit suites, callback/nested/timeout/store checks, RAM identity/lifecycle and subpage checks pass.
- Twelve public-API snapshot cases (two architectures, 1/2/16 regions, COW/non-COW) each pass 200 restoration rounds with exact byte checks.
- Per architecture, 100 nested snapshot cycles with three inner restores pass, checking absolute mapping enumeration, overlap refusal, and removal of post-snapshot roots.
- Linker-wrapped calloc failure tests pass for growing a saved context and restoring a larger context. They verify NOMEM, preserved state/context, and successful retry.
- Linker-wrapped realloc failure before restore passes with the old index pointer/count/level/queue sentinel and current visibility preserved; retry succeeds.
- Explicit internal conflict injections exercise both active/queued and queued/queued preflight refusal before mutation, then successful retry after removing the injected inconsistency. These guard tests are not claims of a real-world conflicting valid history.
- A real ARM64 mapping ending at 2^64 exercises end-wrap overlap refusal and retry.
- Eight ordinary Java workload smoke runs pass with actual loaded native hashes; this is not a timing comparison or full JUnit run.
- AddressSanitizer and UndefinedBehaviorSanitizer runs pass for nested snapshots on both architectures and COW restore at 16 regions x200 rounds and 128 regions x100 rounds. No sanitizer diagnostics occurred in those runs.

LeakSanitizer initially could not operate in the tracing environment and exited with a runtime limitation. Those failures are preserved. The address/undefined-behavior checks were then run with leak detection disabled, explicitly as separate checks. No leak-free claim is made. Earlier lost working-directory results are not substituted for these fresh records.

## Scope and remaining release gates

Independent source review found no further concrete blocker in the tested snapshot normal, recoverable calloc/realloc, and overlap-preflight paths. This is not proof of global OOM safety: general uc_mem_map publishes a region before index registration, and other compatibility-allocation callers retain their existing behavior. Restoring a newer memory context after its versions were discarded by an older context is not certified. Non-ARM target builds, broad performance/RSS non-regression, a complete JUnit run and leak checking remain outside this checkpoint.

Status: validated within the stated correctness scope, held from production release pending broader gates. Tests, fixtures, binaries and raw logs remain outside Git. No native replacement or main-branch merge is performed.
