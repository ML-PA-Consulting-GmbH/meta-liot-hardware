FILESEXTRAPATHS:prepend := "${THISDIR}/files:"
SRC_URI += "file://snapd-init.service"

SYSTEMD_SERVICE:${PN} += "snapd-init.service"