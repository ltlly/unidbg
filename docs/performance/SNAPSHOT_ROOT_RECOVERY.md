# Snapshot root identity repair: experimental recovery checkpoint

This is a correctness investigation, not an accepted performance optimization. Based on the independent RAM-identity source baseline. None of the held jump-cache, clock-elision or snapshot moveout performance patches is included. Production remains unchanged; overall 20x is not achieved.

## Invariant

The public mapping index refers to absolute root mappings, not container-relative COW children. Converting a RAM mapping to a container replaces the index entry with that root and retains permissions and birth priority. Snapshot moveout removes the root from the index. Restore keeps earlier detached-level sentinels intact, filters newer layers before publishing topology, discards roots created after the target snapshot, and registers the surviving root rather than a potentially removed child.

## Fresh evidence after interruption

The earlier unpublished working directory and its tests were lost. Historical test claims are not substituted for fresh verification. Source was reconstructed, archived and fully rebuilt on October 27.

Initial recovered static archive SHA256: fb7c502f9dcb3f0699b7a5990b8f0e1839ad239586077b8874e17a7d458eba20.

Fresh checks pass:
- ARM32 and ARM64 native unit suites.
- Callback/nesting/timeout and store checks, RAM identity/lifecycle and subpage checks.
- Twelve public-API snapshot cases: ARM32/ARM64 x 1/2/16 regions x COW/non-COW, each 200 restore rounds with exact byte checks.
- Per architecture, 100 nested-snapshot cycles with three repeated inner restores, absolute mapping enumeration, overlap rejection and removal of roots born after the restored snapshot.

These checks do not establish comprehensive memory safety. No sanitizer completion, fresh JUnit suite, other-architecture coverage or performance benefit is claimed. Source/tests/logs are separate; only source patch and this report enter Git.

## Remaining blockers

Restore allocation-failure and overlap-error partial-state consistency remains unresolved. The current algorithm copies the saved FlatView before restoring topology; an early error may leave a view inconsistent with the actually restored tree. Register-allocation failure is returned and queue ownership retained, but that alone does not prove visibility consistency. Allocation-failure injection and independent review are still required. The lifetime of restoring a newer context after discarding its versions through an older context is not certified.

Decision: HOLD. This checkpoint makes recovered source and limitations durable; it is not ready for production merge or binary replacement.

## Follow-up: recoverable FlatView allocation failure

The recovered candidate now allocates a replacement ranges buffer before releasing the old one. If that allocation fails, the old ranges/count/dispatcher stay untouched. A failed context restore also restores the previous snapshot level and executes the existing JIT-state restoration path.

Fresh linker-wrapped calloc failure tests on ARM32 and ARM64 pass for both growing an existing saved context and restoring a larger context into a smaller current view. They check the exact NOMEM return, unchanged current visibility/snapshot level, preserved saved context, and successful retry. Native suites and nested/region tests were rerun successfully after this change. This narrowly validates those allocation exits, not all allocations or sanitizers.

Important source correction: glib_compat.c contains disabled allocator examples. The active gmem.c g_realloc returns NULL and frees the old allocation on a failed nonzero realloc. Therefore mem_map failure is a real remaining issue, not an abort-only path. It and overlap partial-state recovery still need resolution. HOLD remains.
