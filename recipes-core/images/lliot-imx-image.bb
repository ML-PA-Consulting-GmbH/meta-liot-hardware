require recipes-core/images/phytec-headless-image.bb

DESCRIPTION = "PHYTEC headless image with RAUC A/B and persistent snapd partition"

# Partup Layout nutzen
IMAGE_FSTYPES += "partup"
