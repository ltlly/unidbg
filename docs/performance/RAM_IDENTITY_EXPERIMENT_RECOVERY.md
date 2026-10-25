# Experimental RAM identity correction (not accepted)

2026-10-25 recovery checkpoint, based on ab9400b124b699f56518f87b347777866f25ce4b.
This branch is an experiment, not the validated performance release. Overall 20x
speedup has not been achieved. No new precompiled native library is shipped here.

The pre-free allocator used the last RAMBlock's end despite the list being
ordered by size. The post-free gap search could align a candidate into another
live block and then ignore that containing block. Distinct live backing ranges
could therefore receive overlapping internal identities, affecting code-cache
invalidation independently of their distinct guest and host addresses.

The patch computes a checked maximum live end before frees; after frees it
checks addition/alignment overflow and rejects candidates contained in live
blocks. It adds no cache or retained state and does not bypass invalidation,
permissions, hooks, snapshots or self-modifying-code checks. No workload names,
input values or expected results select a fast path.

## Fresh recovery evidence

An environment rollback removed the earlier private candidate, profiles and
raw performance results. Historical reported metrics are not substituted for
recoverable raw evidence. This reconstruction has a different binary hash and
requires new validation; do not inherit prior speed or memory acceptance.

Fresh Linux x86_64 checks:
- Published baseline native SHA256: 27fcc8f9c7c6bf3708fd5fc4943941d008ffa493bde49283dff80e9689d32dfa.
- Reconstructed candidate SHA256: f1dee95878593f811ce696ba1dcf21bafa3277112540c3b132e459c54dae0ade.
- Source comparison against upstream 2.1.4 plus published patches 0001–0010
  differs only in qemu/exec.c by this patch.
- ARM32/ARM64 allocation and reuse reproducer: 288 overlapping-pair observations
  on the baseline, zero on candidate. Observations are not unique collisions.
- ARM and ARM64 native unit suites pass on both versions.
- Exit callback matrix and guest-store differential checks pass on both;
  nested callbacks, protect/delete/stop write-hook cases match.
- Sixteen fresh JVM smoke runs (eight sample families on both versions) pass,
  with runtime native SHA witnesses and existing output checks.
- Maven test-compile succeeds; it is not a claim that all Java tests ran.

Current-page unmap/remap inside a write hook remains an earlier documented
baseline limitation, not certified safe by this checkpoint. Additional lifecycle,
alias/snapshot, numeric-boundary, Java regression and performance/RSS checks are
pending. No fresh speed or memory claim is made.

New test code, samples, binaries and raw logs remain outside Git as requested.
The published perf/validated-optimization branch and bundled native are unchanged.
