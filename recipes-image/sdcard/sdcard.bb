SUMMARY = "SD card image generator"
COMPATIBLE_MACHINE = "(luckfox-pico-max)"

inherit genimage

LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COREBASE}/meta/files/common-licenses/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302"

SRC_URI += "file://genimage.config"

DEPENDS += "genext2fs-native"

GENIMAGE_VARIABLES[MACHINE] = "${MACHINE}"

do_genimage[depends] += " \
    virtual/bootloader:do_deploy \
    core-image-minimal:do_image_complete \
"