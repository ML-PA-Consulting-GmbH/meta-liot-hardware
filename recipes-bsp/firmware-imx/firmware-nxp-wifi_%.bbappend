# workaround for a scarthgap fu¤%up in the original firmware-nxp-wifi recipe
# meta-imx and meta-freescale includes the same firmware (${PN}-nxpiw610-sdio) twice
do_configure[noexec] = "1"
do_compile[noexec] = "1"

# Deduplicate PACKAGES to fix QA Issue [packages-list]
python () {
    packages = d.getVar("PACKAGES").split()
    seen = set()
    new_packages = []
    for pkg in packages:
        if pkg not in seen:
            seen.add(pkg)
            new_packages.append(pkg)
    d.setVar("PACKAGES", " ".join(new_packages))
}

# this removes all instances of the string from PACKAGES....bad idea
# PACKAGES:remove:imx8mp-lpddr4-evk = "${PN}-nxpiw610-sdio"