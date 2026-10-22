# [unicorn2](https://github.com/unicorn-engine/unicorn) backend

ARM emulator backend based on [Unicorn Engine 2](https://github.com/unicorn-engine/unicorn) (upstream `2.1.4` plus the patches in `src/main/native/patches/`). Supports emulating ARM32/ARM64 native libraries on macOS, Linux and Windows.

## Supported Platforms

| Platform | Architecture | Output |
|----------|-------------|--------|
| macOS | ARM64 | `osx_arm64/libunicorn.dylib` |
| macOS | x86_64 | `osx_64/libunicorn.dylib` |
| Linux | x86_64 | `linux_64/libunicorn.so` |
| Linux | ARM64 | `linux_arm64/libunicorn.so` |
| Windows | x86_64 | `windows_64/unicorn.dll` |

## Prerequisites

### macOS Native Build

- Xcode Command Line Tools
- JDK (`JAVA_HOME` environment variable must be set)
- `pkg-config` (`brew install pkgconf`), required by unicorn's `qemu/configure`
- Unicorn source is cloned and patched automatically by `build.sh`: the upstream tag `2.1.4` is
  cloned into `~/git/unicorn-2.1.4` and `patches/*.patch` are applied. `build.sh` refuses to build a
  tree that is not exactly upstream `2.1.4` plus these patches.

#### Why the patch

`patches/0001-apple-jit-state-for-hooks-in-foreign-runtimes.patch` only changes behaviour on Apple
Silicon. Unicorn 2.1.x caches the per-thread `MAP_JIT` write protection, skips
`pthread_jit_write_protect_np()` when the cache already matches, and restores the caller's state only
when the outermost API call returns, but HotSpot toggles the same state on every JNI transition. A
`uc_*` call made from a Java hook (e.g. `munmap` in a syscall handler, or a nested `emu_start`) then
patches translated code with the thread still in execute mode and dies with `SIGBUS` in
`do_tb_phys_invalidate`; and once that is avoided, the nested call returns to Java in write mode and
the JVM faults on its own code. The patch always applies the requested state and hands the thread
back in its entry state on every nesting level. Drop it once upstream unicorn ships an equivalent fix.

### Docker Cross-Compilation (Linux / Windows)

- [Docker](https://www.docker.com/) (with `docker buildx` multi-platform support)

Docker clones the upstream unicorn tag from GitHub, applies `patches/` and compiles it inside the container.

## Build

All builds are performed via a single `build.sh` script located in `src/main/native/`:

```bash
cd backend/unicorn2/src/main/native

# Build all platforms
./build.sh all

# Build macOS only (ARM64 + x86_64)
./build.sh osx

# Build a single macOS target
./build.sh osx_arm64
./build.sh osx_64

# Build all Docker targets (Linux + Windows)
./build.sh docker

# Build a single Docker target
./build.sh linux_64
./build.sh linux_arm64
./build.sh windows_64

# Clean build (rebuild from scratch)
./build.sh --clean osx
```

Set the `UNICORN_HOME` environment variable to specify a custom unicorn source path (defaults to `~/git/unicorn-2.1.4`):

```bash
UNICORN_HOME=/path/to/unicorn ./build.sh osx_arm64
```

Build artifacts are placed in `src/main/resources/natives/<platform>/`.

## Scalar memory access

The JNI binding provides optional scalar-memory APIs for little-endian 1, 2,
4, and 8 byte values. They use the same Unicorn memory access checks as
`mem_read` and `mem_write`, without allocating a Java byte array for each value.
`Unicorn.hasScalarMemoryAccess()` reports native scalar-read support (API v1),
and `Unicorn.hasScalarMemoryWriteAccess()` reports scalar-write support (API v2).

The bundled Linux x86_64 library includes both optimizations. Older native
libraries, including other bundled platform libraries until rebuilt, remain
compatible: the Java binding detects the optional API version and falls back to
byte-array access for unsupported operations. Rebuilding a platform with the
commands above enables both scalar APIs on that platform. No instruction tracing
or memory validation is turned off by these optimizations. System-write tracing
and custom `onSystemWrite` observers continue receiving byte-array callbacks;
only the unobserved default path avoids the notification allocation.

## Bounded bulk JNI transfers

Bulk reads and writes of 1 through 4096 bytes use a per-call native stack
buffer with JNI array-region transfers instead of an acquired array-elements
buffer. Larger transfers retain the existing path. This avoids holding a pinned
Java array across Unicorn operations and keeps the same guest-memory checks,
callbacks and cache behavior. The rebuilt Linux x86_64 library includes this
path; other platform libraries need rebuilding to enable it.

The native bulk-read entry point rejects lengths outside `0..Integer.MAX_VALUE`
before allocating a Java array, preventing truncation of the destination size.

## Read-only host/API writes

The `0002` engine patch avoids rebuilding the guest memory topology for host
writes to already-readonly, ordinary root RAM mappings. It keeps full-range
validation, FlatView/RAMBlock clipping, and pre/post TLB and jump-cache flushes.
It does not grant guest write permission or automatically invalidate translated
blocks. Writable memory, MMIO, snapshot/COW mappings, and initially inconsistent
pointer-backed readonly state retain the legacy path.

This is a workload-specific optimization, not a general speed multiplier.
Read-only-write microbenchmarks benefit strongly as mapped-region counts grow;
complete application timings remain mixed, and individual writable-memory
microbenchmarks can be slightly slower. The bundled Linux x86_64 library has
been rebuilt with the patch. Other platforms must rebuild from upstream 2.1.4
plus the complete `patches/` set; they have not been runtime-validated here.

## Bounded private RAM backing reuse

The `0003` engine patch reuses up to 128 internally owned host RAM allocations
per emulator. Total admission is capped at 1 MiB, charging each payload plus one
host page for allocator/guard overhead. Non-host-page-sized allocations retain a
separate 64 KiB admission limit; individual payloads cannot exceed 64 KiB. Fixed
cache metadata is additional. These limits are not a universal bound on
platform-specific virtual-address reservation overhead.

The occupied entries form a dense prefix. Removal swaps the final entry into
the vacated slot, so a burst of same-sized pages can reuse its first matching
entry without scanning empty slots. Only exact-size buffers are reused, with
their original alignment restored and all payload bytes cleared before reuse.
Caller-owned `mem_map_ptr` buffers are never admitted. Every retained allocation
is released during emulator close, including when memory snapshots have held
unmapped regions until late teardown.

This does not keep freed guest mappings alive or cache RAMBlock/MemoryRegion
objects. Normal map/unmap validation, permissions, TB/TLB invalidation, partial
unmaps and snapshot/COW ownership remain in place. Public host reads/writes and
guest accesses to an unmapped guest address still fail.

Linux x86_64 paired fresh-process checks of this larger page pool versus the
previous eight-entry implementation found a 100-entry Map-to-JNI signing workload
about 24% faster in its operation phase and 23% faster end to end. A small-map
workload improved about 4%; peak RSS was essentially unchanged for both. Long
security-library encryption/decryption checks were near neutral to slightly
faster, with peak RSS changes around 1%. Fifteen paired cold-start repetitions
per workload were near neutral. Early sparse-cache experiments with slower
security-library samples were rejected rather than shipped.

This is not an overall 20x claim. ARM32/ARM64 dense ownership/accounting, zeroing,
mixed-size reuse, permissions, unmapped accesses, executable remapping,
caller-owned buffers, partial unmaps, memory snapshots and close/leak checks
passed, along with the Java regression checks. Other platform binaries have not
been rebuilt or runtime-validated here.
