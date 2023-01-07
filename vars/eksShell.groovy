def call(String registryCred = 'a', String registryname = 'a', String docTag = 'a', String grepo = 'a', String gbranch = 'a', String gitcred = 'a', String depname = 'a', String contname = 'a') {

pipeline {
environment { 
		registryCredential = "${registryCred}"
    registry = "${registryname}" 	
		dockerTag = "${docTag}$BUILD_NUMBER"
		gitRepo = "${grepo}"
		gitBranch = "${gbranch}"
		gitCredId = "${gitcred}"
    deployment = "${depname}"
    containerName = "${contname}"
	}
		
  agent none

  stages {
      stage('Git Checkout') {
        agent { label 'docker' }
        steps {
          checkout scmGit(branches: [[name: '$gitBranch']], extensions: [], userRemoteConfigs: [[credentialsId: '$gitCredId', url: '$gitRepo']])
        }
      }

      stage('build') {
        agent { label 'docker' }
        steps {
          sh 'docker build -t $registry:$dockerTag .'
        }
      }

      stage('push to dockerhub') {
        agent { label 'docker' }
        steps {
          sh 'docker push $registry:$dockerTag'
        }
      }

      stage('deploy to k8s') {
        agent { label 'eks' }
        steps {
          sh 'kubectl set image deploy $deployment $containerName="$registry:$dockerTag" --record'
        }
      }
}
}
