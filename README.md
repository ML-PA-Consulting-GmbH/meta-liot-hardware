# meta-liot-hardware
Layer for Yocto to provide compatibility with L.IoT hardware
# Overview:
```
.
├── LICENSE
├── README.md
├── conf
│   ├── layer.conf
│   └── machine
│       ├── edge-imx6ul-tauri-s.conf
│       ├── edge-imx8mm-polis.conf
│       ├── edge-imx8mm-tauri-l.conf
│       ├── edge-imx8mp-pollux.conf
│       ├── edge-imx93-nash.conf
│       └── edge-imx93-segin.conf
├── recipes-core
│   └── images
│       ├── edge-imx-image.bb
│       └── files
│           ├── fstab
│           └── generate-partup.py
└── recipes-images
    └── bundles
        └── edge-imx-bundle.bb
```
