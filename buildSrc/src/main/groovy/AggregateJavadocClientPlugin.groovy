import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.tasks.TaskProvider
import org.gradle.api.tasks.javadoc.Javadoc

class AggregateJavadocClientPlugin implements Plugin<Project> {

    private ConfigurableFileCollection javadocClasspath

    private TaskProvider<Task> collectJavadocClasspath

    @Override
    void apply(Project project) {
        collectJavadocClasspath = project.getTasks().register("collectJavadocClasspath")
        javadocClasspath = project.files().builtBy(collectJavadocClasspath)

        project.getPlugins().withType(JavaPlugin.class).tap {
            configureEach(javaPlugin -> {
                collectJavadocClasspath.configure(c -> {
                    Javadoc javadoc = (Javadoc) project.getTasks().named(JavaPlugin.JAVADOC_TASK_NAME)
                    c.setEnabled(javadoc.isEnabled())
                    c.doFirst(t -> {
                        javadocClasspath
                                .from(javadoc.getClasspath().getFiles())
                                .builtBy(javadoc.getClasspath())
                    })
                })
            })
        }
    }


}
