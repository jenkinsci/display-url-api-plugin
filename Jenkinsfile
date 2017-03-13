pipeline {
  agent { docker 'maven:maven:3.3.3' }
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
