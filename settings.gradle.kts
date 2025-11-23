pluginManagement {
    repositories {
        maven {
            url = uri("https://raw.githubusercontent.com/graalvm/native-build-tools/snapshots")
        }
        gradlePluginPortal()
    }
}

rootProject.name = "curved-spacetime"

include("curved-spacetime-quilt-loader-patches")
include("curved-spacetime-cli-module")
include("curved-spacetime-vulkan-module")
include("curved-spacetime-glfw-module")
include("curved-spacetime-render-module")
include("curved-spacetime-simulator-module")
include("curved-spacetime-webserver-module")
include("curved-spacetime-glfw-render-module")
include("curved-spacetime-vulkan-glfw-module")
include("curved-spacetime-vulkan-render-module")
include("curved-spacetime-webserver-openapi-module")
include("curved-spacetime-vulkan-glfw-render-module")
include("curved-spacetime-main-module")
include("curved-spacetime-loader-module")
include("curved-spacetime-quilt-loader-module")
include("curved-spacetime-closed-world-loader-module")

project(":curved-spacetime-quilt-loader-patches").name = "curved-spacetime-quilt-loader-patches"
project(":curved-spacetime-cli-module").name = "curved-spacetime-cli-module"
project(":curved-spacetime-vulkan-module").name = "curved-spacetime-vulkan-module"
project(":curved-spacetime-glfw-module").name = "curved-spacetime-glfw-module"
project(":curved-spacetime-render-module").name = "curved-spacetime-render-module"
project(":curved-spacetime-simulator-module").name = "curved-spacetime-simulator-module"
project(":curved-spacetime-webserver-module").name = "curved-spacetime-webserver-module"
project(":curved-spacetime-glfw-render-module").name = "curved-spacetime-glfw-render-module"
project(":curved-spacetime-vulkan-glfw-module").name = "curved-spacetime-vulkan-glfw-module"
project(":curved-spacetime-vulkan-render-module").name = "curved-spacetime-vulkan-render-module"
project(":curved-spacetime-webserver-openapi-module").name = "curved-spacetime-webserver-openapi-module"
project(":curved-spacetime-vulkan-glfw-render-module").name = "curved-spacetime-vulkan-glfw-render-module"
project(":curved-spacetime-main-module").name = "curved-spacetime-main-module"
project(":curved-spacetime-loader-module").name = "curved-spacetime-loader-module"
project(":curved-spacetime-quilt-loader-module").name = "curved-spacetime-quilt-loader-module"
project(":curved-spacetime-closed-world-loader-module").name = "curved-spacetime-closed-world-loader-module"