require recipes-images/images/phytec-headless-image.bb

DESCRIPTION = "L.IOT headless image with RAUC and snapd integration"

# Parameter
BOARDNAME ?= "phyboard-segin-imx93-2"
EMMC_SIZE_MB ?= "7280"
DEPLOY_DIR_IMAGE_PATH = "${DEPLOY_DIR_IMAGE}"

IMAGE_FSTYPES += "partup"
IMAGE_INSTALL:append = " snapd"
# Lokale Dateien brauchen keine Checksummen im Recipe
SRC_URI += "file://generate-partup.py"

python do_generate_partup_config() {
    import subprocess
    import os

    # Pfade auflösen
    # WORKDIR ist das Verzeichnis, in dem Bitbake das Skript aus SRC_URI ablegt
    workdir = d.getVar('WORKDIR')
    script = os.path.join(workdir, 'generate-partup.py')
    board = d.getVar('BOARDNAME')
    size = d.getVar('EMMC_SIZE_MB')
    deploy_dir = d.getVar('DEPLOY_DIR_IMAGE_PATH')

    if not os.path.exists(deploy_dir):
        os.makedirs(deploy_dir)

    # Aufruf des Skripts (Argumente: BOARD, SIZE, OUTPUT_DIR)
    try:
        bb.note(f"Starte Partup-Konfigurations-Generator für {board}...")
        subprocess.check_call(['python3', script, board, size, deploy_dir])
    except subprocess.CalledProcessError as e:
        bb.fatal(f"Fehler bei der Partup-Generierung: {e}")
}

# WICHTIG: Der Task-Name sollte eindeutig sein
addtask generate_partup_config after do_rootfs before do_image_partup
