# meta-liot-hardware
Layer for Yocto to provide compatibility with L.IoT hardware
# Overview:
.
├── LICENSE
├── README.md
├── conf
│   ├── layer.conf
│   └── machine
│       └── edge-imx93-yo.conf
└── recipes-core
    ├── base-files
    │   └── files
    │       └── fstab
    └── images
        ├── edge-imx-image.bb
        └── files
            └── generate-partup.py
