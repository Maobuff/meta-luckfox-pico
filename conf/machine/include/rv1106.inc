require conf/machine/include/arm/armv7a/tune-cortexa7.inc

PREFERRED_PROVIDER_virtual/bootloader ?= "u-boot-rockchip"

PREFERRED_PROVIDER_virtual/kernel ?= "linux-rockchip"

KERNEL_IMAGETYPE ?= "Image"
