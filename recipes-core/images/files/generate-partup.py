#!/usr/bin/env python3
import sys
import os

# Argumente einlesen
BOARD = sys.argv[1]
TOTAL_MB = int(sys.argv[2])
OUTPUT_DIR = sys.argv[3]

# Fixe Partitionen (MiB)
BOOT_SIZE = 128
CONFIG_SIZE = 60
SNAP_SIZE = 600

# Rootfs A/B proportional berechnen
# (Wir lassen am Ende oft ein paar MB Puffer für die GPT-Tabelle)
rootfs_total = TOTAL_MB - (BOOT_SIZE * 2 + CONFIG_SIZE + SNAP_SIZE + 4)
rootfs_size = rootfs_total // 2

# YAML Partup erzeugen
# Hinweis: 'image' Parameter sind Platzhalter, die das partup-Tool 
# in Yocto meist automatisch mit den .img/.bin Dateien füllt.
yaml_content = f"""
# Partup Konfiguration für {BOARD}
disk:
  device: mmcblk0
  size: {TOTAL_MB}MiB

partitions:
  - name: boot0
    type: primary
    fstype: vfat
    size: {BOOT_SIZE}MiB
  - name: boot1
    type: primary
    fstype: vfat
    size: {BOOT_SIZE}MiB
  - name: config
    type: primary
    fstype: ext4
    size: {CONFIG_SIZE}MiB
  - name: rootfsA
    type: primary
    fstype: ext4
    size: {rootfs_size}MiB
  - name: rootfsB
    type: primary
    fstype: ext4
    size: {rootfs_size}MiB
  - name: snapdata
    type: primary
    fstype: ext4
    size: {SNAP_SIZE}MiB
"""

# Datei schreiben (Endung .partup ist Standard bei Phytec)
file_name = f"{BOARD}.partup"
file_path = os.path.join(OUTPUT_DIR, file_name)

with open(file_path, "w") as f:
    f.write(yaml_content.strip())

print(f"SUCCESS: {file_path} erzeugt!")
print(f"Layout: Rootfs A/B je {rootfs_size}MiB, Snapdata {SNAP_SIZE}MiB")