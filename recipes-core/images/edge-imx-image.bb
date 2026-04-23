SUMMARY = "edge yocto custom image with RAUC, snapd with partup packaging"
LICENSE = "CLOSED"

require recipes-images/images/phytec-headless-image.bb


#  Default values for variables, can be overridden by local.conf or machine configuration
BOARDNAME ?= "edge-imx93-segin"
EMMC_SIZE_MB ?= "7260"
DEPLOY_DIR_IMAGE_PATH = "${DEPLOY_DIR_IMAGE}"

IMAGE_FSTYPES += "partup"
IMAGE_INSTALL:append = " snapd"

# Add the generate-partup.py script to the source files for this recipe
SRC_URI += " file://generate-partup.py "

# This function will be called after the image is built to generate the partup package
python do_generate_partup_package() {
    import subprocess
    import os
    import shutil
    import glob

    # 1. Variablen vorbereiten
    board = d.getVar('BOARDNAME')
    size = d.getVar('EMMC_SIZE_MB')
    deploy_dir = d.getVar('DEPLOY_DIR_IMAGE_PATH')
    rootfs_link = d.getVar('IMAGE_LINK_NAME')
    machine = d.getVar('MACHINE')
    seed_path = f"{deploy_dir}/{machine}-seed.tar.gz"
    package_output = f"{board}.partup"
    yaml_config = "layout.yaml"

    # 2. Temporäres Arbeitsverzeichnis erstellen
    pkg_work_dir = os.path.join(deploy_dir, "partup_work")
    if os.path.exists(pkg_work_dir):
        shutil.rmtree(pkg_work_dir)
    os.makedirs(pkg_work_dir)

    # 3. Alle relevanten Dateien sammeln und REAL in pkg_work_dir kopieren
    # (Das löst die Symlinks auf und stellt sicher, dass das Python-Skript sie sieht)
    search_patterns = [
        "imx-boot", "Image", "oftree", "tee.bin", "bootenv.txt",
        "config-partition.tar.gz", f"{rootfs_link}.ext4",
        "*.dtb", "*.dtbo", "*.bin"
    ]

    files_to_copy = []
    for p in search_patterns:
        files_to_copy.extend(glob.glob(os.path.join(deploy_dir, p)))

    files_to_copy.append(seed_path)

    for src in set(files_to_copy):
        filename = os.path.basename(src)
        dst = os.path.join(pkg_work_dir, filename)
        # Realpath auflösen, um SameFileError zu vermeiden und echte Daten zu kopieren
        shutil.copyfile(os.path.realpath(src), dst)

    # 4. Jetzt das Python-Skript aufrufen
    # WICHTIG: Wir übergeben pkg_work_dir als Argument, damit es dort scannen kann
    script = bb.utils.which(d.getVar('FILESPATH'), 'generate-partup.py')
    if not script:
        bb.fatal("Skript generate-partup.py nicht gefunden!")

    try:
        # Aufruf: script BOARD SIZE WORK_DIR ROOTFS_LINK SEED_FILENAME
        # Beachte: SEED_FILENAME nur als Name, da das Skript im WORK_DIR arbeitet
        subprocess.check_call([
            'python3', script, 
            board, size, pkg_work_dir, rootfs_link, os.path.basename(seed_path)
        ])
    except subprocess.CalledProcessError as e:
        bb.fatal(f"Fehler beim Erstellen der layout.yaml: {e}")

    # 5. Partup Paket bauen (im pkg_work_dir)
    try:
        # Wir nehmen alle Dateien im Ordner (außer das Zielpaket selbst)
        all_files = [f for f in os.listdir(pkg_work_dir) if f != yaml_config]
        cmd = ['partup', 'package', '-f', package_output] + all_files + [yaml_config]
        
        subprocess.check_call(cmd, cwd=pkg_work_dir)
        
        # Paket ins finale deploy_dir verschieben
        shutil.move(os.path.join(pkg_work_dir, package_output), os.path.join(deploy_dir, package_output))
        bb.plain(f"SUCCESS: {package_output} erstellt.")
    except subprocess.CalledProcessError as e:
        bb.fatal(f"Partup packaging fehlgeschlagen: {e}")
    finally:
        # Aufräumen
        shutil.rmtree(pkg_work_dir)
}
modify_rootfs() {
    install -m 0644 ${THISDIR}/files/fstab ${IMAGE_ROOTFS}/etc/fstab
    current_work_dir=$(pwd)
    cd ${IMAGE_ROOTFS}
    rm -f ${DEPLOY_DIR_IMAGE}/${MACHINE}-seed.tar ${DEPLOY_DIR_IMAGE}/${MACHINE}-seed.tar.gz
    tar cf ${DEPLOY_DIR_IMAGE}/${MACHINE}-seed.tar var/lib/snapd var/snap
    cd ${current_work_dir}
    cat ${IMAGE_ROOTFS}/etc/fstab
    rm -rf ${IMAGE_ROOTFS}/var/lib/snapd
    rm -rf ${IMAGE_ROOTFS}/var/snap
    gzip ${DEPLOY_DIR_IMAGE}/${MACHINE}-seed.tar
}


ROOTFS_POSTPROCESS_COMMAND += " modify_rootfs; "

# Add the generate_partup_package task to the build process
addtask generate_partup_package after do_image_complete before do_build

# Ensure that the generate_partup_package task runs after the necessary files are in place
do_generate_partup_package[depends] += "partup-native:do_populate_sysroot"