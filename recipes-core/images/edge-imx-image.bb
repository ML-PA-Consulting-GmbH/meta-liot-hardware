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

    workdir = d.getVar('WORKDIR')
    script = os.path.join(workdir, 'generate-partup.py')
    board = d.getVar('BOARDNAME')
    size = d.getVar('EMMC_SIZE_MB')
    deploy_dir = d.getVar('DEPLOY_DIR_IMAGE_PATH')

    if not os.path.exists(deploy_dir):
        os.makedirs(deploy_dir)

    try:
        bb.note(f"Starte Partup-Konfigurations-Generator für {board}...")
        subprocess.check_call(['python3', script, board, size, deploy_dir])
    except subprocess.CalledProcessError as e:
        bb.fatal(f"Fehler bei der Partup-Generierung: {e}")
}

addtask generate_partup_config after do_rootfs before do_image_partup
