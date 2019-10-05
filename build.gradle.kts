/*
 * This file was generated by the Gradle 'init' task.
 *
 * This generated file contains a sample Java project to get you started.
 * For more details take a look at the Java Quickstart chapter in the Gradle
 * user guide available at https://docs.gradle.org/5.0/userguide/tutorial_java_projects.html
 */

plugins {
    // Apply the java plugin to add support for Java
    java

    // Apply the application plugin to add support for building an application
    application

    // Apply the groovy plugin to also add support for Groovy (needed for Spock)
    groovy
}

repositories {
    // Use jcenter for resolving your dependencies.
    // You can declare any Maven/Ivy/file repository here.
    jcenter()
     
}

dependencies {
    // This dependency is found on compile classpath of this component and consumers.
    implementation("org.apache.xmlgraphics:batik-all:1.11")
    implementation("org.apache.commons:commons-lang3:3.9")
}

application {
    // Define the main class for the application
    mainClassName = "flag.icon.css.transcoder.FlagIconTranscoder"

   
 
}
