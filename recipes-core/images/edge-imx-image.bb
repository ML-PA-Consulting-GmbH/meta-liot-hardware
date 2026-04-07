SUMMARY = "edge yocto custom image with RAUC, snapd with partup packaging"
LICENSE = "CLOSED"

require recipes-images/images/phytec-headless-image.bb

#  Default values for variables, can be overridden by local.conf or machine configuration
BOARDNAME ?= "edge-imx93-yo"
EMMC_SIZE_MB ?= "7260"
DEPLOY_DIR_IMAGE_PATH = "${DEPLOY_DIR_IMAGE}"

IMAGE_FSTYPES += "partup"
IMAGE_INSTALL:append = " snapd"

# Add the generate-partup.py script to the source files for this recipe
SRC_URI += "file://generate-partup.py"

# This function will be called after the image is built to generate the partup package
python do_generate_partup_package() {
    import subprocess
    import os

    script = bb.utils.which(d.getVar('FILESPATH'), 'generate-partup.py')
    if not script:
        bb.fatal("Skript generate-partup.py nicht im Layer gefunden!")

    board = d.getVar('BOARDNAME')
    size = d.getVar('EMMC_SIZE_MB')
    deploy_dir = d.getVar('DEPLOY_DIR_IMAGE_PATH')
    rootfs_link = d.getVar('IMAGE_LINK_NAME')
    seed_filename = f"{deploy_dir}/edge-imx93-yo-seed.tar.gz"
    yaml_config = "layout.yaml"
    package_output = f"{board}.partup.pkg"

    files_to_include = [
        "imx-boot",
        "Image",
        "oftree",
        f"{rootfs_link}.ext4",
        f"{seed_filename}"
    ]
    try:
        #  Generate the YAML configuration file for partup
        subprocess.check_call(['python3', script, board, size, deploy_dir, rootfs_link, seed_filename])
    except subprocess.CalledProcessError as e:
        bb.fatal(f"Error in creating yaml file: {e}")

    # Now call partup to create the package
    cmd = ['partup', 'package', '-f' , package_output] + files_to_include + [yaml_config]

    try:
        # Run the partup command in the deploy directory
        subprocess.check_call(cmd, cwd=deploy_dir)
        
        with open(os.path.join(deploy_dir, yaml_config), 'r') as f:
            bb.plain(f"\n--- PARTUP CONFIG GENERATED ---\n{f.read()}\n")
            
        bb.plain(f"SUCCESS: partup packge created in {deploy_dir}/{package_output}")
    except subprocess.CalledProcessError as e:
        bb.fatal(f"ERROR: partup package creation failed (check please if all needed files are in the deploy folder): {e}")
}

# Add the generate_partup_package task to the build process
addtask generate_partup_package after do_image_complete before do_build

# Ensure that the generate_partup_package task runs after the necessary files are in place
do_generate_partup_package[depends] += "partup-native:do_populate_sysroot"