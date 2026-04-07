DESCRIPTION = "Generate Partup XML only (image packaging, no flash)"
LICENSE = "MIT"

inherit python3native

# Parameter setzen
BOARDNAME ?= "phyboard-segin-imx93-2"
EMMC_SIZE_MB ?= "7280"

# Python Script aus Layer Files
SRC_URI = "file://generate-partup.py"
SRC_URI[md5sum] = e8b6f3018131749b3fa6c03a06efc53f
SRC_URI[sha256sum] = 782b0d89ea16e41fd801ca05357293dd668ec345d94561ec246b3faef3f016e5
do_generate() {
    THISDIR="$(dirname "$0")"
    SCRIPT="$THISDIR/generate-partup.py"

    # Aufruf des Python-Skripts
    python3 "$SCRIPT" "$BOARDNAME" "$EMMC_SIZE_MB"
}

# Nur generate-Task, kein flashen
addtask generate after do_configure before do_build