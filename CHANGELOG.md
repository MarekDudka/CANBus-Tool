# Changelog

All notable changes to CANBus-Tool are documented here.

## 1.0.0

- Linux desktop app (Kotlin, Compose Multiplatform) for CAN adapters via SocketCAN (`can0`,
  `vcan0`, …): live message viewer, id/type filtering, recording and CSV export.
- `socketcan` module: raw `AF_CAN` sockets through the OS's own libc, no vendor library or driver.
- Unified Trace view: RX and TX messages interleaved chronologically in one table with a Dir
  column, filterable by id range and frame kind. Filter and recording controls only shown while
  the Trace tab is active.
- Overview tab: live per-id stats (latest payload, count, cycle time) for RX and TX separately.
- Send a message manually or configure cyclic (periodic) TX messages via right-click on the TX
  area, each independently switchable on/off.
- CSV export of a recorded session (Start/Stop/Clear/Export).
- Help → Set up CAN interface…: the commands to bring up a PEAK PCAN-USB as `can0`.
- Packaging: `.deb` via Compose Desktop's Gradle plugin, plus an `.AppImage` via
  `scripts/build-appimage.sh`.
