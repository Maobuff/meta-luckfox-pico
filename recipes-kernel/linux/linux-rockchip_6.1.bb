# Copyright (C) 2024, Rockchip Electronics Co., Ltd
# Released under the MIT license (see COPYING.MIT for the terms)

inherit python3-dir

DEPENDS:append = " openssl-native lz4-native ${PYTHON_PN}-native"

FILESEXTRAPATHS:prepend := "${THISDIR}/files:"

require recipes-kernel/linux/linux-yocto.inc

SRCREV = "${AUTOREV}"
SRC_URI = " \
	git://github.com/radxa/kernel.git;protocol=https;branch=linux-6.1-stan-rkr4.1-buildroot; \
    file://luckfox_pico_max_defconfig \
	file://rv1106g-luckfox-pico-max.dts \
	file://rv1106-luckfox-pico-pro-max-ipc.dtsi \
"

LIC_FILES_CHKSUM = "file://COPYING;md5=6bc538ed5bd9a7fc9398086aedcd7e46"

KERNEL_VERSION_SANITY_SKIP = "1"
LINUX_VERSION ?= "6.1"

KERNEL_DEFCONFIG_luckfox-pico-max = "${WORKDIR}/luckfox_pico_max_defconfig"

do_patch:append() {
	for s in `grep -rIl python ${S}/scripts`; do
		sed -i -e '1s|^#!.*python[23]*|#!/usr/bin/env ${PYTHON_PN}|' $s
	done
}

do_configure:append() {
	cp ${WORKDIR}/rv1106g-luckfox-pico-max.dts ${S}/arch/arm/boot/dts
	cp ${WORKDIR}/rv1106-luckfox-pico-pro-max-ipc.dtsi ${S}/arch/arm/boot/dts

	echo "dtb-$(CONFIG_ARCH_ROCKCHIP) += rv1106g-luckfox-pico-max.dtb" >> ${S}/arch/arm/boot/dts/Makefile
}

KERNEL_IMAGETYPES:append = \
	"${@' boot.img'}"
python () {
    if not d.getVar('KERNEL_DEVICETREE'):
        raise bb.parse.SkipPackage('KERNEL_DEVICETREE is not specified!')

        # Use rockchip stype target, which is '<dts(w/o suffix)>.img'
    d.setVar('KERNEL_IMAGETYPE_FOR_MAKE', ' ' + d.getVar('KERNEL_DEVICETREE').replace('rockchip/', '').replace('.dtb', '.img'));
}

# Link rockchip style images
do_install:prepend() {
	for image in $(ls "${B}/" | grep ".img$"); do
		ln -rsf ${B}/${image} ${B}/arch/${ARCH}/boot/
	done
}
