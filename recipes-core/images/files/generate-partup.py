#!/usr/bin/env python3
import sys
import os

# Argumente einlesen
BOARD         = sys.argv[1]
TOTAL_MB      = int(sys.argv[2])
OUTPUT_DIR    = sys.argv[3]  # Dies sollte nun dein pkg_work_dir sein
ROOTFS_BASE   = sys.argv[4]
SEED_FILENAME = os.path.basename(sys.argv[5]) # Nur Dateiname

# Dateinamen definieren
ROOTFS_FILENAME = f"{ROOTFS_BASE}.ext4"
BOOT_FILENAME   = "imx-boot"

# Partitionen berechnen
BOOT_SIZE   = 128
CONFIG_SIZE = 64
SNAP_SIZE   = 500
available_pool = TOTAL_MB - (BOOT_SIZE * 2 + CONFIG_SIZE + SNAP_SIZE + 10)
rootfs_size = available_pool // 2

# --- DYNAMISCHE DATEILISTE ERSTELLEN ---
# Wir listen alle Dateien im OUTPUT_DIR auf, die in die BOOT-Partition sollen.
# Wir schließen System-Files aus, die bereits in anderen Sektionen fest verplant sind.
exclude_from_boot = [BOOT_FILENAME, ROOTFS_FILENAME, SEED_FILENAME, "layout.yaml"]

boot_files_yaml = ""
if os.path.exists(OUTPUT_DIR):
    for filename in sorted(os.listdir(OUTPUT_DIR)):
        if filename not in exclude_from_boot and os.path.isfile(os.path.join(OUTPUT_DIR, filename)):
            boot_files_yaml += f"      - filename: {filename}\n"

# YAML Template zusammenbauen
yaml_template = f"""api-version: 1
disklabel: msdos
mmc:
  boot-partitions:
    enable: 1
    binaries:
      - input-offset: 0
        output-offset: 0
        input:
          filename: {BOOT_FILENAME}

raw:
  - input-offset: 0kiB
    output-offset: 32kiB
    input:
      filename: {BOOT_FILENAME}

clean:
  - offset: 7168kiB
    size: 64kiB
  - offset: 7296kiB
    size: 64kiB

partitions:
  - label: boot0
    type: primary
    filesystem: fat32
    size: {BOOT_SIZE}MiB
    offset: 8MiB
    input:
{boot_files_yaml}
  - label: boot1
    type: primary
    filesystem: fat32
    size: {BOOT_SIZE}MiB
    input:
{boot_files_yaml}
  - label: config
    type: primary
    filesystem: ext4
    size: {CONFIG_SIZE}MiB

  - label: root0
    type: logical
    filesystem: null
    size: {rootfs_size}MiB
    block-size: 4kiB
    input:
      - filename: {ROOTFS_FILENAME}

  - label: root1
    type: logical
    filesystem: null
    block-size: 4kiB
    size: {rootfs_size}MiB
    input:
      - filename: {ROOTFS_FILENAME}

  - label: snapdata
    type: logical
    filesystem: ext4
    size: {SNAP_SIZE}MiB
    input:
      - filename: {SEED_FILENAME}
"""

file_name = "layout.yaml"
file_path = os.path.join(OUTPUT_DIR, file_name)

with open(file_path, "w") as f:
    f.write(yaml_template)

print(f"SUCCESS: {file_name} created. Included {boot_files_yaml.count('filename')} files in BOOT partitions.")
