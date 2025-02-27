require recipes-bsp/u-boot/u-boot.inc
require recipes-bsp/u-boot/u-boot-common.inc

PROVIDES = "virtual/bootloader"

DEPENDS += "bc-native u-boot-tools-native dtc-native"

PV = "2017.09"

LIC_FILES_CHKSUM = "file://Licenses/README;md5=a2c678cfd4a4d97135585cad908541c6"

SRCREV = "${AUTOREV}"
SRCREV_rkbin = "${AUTOREV}"
SRC_URI = " \
	git://github.com/radxa/u-boot.git;protocol=https;branch=next-dev-buildroot; \
    git://github.com/radxa/rkbin.git;protocol=https;branch=develop-v2024.10;name=rkbin;destsuffix=rkbin; \
    file://relocated-environment.cfg \
"
SRCREV_FORMAT = "default_rkbin"

EXTRA_OEMAKE += " KCFLAGS='\
    -Wno-error=enum-int-mismatch \
    -Wno-error=address \
    -Wno-error=maybe-uninitialized' \
"
#TODO: check if CONFIG_FIT_IMAGE_POST_PROCESS=y is needed
UBOOT_MACHINE += "${@oe.utils.conditional('RK_BOOTMEDIA', 'spi-nand', 'rk-sfc.config', '', d)}"
UBOOT_MACHINE += "${@oe.utils.conditional('RK_BOOTMEDIA', 'spi-nor', 'rk-sfc.config', '', d)}"
UBOOT_MACHINE += "${@oe.utils.conditional('RK_BOOTMEDIA', 'emmc', 'rk-emmc.config', '', d)}"
UBOOT_MACHINE += "${@oe.utils.conditional('RK_BOOTMEDIA', 'sdcard', 'rk-emmc.config', '', d)}"

do_configure:prepend() {
    sed -i -e '/^select_tool/d' -e '/^clean/d' -e '/^\t*make/d' -e '/which python2/{n;n;s/exit 1/true/}' ${S}/make.sh 

    [ ! -e "${S}/.config" ] || make -C ${S} mrproper
}

RK_LOADER_BIN = "loader.bin"
RK_IDBLOCK_IMG = "idblock.img"
RK_ENV_IMG = "env.img"
RK_ENV_TXT = "env.txt"

UBOOT_BINARY = "uboot.img"

do_compile:append() {
    cd ${B}

	# Prepare needed files
	for d in make.sh scripts configs arch/arm/mach-rockchip; do
		cp -rT ${S}/${d} ${d}
	done
        
    local RK_BOOT_INI="../rkbin/RKBOOT/RV1106MINIALL.ini"
    #TODO: RK_ROOTFS_PART_NUM should be parsed from RK_ENV_PART
    local RK_ROOTFS_PART_NUM=4

    echo "blkdevparts=mmcblk1:${RK_ENV_PART}" > .${RK_ENV_TXT}
    echo "sys_bootargs=root=/dev/mmcblk1p${RK_ROOTFS_PART_NUM}" >> .${RK_ENV_TXT}

    bbplain $(cat .${RK_ENV_TXT})

    mkenvimage -s ${RK_ENV_SIZE} -p 0x0 -o ${RK_ENV_IMG} .${RK_ENV_TXT}

    ./make.sh --spl-new ${RK_BOOT_INI}

    ln -sf *_download_*.bin "${RK_LOADER_BIN}"
    ln -sf *_idblock_*.img "${RK_IDBLOCK_IMG}"
}

do_deploy:append() {
    cd ${B}

    install ${RK_LOADER_BIN} "${DEPLOYDIR}/${RK_LOADER_BIN}"
    install ${RK_IDBLOCK_IMG} "${DEPLOYDIR}/${RK_IDBLOCK_IMG}"
    install ${RK_ENV_IMG} "${DEPLOYDIR}/${RK_ENV_IMG}"
    install .${RK_ENV_TXT} "${DEPLOYDIR}/${RK_ENV_TXT}"
}
