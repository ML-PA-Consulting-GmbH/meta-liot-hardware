#!/usr/bin/env python3
import sys
import os

BOARD = sys.argv[1]
TOTAL_MB = int(sys.argv[2])
WORKDIR = os.getenv("WORKDIR")
# Fixe Partitionen (MB)
BOOT_SIZE = 128
CONFIG_SIZE = 55
SNAP_SIZE = 500

# Rootfs A/B proportional
rootfs_total = TOTAL_MB - (BOOT_SIZE*2 + CONFIG_SIZE + SNAP_SIZE)
rootfs_size = rootfs_total // 2

# Startadressen
boot0_start = 0
boot1_start = boot0_start + BOOT_SIZE
config_start = boot1_start + BOOT_SIZE
rootfsA_start = config_start + CONFIG_SIZE
rootfsB_start = rootfsA_start + rootfs_size
snap_start = rootfsB_start

# XML Partup erzeugen
xml = f"""<?xml version="1.0"?>
<partitions>
    <partition name="boot0" start="{boot0_start}MB" size="{BOOT_SIZE}MB" fstype="vfat"/>
    <partition name="boot1" start="{boot1_start}MB" size="{BOOT_SIZE}MB" fstype="vfat"/>
    <partition name="config" start="{config_start}MB" size="{CONFIG_SIZE}MB" fstype="ext4"/>
    <partition name="rootfs_ext" type="extended" start="{rootfsA_start}MB" size="{rootfs_size*2 + SNAP_SIZE}MB">
        <logical name="rootfsA" start="{rootfsA_start}MB" size="{rootfs_size}MB" fstype="ext4"/>
        <logical name="rootfsB" start="{rootfsB_start}MB" size="{rootfs_size}MB" fstype="ext4"/>
        <logical name="snapdata" start="{snap_start}MB" size="{SNAP_SIZE}MB" fstype="ext4"/>
    </partition>
</partitions>
"""

out_file = f"${WORKDIR}/{BOARD}.partup"
with open(out_file, "w") as f:
    f.write(xml)

print(f"{out_file} erzeugt! Größe={TOTAL_MB}MB, RootfsA/B={rootfs_size}MB")