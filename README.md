# Computer Networks & Systems Labs

> *Educational project built during university.*

A collection of network programming implementations in Java focusing on custom transport protocols, P2P network topologies, UDP reliability mechanisms, and socket-level data transfer.

## 📦 Projects Overview

### 1. TreeChat (P2P Mesh Network over UDP)
A peer-to-peer chat application built on top of UDP datagrams that dynamically organizes nodes into a tree structure.
* **Fault Tolerance:** Implements message acknowledgments (ACKs), retransmissions, and unconfirmed message tracking.
* **Network Rebuilding:** Supports automatic topology rebuilding and deputy node promotion when a parent node disconnects or drops offline.
* **Packet Simulation:** Includes test streams for simulating packet loss (`PacketStreamWithLosses`) and corruption (`PacketStreamWithCorruption`).

### 2. Reliable TCP File Transfer (`networklab2`)
A client-server application for reliable file transmission over TCP sockets.
* **Custom Protocol Headers:** Manual binary encoding/decoding of custom application headers (`HeaderConverter`, `HeaderInfo`).
* **Integrity & Performance:** Includes checksum calculations (`CheckSumCounter`) and real-time transfer speed/time tracking (`TimeTracker`).

### 3. Multicast Discovery (`lab1`)
* A utility for detecting and tracking active application instances on a local network segment using IP Multicast.

## 🛠️ Concepts & Technologies

* **Languages:** Java
* **Networking & Protocols:** Sockets (TCP/UDP Datagrams), IP Multicast, Custom Packet Headers
* **Distributed Systems Concepts:** Dynamic Topology Rebuilding, P2P Routing, Heartbeats/Pings, Reliability over Lossy Networks
* **Build Tool:** Maven
