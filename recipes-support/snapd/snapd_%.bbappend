FILESEXTRAPATHS:prepend := "${THISDIR}/files:"
SRC_URI += "file://snapd-init.service"


do_install_append() {
    install -m 0644 ${WORKDIR}/snapd-init.service ${D}${systemd_unitdir}/system/
}

SYSTEMD_SERVICE:${PN} += "snapd-init.service"

