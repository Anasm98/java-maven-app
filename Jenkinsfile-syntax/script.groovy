def buildJar() {
    echo "building the application..."
    sh 'mvn package'
} 

def buildImage() {
    echo "building the docker image..."
    withCredentials([usernamePassword(credentialsId: 'docker-hub-cred', passwordVariable: 'PASS', usernameVariable: 'USER')]) {
        sh 'docker build -t anasm98/javmav-app-anasm:jma-anasm-1.0.2 .'
        sh "echo $PASS | docker login -u $USER --password-stdin"
        sh 'docker push anasm98/javmav-app-anasm:jma-anasm-1.0.2'
    }
} 

def deployApp() {
    echo 'deploying the application...'
} 

return this

