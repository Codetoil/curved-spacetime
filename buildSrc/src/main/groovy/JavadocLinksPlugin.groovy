import io.freefair.gradle.plugins.maven.javadoc.JavadocLinkUtil
import io.freefair.gradle.plugins.maven.javadoc.ResolveJavadocLinks
import io.freefair.gradle.plugins.okhttp.OkHttpPlugin
import org.gradle.api.JavaVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskProvider
import org.gradle.api.tasks.javadoc.Javadoc
import org.gradle.external.javadoc.MinimalJavadocOptions
import org.gradle.external.javadoc.StandardJavadocDocletOptions

class JavadocLinksPlugin implements Plugin<Project> {

    private Project project

    @Override
    void apply(Project project) {
        this.project = project

        project.getPlugins().apply(OkHttpPlugin.class)

        project.getPlugins().withType(AggregateJavadocPlugin.class).configureEach(ajp -> {

            project.afterEvaluate(p -> {
                addJavaSeLink(ajp.getJavadocTask())
                this.addLinks(ajp.getJavadocTask(), ajp.getJavadocClasspath())
            })
        })

        project.getPlugins().withType(JavaPlugin.class).tap {
            configureEach(javaPlugin -> {
                TaskProvider<Javadoc> javadoc = project.getTasks().named(JavaPlugin.JAVADOC_TASK_NAME, Javadoc.class)

                def main = project.getExtensions().getByType(JavaPluginExtension.class).getSourceSets().named("main")
                def compileClasspath = project.getConfigurations().named(main.get().getCompileClasspathConfigurationName())

                project.afterEvaluate(p -> {
                    addJavaSeLink(javadoc)
                    this.addLinks(javadoc, compileClasspath.get())
                })
            })
        }
    }

    void addLinks(TaskProvider<Javadoc> javadocTaskProvider, Configuration classpath) {

        TaskProvider<ResolveJavadocLinks> resolveJavadocLinks = project.getTasks().register("resolveJavadocLinks", ResolveJavadocLinks.class)

        resolveJavadocLinks.configure(rjd -> {
            rjd.getJavadocTool().convention(javadocTaskProvider.get().getJavadocTool())
            rjd.setClasspath(classpath)
        })

        javadocTaskProvider.configure(javadoc -> {
            javadoc.dependsOn(resolveJavadocLinks)
            javadoc.getInputs()
                    .file(resolveJavadocLinks.map(ResolveJavadocLinks::getOutputFile))
                    .withPathSensitivity(PathSensitivity.NAME_ONLY)

            MinimalJavadocOptions options = javadoc.getOptions()

            if (options instanceof StandardJavadocDocletOptions) {
                StandardJavadocDocletOptions standardOptions = (StandardJavadocDocletOptions) options
                standardOptions.linksFile(resolveJavadocLinks.get().getOutputFile().get().getAsFile())
            }
        })

    }

    private static void addJavaSeLink(TaskProvider<Javadoc> javadocTaskProvider) {

        javadocTaskProvider.configure(jd -> {
            JavaVersion javaVersion = JavadocLinkUtil.getJavaVersion(jd)
            String link = JavadocLinkUtil.getJavaSeLink(javaVersion)

            MinimalJavadocOptions options = jd.getOptions()
            if (options instanceof StandardJavadocDocletOptions) {
                ((StandardJavadocDocletOptions) options).links(link)
            }
        })
    }

}
