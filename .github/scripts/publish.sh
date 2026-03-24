getProp(){
   grep "${1}" gradle.properties | cut -d'=' -f2 | sed 's/\r//'
}
publishVersion=$(getProp VERSION_NAME)
snapshotSuffix='SNAPSHOT'

chmod +x ./gradlew

if [[ "$publishVersion" != *"$snapshotSuffix"* ]]; then
  echo "auto release artifacts of ${publishVersion}"
  ./gradlew :kotlin-source-printer:publishAndReleaseToMavenCentral
  ./gradlew :kotlin-compile-testing-extensions:publishAndReleaseToMavenCentral 
else
  echo "public artifacts of ${publishVersion}"
  ./gradlew :kotlin-source-printer:publishAllPublicationsToMavenCentral    
  ./gradlew :kotlin-compile-testing-extensions:publishAllPublicationsToMavenCentral 
fi
