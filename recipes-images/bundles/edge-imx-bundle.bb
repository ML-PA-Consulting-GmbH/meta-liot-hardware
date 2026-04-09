require recipes-images/bundles/phytec-headless-bundle.bb

SUMMARY = "Custom RAUC bundle for edge-imx-image"

RAUC_SLOT_rootfs = "edge-imx-image"

RAUC_CERT_FILE = "${CERT_PATH}/rauc/development-1.cert.pem"
RAUC_KEY_FILE = "${CERT_PATH}/rauc/private/development-1.key.pem"
RAUC_INTERMEDIATE_FILE = "${CERT_PATH}/rauc-intermediate/ca.cert.pem"
