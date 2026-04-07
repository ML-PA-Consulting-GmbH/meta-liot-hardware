DESCRIPTION = "Generate Partup XML only (image packaging, no flash)"
LICENSE = "MIT"

inherit python3native

# Parameter setzen
BOARDNAME ?= "phyboard-segin-imx93-2"
EMMC_SIZE_MB ?= "7280"

# Python Script aus Layer Files
SRC_URI = "file://generate-partup.py"

do_generate() {
    import os
    import subprocess

    script = os.path.join(d.getVar('THISDIR', True), 'files', 'generate-partup.py')
    echo ${script}
    subprocess.run(['python3', script, d.getVar('BOARDNAME', True), d.getVar('EMMC_SIZE_MB', True)], check=True)
}

# Nur generate-Task, kein flashen
addtask generate after do_configure before do_build