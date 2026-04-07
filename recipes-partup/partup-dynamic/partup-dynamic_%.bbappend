FILESEXTRAPATHS:prepend := "${THISDIR}/files:"

do_configure_prepend() {
    python3 ${THISDIR}/files/generate-partup.py ${BOARDNAME} ${EMMC_SIZE_MB}
}

SRC_URI += "file://${BOARDNAME}-partup.xml"