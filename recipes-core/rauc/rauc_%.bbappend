FILESEXTRAPATHS:prepend := "${THISDIR}/files:"

SRC_URI:append:generic-arm64 = " file://generic-arm64/system.conf"

do_install:append:generic-arm64() {
    install -d ${D}${sysconfdir}/rauc
    install -m 0644 ${WORKDIR}/generic-arm64/system.conf ${D}${sysconfdir}/rauc/system.conf
}
