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
"
SRCREV_FORMAT = "default_rkbin"

EXTRA_OEMAKE += " KCFLAGS='\
    -Wno-error=enum-int-mismatch \
    -Wno-error=address \
    -Wno-error=maybe-uninitialized' \
"

do_configure:prepend() {
       sed -i -e '/^select_tool/d' -e '/^clean/d' -e '/^\t*make/d' -e '/which python2/{n;n;s/exit 1/true/}' ${S}/make.sh 

       [ ! -e "${S}/.config" ] || make -C ${S} mrproper
}

RK_LOADER_BIN="loader.bin"
RK_IDBLOCK_IMG="idblock.img"
RK_ENV_IMG="env.img"

UBOOT_BINARY = "uboot.img"

do_compile:append() {
        cd ${B}

	# Prepare needed files
	for d in make.sh scripts configs arch/arm/mach-rockchip; do
		cp -rT ${S}/${d} ${d}
	done
        
        local RK_BOOT_INI
        case $RK_BOOTMEDIA in
            spl-nor)
                RK_BOOT_INI="../rkbin/RKBOOT/RV1106MINIALL_SPI_NOR.ini"
                ;;
            *)
                RK_BOOT_INI="../rkbin/RKBOOT/RV1106MINIALL_SPI_NAND_TB.ini"
                ;;
        esac

        echo "mtdparts=rk-nand:$(RK_ENV_PART)" > .env.txt
        mkenvimage -s 8192 -p 0x0 -o env.img .env.txt

        ./make.sh --spl-new $RK_BOOT_INI

        ln -sf *_download_*.bin "${RK_LOADER_BIN}"
        ln -sf *_idblock_*.img "${RK_IDBLOCK_IMG}"
}

do_deploy:append() {
        cd ${B}

        install ${RK_LOADER_BIN} "${DEPLOYDIR}/${RK_LOADER_BIN}"
        install ${RK_IDBLOCK_IMG} "${DEPLOYDIR}/${RK_IDBLOCK_IMG}"
        install ${RK_ENV_IMG} "${DEPLOYDIR}/${RK_ENV_IMG}"
}
