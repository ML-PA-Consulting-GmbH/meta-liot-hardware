#!/usr/bin/env python3
import sys

BOARD = sys.argv[1]
total_mb = int(sys.argv[2])

# Fixe Partitionen
boot_size = 130
config_size = 60
snap_size = 600

# Rootfs A/B proportional berechnen
rootfs_total = total_mb - (boot_size*2 + config_size + snap_size)
rootfs_size = rootfs_total // 2

# Startadressen in MB
boot0_start = 0
boot1_start = boot0_start + boot_size
config_start = boot1_start + boot_size
rootfsA_start = config_start + config_size
rootfsB_start = rootfsA_start + rootfs_size
snap_start = rootfsB_start

# XML erstellen
xml = f"""<?xml version="1.0"?>
<partitions>
    <partition name="boot0" start="{boot0_start}MB" size="{boot_size}MB" fstype="vfat"/>
    <partition name="boot1" start="{boot1_start}MB" size="{boot_size}MB" fstype="vfat"/>
    <partition name="config" start="{config_start}MB" size="{config_size}MB" fstype="ext4"/>
    <partition name="rootfs_ext" type="extended" start="{rootfsA_start}MB" size="{rootfs_size*2 + snap_size}MB">
        <logical name="rootfsA" start="{rootfsA_start}MB" size="{rootfs_size}MB" fstype="ext4"/>
        <logical name="rootfsB" start="{rootfsB_start}MB" size="{rootfs_size}MB" fstype="ext4"/>
        <logical name="snapdata" start="{snap_start}MB" size="{snap_size}MB" fstype="ext4"/>
    </partition>
</partitions>
"""

with open(f"{BOARD}-partup.xml", "w") as f:
    f.write(xml)

print(f"{BOARD}-partup.xml erzeugt!")