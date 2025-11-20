import org.gradle.api.artifacts.ModuleVersionIdentifier

fun urlProviderFunc(id: ModuleVersionIdentifier): String {
    return "https://javadoc.io/doc/${id.group}/${id.name}/${id.version}/"
}
