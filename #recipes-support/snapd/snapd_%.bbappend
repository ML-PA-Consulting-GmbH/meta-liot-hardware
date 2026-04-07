FILESEXTRAPATHS:prepend := "${THISDIR}/files:"
SRC_URI = "file://snapd-init.service"
SRC_URI[md5sum] = "3461dbf353374791f6c1363956ab9917"
SRC_URI[sha256sum] = "8ceba576befb80e21d1dc9da44ed3a1743ddf6116896b19b7cc4d643b766b57d"

do_install:append() {
    install -m 0644 ${WORKDIR}/snapd-init.service ${D}${systemd_unitdir}/system/
}

SYSTEMD_SERVICE:${PN} += "snapd-init.service"

