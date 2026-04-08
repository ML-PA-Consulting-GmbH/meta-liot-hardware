SUMMARY = "edge yocto custom image with RAUC, snapd with partup packaging"
LICENSE = "CLOSED"

require recipes-images/images/phytec-headless-image.bb

#  Default values for variables, can be overridden by local.conf or machine configuration
BOARDNAME ?= "edge-imx93-yo"
EMMC_SIZE_MB ?= "7260"
DEPLOY_DIR_IMAGE_PATH = "${DEPLOY_DIR_IMAGE}"

IMAGE_FSTYPES += "partup"
IMAGE_INSTALL:append = " snapd"

# Add the generate-partup.py script to the source files for this recipe
SRC_URI += "file://generate-partup.py"

# This function will be called after the image is built to generate the partup package
python do_generate_partup_package() {
    import subprocess
    import os
    import shutil
    import glob

    # Pfade und Variablen vorbereiten
    board = d.getVar('BOARDNAME')
    size = d.getVar('EMMC_SIZE_MB')
    deploy_dir = d.getVar('DEPLOY_DIR_IMAGE_PATH')
    rootfs_link = d.getVar('IMAGE_LINK_NAME')
    seed_filename = "edge-imx93-yo-seed.tar.gz"
    yaml_config = "layout.yaml"
    package_output = f"{board}.partup.pkg"

    # 1. Temporäres Arbeitsverzeichnis für das Packaging erstellen
    pkg_work_dir = os.path.join(deploy_dir, "partup_tmp")
    if os.path.exists(pkg_work_dir):
        shutil.rmtree(pkg_work_dir)
    os.makedirs(pkg_work_dir)

    # 2. Dynamische Dateiliste erstellen
    # Wir nehmen die festen Dateien + alle .dtb, .dtbo und Boot-Files
    search_patterns = [
        "imx-boot",
        "Image",
        "oftree",
        "tee.bin",
        "bootenv.txt",
        "config-partition.tar.gz",
        f"{rootfs_link}.ext4",
        "*.dtb",
        "*.dtbo",
        "*.bin" # für die M33 Cores
    ]

    files_to_include = []
    for pattern in search_patterns:
        # Suche im deploy_dir nach dem Pattern
        found = glob.glob(os.path.join(deploy_dir, pattern))
        for f in found:
            # Wir nehmen nur die Dateinamen, keine Pfade
            files_to_include.append(os.path.basename(f))


    files_to_include.append(os.path.basename(seed_filename))

    # 3. YAML generieren (im deploy_dir, da das Skript es dort erwartet)
    script = bb.utils.which(d.getVar('FILESPATH'), 'generate-partup.py')
    try:
        subprocess.check_call(['python3', script, board, size, deploy_dir, rootfs_link, seed_filename])
        shutil.copy(os.path.join(deploy_dir, yaml_config), pkg_work_dir)
    except Exception as e:
        bb.fatal(f"Fehler bei YAML Erstellung: {e}")

    # 4. Dateien "ent-symlinken" und in pkg_work_dir kopieren
    resolved_names = []
    for filename in set(files_to_include): # set() vermeidet Dopplungen
        src = os.path.join(deploy_dir, filename)
        dst = os.path.join(pkg_work_dir, filename)

        if os.path.exists(src):
            real_src = os.path.realpath(src)
            shutil.copyfile(real_src, dst)
            resolved_names.append(filename)
    
    shutil.copyfile(f"{deploy_dir}/{seed_filename}", os.path.join(pkg_work_dir, f"{seed_filename}"))
    # 5. Partup im temporären Verzeichnis ausführen
    try:
        cmd = ['partup', 'package', '-f', package_output] + resolved_names + [yaml_config]
        subprocess.check_call(cmd, cwd=pkg_work_dir)
        
        # Ergebnis zurück ins deploy_dir
        shutil.move(os.path.join(pkg_work_dir, package_output), os.path.join(deploy_dir, package_output))
        bb.plain(f"SUCCESS: {package_output} erstellt mit {len(resolved_names)} Dateien.")
    except subprocess.CalledProcessError as e:
        bb.fatal(f"Partup Fehler: {e}")
    finally:
        # Aufräumen
        shutil.rmtree(pkg_work_dir)
}

# Add the generate_partup_package task to the build process
addtask generate_partup_package after do_image_complete before do_build

# Ensure that the generate_partup_package task runs after the necessary files are in place
do_generate_partup_package[depends] += "partup-native:do_populate_sysroot"