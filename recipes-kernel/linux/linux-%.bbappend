FILESEXTRAPATHS:prepend := "${THISDIR}/files:"

# Füge das Fragment zur SRC_URI hinzu
SRC_URI += "file://ftpm.cfg"

# QEMU ARM64 spezifische Kernel-Konfiguration (virtio, etc.)
SRC_URI:append:generic-arm64 = " file://qemu-arm64.cfg"
