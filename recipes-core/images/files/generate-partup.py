#!/usr/bin/env python3
import sys
import os

# This script generates a YAML file for partitioning an SD card for the given board.
# It takes the following command-line arguments:
BOARD       = sys.argv[1]
TOTAL_MB    = int(sys.argv[2])
OUTPUT_DIR  = sys.argv[3]
ROOTFS_BASE = sys.argv[4]
SEED_FILENAME = sys.argv[5]

 # Define filenames for the input files
ROOTFS_FILENAME = f"{ROOTFS_BASE}.rootfs.ext4"
KERNEL_FILENAME = "Image"
DTB_FILENAME    = "oftree"
BOOT_FILENAME   = "imx-boot"

# Partition sizes in MiB
BOOT_SIZE   = 128
CONFIG_SIZE = 55
SNAP_SIZE   = 500

# Calculate the available space for the root filesystem partitions
available_pool = TOTAL_MB - (BOOT_SIZE * 2 + CONFIG_SIZE + SNAP_SIZE + 10)
rootfs_size = available_pool // 2

# Generate the YAML content for the partition layout
yaml_template = f"""api-version: 1
disklabel: gpt

mmc:
  boot-partitions:
    enable: 1
    binaries:
      - input-offset: 0
        output-offset: 0
        input:
          filename: {BOOT_FILENAME}

partitions:
  - label: BOOT0
    type: primary
    filesystem: fat32
    size: {BOOT_SIZE}MiB
    offset: 4MiB
    input:
      - filename: {KERNEL_FILENAME}
      - filename: {DTB_FILENAME}

  - label: BOOT1
    type: primary
    filesystem: fat32
    size: {BOOT_SIZE}MiB
    input:
      - filename: {KERNEL_FILENAME}
      - filename: {DTB_FILENAME}

  - label: CONFIG
    type: primary
    filesystem: ext4
    size: {CONFIG_SIZE}MiB

  - label: ROOT0
    type: primary
    filesystem: null
    size: {rootfs_size}MiB
    input:
      - filename: {ROOTFS_FILENAME}

  - label: ROOT1
    type: primary
    filesystem: null
    size: {rootfs_size}MiB
    input:
      - filename: {ROOTFS_FILENAME}

  - label: SNAPDATA
    type: primary
    filesystem: ext4
    size: {SNAP_SIZE}MiB
    input:
      - filename: {SEED_FILENAME}
"""
# Write the generated YAML content to a file
file_name = "layout.yaml"
file_path = os.path.join(OUTPUT_DIR, file_name)

with open(file_path, "w") as f:
    f.write(yaml_template)

print(f"SUCCESS: {file_name} was created for {BOARD}.")
