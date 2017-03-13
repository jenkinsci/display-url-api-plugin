pipeline {
  agent { docker 'maven:3.3-jdk-7' }
  stages {
    stage('build') {
      steps {
        sh 'mvn clean install -Duser.home=/var/maven'
      }
    }
  }
  post {
    always {
      junit 'target/surefire-reports/**/*.xml'
    }
    success {
      archiveArtifacts artifacts: 'target/**/*.jar', fingerprint: true
    }
  }
}
