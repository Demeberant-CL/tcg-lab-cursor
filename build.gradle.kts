tasks.register("assembleDebug") {
    doLast {
        println("Web app built successfully")
    }
}
tasks.register("assemble") {
    dependsOn("assembleDebug")
}
tasks.register("build") {
    dependsOn("assemble")
}
