def buildJar() {
    echo "building the application..."
    sh 'mvn clean package'
} 

def versionInc() {
    echo "incrementing app version"
    sh 'mvn build-helper:parse-version versions:set -DnewVersion=\\\${parsedVersion.majorVersion}.\\\${parsedVersion.minorVersion}.\\\${parsedVersion.nextIncrementalVersion} versions:commit'
    def matcher = readFile('pom.xml') =~ '<version>(.+)</version>'
    def version = matcher[0][1]
    echo "mvn app version: $version"
    env.IMAGE_NAME = "anasm98/javmav-app-anasm:$version-$BUILD_NUMBER"
    echo "docker image version name: $IMAGE_NAME"
} 

def buildImage() {
    echo "building the docker image..."
    withCredentials([usernamePassword(credentialsId: 'docker-hub-cred', passwordVariable: 'PASS', usernameVariable: 'USER')]) {
        sh "docker build -t ${IMAGE_NAME} ."
        sh "echo $PASS | docker login -u $USER --password-stdin"
        sh "docker push ${IMAGE_NAME}"
    }
} 

def deployApp() {
    echo 'deploying the application as docker image into ec2 server...'
    def dockerCmd = "docker run -d -p 8080:8080 ${IMAGE_NAME}"
    sshagent(['ec2-server-key']) {
        sh "ssh -o StrictHostKeyChecking=no ec2-user@13.233.91.184 ${dockerCmd}" 
    }
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

