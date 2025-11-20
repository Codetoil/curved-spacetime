package io.freefair.gradle.plugins.maven.javadoc;

import org.gradle.api.JavaVersion;
import org.gradle.api.plugins.JavaPluginExtension;
import org.gradle.api.tasks.javadoc.Javadoc;
import org.gradle.jvm.toolchain.JavaLanguageVersion;
import org.gradle.util.GradleVersion;

/**
 * @author Lars Grefer
 */
public class JavadocLinkUtil {

    public static JavaVersion getJavaVersion(Javadoc javadoc) {
        if (javadoc.getJavadocTool().isPresent()) {
            JavaLanguageVersion languageVersion = javadoc.getJavadocTool().get().getMetadata().getLanguageVersion();
            return JavaVersion.toVersion(languageVersion.asInt());
        }

        JavaPluginExtension javaPluginExtension = javadoc.getProject().getExtensions().findByType(JavaPluginExtension.class);

        if (javaPluginExtension != null) {
            return javaPluginExtension.getSourceCompatibility();
        }

        return JavaVersion.current();
    }

    public static String getJavaSeLink(JavaVersion javaVersion) {

        if (javaVersion.isJava11Compatible()) {
            return "https://docs.oracle.com/en/java/javase/" + javaVersion.getMajorVersion() + "/docs/api/";
        }
        else {
            return "https://docs.oracle.com/javase/" + javaVersion.getMajorVersion() + "/docs/api/";
        }
    }

    public static String getGradleApiLink(GradleVersion gradleVersion) {
        if (gradleVersion == null) {
            gradleVersion = GradleVersion.current();
        }

        return "https://docs.gradle.org/" + gradleVersion.getVersion() + "/javadoc/";
    }
}
