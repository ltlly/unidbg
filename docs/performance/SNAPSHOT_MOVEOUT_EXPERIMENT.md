# Snapshot moveout duplicate rebuild: held, narrow-scope experiment

This source-only experiment removes only the redundant forced pending flag after memory_moveout's root del_subregion commit. The outer begin/commit stays balanced; no pending topology means the latter commit does no further rebuilding. Original first publication, mapping bookkeeping, invalidation and TLB behavior remain. It changes no ordinary mapping or guest workload-specific behavior.

An initial candidate removed the corresponding forced rebuild from memory_movein too. That version is rejected: its first commit can use exact-range FlatView replacement, whereas its second root commit fully rerenders topology. During snapshot restoration, a copied saved FlatView can contain not-yet-reattached ranges, so those operations are not automatically equivalent. The movein change was restored before timing. Initial source and failure records are preserved privately.

Baseline native SHA256 a87c36222f1e44490217a0fcc0cd04f52fb2144255c66dee595b7a7805074c6b; moveout-only candidate 6241970e1fa47f414ff110330b782ff3b218449c01daf7f6df340d68f65aa698.

## Validation and pre-existing failure

ARM/AArch64 native units, callback/store checks, RAM-identity lifecycle and subpage tests pass. No new JUnit claim. A new focused public-API test saves a memory snapshot, unmaps alternating complete regions, restores it and checks all original bytes. Non-COW forms pass both architectures.

The separate COW-write + unmap + restore form fails on both baseline and initial candidate: a one-region case reports READ_UNMAPPED after restoration, and larger cases report heap corruption. This failure is preserved and not recast as a candidate pass. Source review found that subregion priorities are descending while moveout/restore incorrectly assume the first child is the original block; filtering can free the selected COW pointer before re-registration. Also, moving a container back before filtering can expose soon-freed COW regions in the dispatcher. These are source-based hypotheses consistent with the reproducer, not a claimed completed fix or sanitizer proof.

## Narrow performance screen

32 separate processes: ARM32/ARM64, 16 regions x1000 rounds or128 regions x200 rounds, two ABBA/BAAB blocks. Only the passing non-COW snapshot-unmap/restore sequence is timed. A/B use one fixed native library path and executable; each child reports the resolved uc_open library path, and the wrapper pins the file hash before/after. All exit and exact checksum checks pass. No concurrent builds/profilers.

Descriptive work-speed ratios (baseline/candidate):
- ARM32,16 regions:1.3126x; blocks1.3629/1.2641; RSS candidate/baseline1.0075.
- ARM32,128 regions:1.3137x; blocks1.3586/1.2703; RSS1.0000.
- ARM64,16 regions:1.2771x; blocks1.2501/1.3047; RSS1.0000.
- ARM64,128 regions:1.3183x; blocks1.3707/1.2679; RSS0.9962.

These results apply only to this constructed snapshot-heavy path, not the eight ordinary Java workloads or overall unidbg throughput. Two blocks provide no confidence or equivalence guarantee. They are not pooled with any other experiment and cannot establish20x.

Decision:HOLD from production while snapshot correctness remains unresolved. Preserve this useful narrow signal without weakening failure-state or lifecycle semantics. No native binary, tests, samples or raw logs are published in Git; the validated branch is unchanged.
