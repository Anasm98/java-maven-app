def buildJar() {
    echo "building the application..."
    sh 'mvn clean package'
} 

def versionInc() {
    echo "incrementing app version"
    sh 'mvn build-helper:parse-version versions:set -DnewVersion=\\\${parsedVersion.majorVersion}.\\\${parsedVersion.minorVersion}.\\\${parsedVersion.nextIncrementalVersion} versions:commit'
    def matcher = readFile('pom.xml') =~ '<version>(.+)</version>'
    def version = matcher[0][1]
    env.IMAGE_NAME = "$version-$BUILD_NUMBER"
} 

def buildImage() {
    echo "building the docker image..."
    withCredentials([usernamePassword(credentialsId: 'docker-hub-cred', passwordVariable: 'PASS', usernameVariable: 'USER')]) {
        sh "docker build -t anasm98/javmav-app-anasm:${IMAGE_NAME} ."
        sh "echo $PASS | docker login -u $USER --password-stdin"
        sh "docker push anasm98/javmav-app-anasm:${IMAGE_NAME}"
    }
} 

def deployApp() {
    echo 'deploying the application...'
} 

def versionUptCmmt() {
    echo 'deploying the application...'
    withCredentials([usernamePassword(credentialsId: 'github-credential', passwordVariable: 'PASS', usernameVariable: 'USER')]) {
        sh 'git config --global user.email "anasm-jenkins@example.com"'
        sh 'git config --global user.name "anasm-jenkins"'
        
        sh 'git status'
        sh 'git branch'
        sh 'git config --list'

        sh "git remote set-url origin https://$GIT_TOKEN@github.com/Anasm98/java-maven-app.git"
        sh 'git add .'
        sh 'git commit -am "ci: version bump"'
        sh 'git push origin HEAD:main'
    }
} 

return this

