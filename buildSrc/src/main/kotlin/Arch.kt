val osArch: String = System.getProperty("os.arch")
val osName: String = System.getProperty("os.name")
var osNameAndArch: String = if (osName.lowercase().contains("linux")) {
    "linux" + (when (osArch) {
        "amd64", "x86_64" -> "-x64"
        "arm" -> "-arm32"
        "aarch64" -> "-arm64"
        "ppc" -> "-ppc64le"
        "riscv" -> "-riscv64"
        else -> throw RuntimeException("Unsupported CPU Architecture for Linux!")
    })
} else if (osName.lowercase().contains("bsd")) {
    "freebsd" + (when (osArch) {
        "amd64", "x86_64" -> "-x64"
        else -> throw RuntimeException("Unsupported CPU Architecture for FreeBSD!")
    })
} else if (osName.lowercase().contains("mac")) {
    "macos" + (when (osArch) {
        "amd64", "x86_64" -> "-x64"
        "aarch64" -> "-arm64"
        else -> throw RuntimeException("Unsupported CPU Architecture for macOS!")
    })
} else if (osName.lowercase().contains("windows")) {
    "windows" + (when (osArch) {
        "amd64", "x86_64" -> "-x64"
        "x86" -> "-x86"
        "aarch64" -> "-arm64"
        else -> throw RuntimeException("Unsupported CPU Architecture for Windows!")
    })
} else {
    throw RuntimeException("Unsupported Operating System!")
}