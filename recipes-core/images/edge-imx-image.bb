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

    # 1. Suche das Skript direkt in den Layer-Pfaden (statt im WORKDIR)
    # FILESPATH enthält alle 'files' Ordner der Layer
    script_name = "generate-partup.py"
    script = bb.utils.which(d.getVar('FILESPATH'), script_name)

    if not script or not os.path.exists(script):
        # Fallback: Suche relativ zum Recipe-File
        this_dir = os.path.dirname(d.getVar('FILE'))
        script = os.path.join(this_dir, "files", script_name)

    if not os.path.exists(script):
        bb.fatal(f"Kritischer Fehler: {script_name} wurde nirgendwo gefunden!")

    # Parameter holen
    board = d.getVar('BOARDNAME')
    size = d.getVar('EMMC_SIZE_MB')
    deploy_dir = d.getVar('DEPLOY_DIR_IMAGE_PATH')

    if not os.path.exists(deploy_dir):
        os.makedirs(deploy_dir)

    # 2. Skript ausführen
    try:
        bb.note(f"Nutze Skript direkt aus Layer: {script}")
        subprocess.check_call(['python3', script, board, size, deploy_dir])
    except subprocess.CalledProcessError as e:
        bb.fatal(f"Fehler beim Ausführen des Partup-Generators: {e}")

    # 3. Log-Ausgabe des Inhalts
    generated_file = os.path.join(deploy_dir, f"{board}.partup")
    if os.path.exists(generated_file):
        with open(generated_file, 'r') as f:
            bb.plain(f"\n--- Generierte Partup-Konfiguration ({board}.partup) ---\n{f.read()}\n---------------------------------------\n")
}

# Task-Reihenfolge (do_unpack/addtask unpack können wir hier weglassen)
addtask generate_partup_config after do_rootfs before do_image_partup

