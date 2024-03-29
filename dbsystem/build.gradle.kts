plugins {
    scala
}

dependencies {
    // Use Scala 2.11 in our library project
    compile("org.scala-lang:scala-library:2.11.12")

    // Use Scalatest for testing our library
    testCompile("junit:junit:4.12")
    testCompile("org.scalatest:scalatest_2.11:3.0.5")

    // Need scala-xml at test runtime
    testRuntime("org.scala-lang.modules:scala-xml_2.11:1.1.0")
}

repositories {
    jcenter()
}
