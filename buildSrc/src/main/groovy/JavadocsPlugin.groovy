import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * @author Lars Grefer
 * @see JavadocLinksPlugin
 */
class JavadocsPlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {
        project.getPluginManager().apply(JavadocLinksPlugin.class)
        project.getPluginManager().apply(JavadocUtf8Plugin.class)
    }
}
