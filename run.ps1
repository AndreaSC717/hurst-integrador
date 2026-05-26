Set-Location $PSScriptRoot
mvn -q "-f" "$PSScriptRoot\pom.xml" compile org.openjfx:javafx-maven-plugin:0.0.8:run
