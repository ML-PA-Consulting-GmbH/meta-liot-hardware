require recipes-images/images/phytec-headless-image.bb

DESCRIPTION = "Edge headless image with RAUC and snapd integration"

# Parameter
BOARDNAME ?= "edge-imx93-yo"
EMMC_SIZE_MB ?= "7280"
DEPLOY_DIR_IMAGE_PATH = "${DEPLOY_DIR_IMAGE}"

IMAGE_FSTYPES += "partup"
SRC_URI += "file://generate-partup.py"

python do_generate_partup_config() {
    import subprocess
    import os

    # WORKDIR prüfen
    workdir = d.getVar('WORKDIR')
    script = os.path.join(workdir, 'generate-partup.py')
    
    if not os.path.exists(script):
        bb.fatal(f"Skript nicht gefunden in {script}. Inhalt von WORKDIR: {os.listdir(workdir)}")

    board = d.getVar('BOARDNAME')
    size = d.getVar('EMMC_SIZE_MB')
    deploy_dir = d.getVar('DEPLOY_DIR_IMAGE_PATH')

    try:
        subprocess.check_call(['python3', script, board, size, deploy_dir])
    except subprocess.CalledProcessError as e:
        bb.fatal(f"Fehler bei Partup-Generierung: {e}")
}

# 1. Den Unpack-Task für das Image-Recipe reaktivieren
addtask unpack before do_generate_partup_config

# 2. Dein Skript nach dem Rootfs, aber vor dem Packaging ausführen
addtask generate_partup_config after do_rootfs before do_image_partup

# 3. Explizite Abhängigkeit setzen, damit die Dateien wirklich im WORKDIR liegen
do_generate_partup_config[depends] += "${PN}:do_unpack"
