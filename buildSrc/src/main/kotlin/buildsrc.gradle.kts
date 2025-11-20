import io.freefair.gradle.plugins.maven.javadoc.JavadocLinkProvider

class FabricLinkProvider: JavadocLinkProvider {
    override fun getJavadocLink(
        group: String?,
        artifact: String?,
        version: String?
    ): String {
        TODO("Not yet implemented")
    }
}

class QuiltLinkProvider: JavadocLinkProvider {
    override fun getJavadocLink(
        group: String?,
        artifact: String?,
        version: String?
    ): String {
        TODO("Not yet implemented")
    }
}