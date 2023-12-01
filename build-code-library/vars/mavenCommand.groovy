import static com.build.Utilities.*

def call(Map config = [:]) {
    if (config.options != null)
        mvn config.this, config.mavenHome, "${config.options} ${config.command}"
    else
        mvn config.this, config.mavenHome, "${config.command}"
}
