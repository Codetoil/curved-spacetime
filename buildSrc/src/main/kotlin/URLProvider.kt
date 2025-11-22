import org.gradle.api.artifacts.ModuleVersionIdentifier

fun urlProviderFunc(id: ModuleVersionIdentifier): String {
    if (id.group == "org.quiltmc" && id.name == "quilt-loader") {
        return "https://javadoc.quiltmc.org/quilt-loader/${id.version}/"
    }

    return "https://javadoc.io/doc/${id.group}/${id.name}/${id.version}/"
}
