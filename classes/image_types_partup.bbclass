# Stub image type registration for 'partup'.
#
# The actual partup package creation is handled by the custom
# do_generate_partup_package task in the image recipe, which calls the
# partup-native tool directly. BitBake's image.bbclass requires IMAGE_CMD_partup
# to be defined whenever 'partup' appears in IMAGE_FSTYPES, so we register a
# no-op here to satisfy that requirement.

IMAGE_CMD:partup = "true"
IMAGE_TYPES += "partup"
